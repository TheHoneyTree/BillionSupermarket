# Licensed to the Apache Software Foundation (ASF) under one
# or more contributor license agreements.  See the NOTICE file
# distributed with this work for additional information
# regarding copyright ownership.  The ASF licenses this file
# to you under the Apache License, Version 2.0 (the
# "License"); you may not use this file except in compliance
# with the License.  You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing,
# software distributed under the License is distributed on an
# "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
# KIND, either express or implied.  See the License for the
# specific language governing permissions and limitations
# under the License.


import math
from typing import Optional

import torch
from torch import nn
from torch.nn import functional as F
from .configuration import Config


def _make_causal_mask(
        input_ids_shape: torch.Size, dtype: torch.dtype, device: torch.device, past_key_values_length: int = 0
):
    """
    Make causal mask used for bi-directional self-attention.
    """
    bsz, tgt_len = input_ids_shape
    mask = torch.full((tgt_len, tgt_len), torch.finfo(dtype).min, device=device)
    mask_cond = torch.arange(mask.size(-1), device=device)
    mask.masked_fill_(mask_cond < (mask_cond + 1).view(mask.size(-1), 1), 0)
    mask = mask.to(dtype)
    if past_key_values_length > 0:
        mask = torch.cat([torch.zeros(tgt_len, past_key_values_length, dtype=dtype, device=device), mask], dim=-1)
    return mask[None, None, :, :].expand(bsz, 1, tgt_len, tgt_len + past_key_values_length)


def _expand_mask(mask: torch.Tensor, dtype: torch.dtype, tgt_len: Optional[int] = None):
    """
    Expands attention_mask from `[bsz, seq_len]` to `[bsz, 1, tgt_seq_len, src_seq_len]`.
    """
    bsz, src_len = mask.size()
    tgt_len = tgt_len if tgt_len is not None else src_len
    expanded_mask = mask[:, None, None, :].expand(bsz, 1, tgt_len, src_len).to(dtype)
    inverted_mask = 1.0 - expanded_mask
    return inverted_mask.masked_fill(inverted_mask.to(torch.bool), torch.finfo(dtype).min)


def prepare_decoder_attention_mask(attention_mask, input_shape, inputs_embeds, past_key_values_length):
    # create causal mask
    # [bsz, seq_len] -> [bsz, 1, tgt_seq_len, src_seq_len]
    combined_attention_mask = None
    if input_shape[-1] > 1:
        combined_attention_mask = _make_causal_mask(
            input_shape,
            inputs_embeds.dtype,
            device=inputs_embeds.device,
            past_key_values_length=past_key_values_length,
        )
    if attention_mask is not None:
        # [bsz, seq_len] -> [bsz, 1, tgt_seq_len, src_seq_len]
        expanded_attn_mask = _expand_mask(attention_mask, inputs_embeds.dtype, tgt_len=input_shape[-1]).to(
            inputs_embeds.device
        )
        combined_attention_mask = (
            expanded_attn_mask if combined_attention_mask is None else expanded_attn_mask + combined_attention_mask
        )
    return combined_attention_mask


class CasualLMOutput:
    def __init__(self, logits: torch.Tensor, past_key_values: list):
        self.logits = logits
        self.past_key_values = past_key_values


class PositionEmbedding(nn.Module):
    def __init__(self, config: Config):
        super().__init__()
        self.dropout = nn.Dropout(config.dropout)
        self.P = torch.zeros((1, config.max_position_embeddings, config.num_hiddens))
        x = torch.arange(
            config.max_position_embeddings,
            dtype=torch.float32
        ).reshape(-1, 1) / torch.pow(
            10000,
            torch.arange(0, config.num_hiddens, 2, dtype=torch.float32) / config.num_hiddens
        )
        self.P[:, :, 0::2] = torch.sin(x)
        self.P[:, :, 1::2] = torch.cos(x)

    def forward(self, x: torch.Tensor):
        x = x + self.P[:, :x.shape[1], :]
        return self.dropout(x)


class AddNorm(nn.Module):
    def __init__(self, config: Config, **kwargs):
        super().__init__(**kwargs)
        self.dropout = nn.Dropout(config.dropout)
        self.ln = nn.LayerNorm(config.num_hiddens)

    def forward(self, x: torch.Tensor, y: torch.Tensor):
        return self.ln(self.dropout(y) + x)


class MultiHeadSelfAttention(nn.Module):
    def __init__(self, config: Config, **kwargs):
        super().__init__(**kwargs)
        self.num_heads = config.num_heads
        self.head_dim = int(config.num_hiddens / config.num_heads)
        self.Wq = nn.Linear(config.num_hiddens, config.num_hiddens, bias=False)
        self.Wk = nn.Linear(config.num_hiddens, config.num_hiddens, bias=False)
        self.Wv = nn.Linear(config.num_hiddens, config.num_hiddens, bias=False)
        self.Wo = nn.Linear(config.num_hiddens, config.num_hiddens, bias=False)
        self.dropout = nn.Dropout(config.dropout)

    def forward(self, x: torch.Tensor, attention_mask: torch.Tensor = None, past_key_value=None,
                use_cache: bool = False):
        q = self.Wq(x)
        k = self.Wk(x)
        v = self.Wv(x)
        q = q.reshape(q.size(0), q.size(1), self.num_heads, self.head_dim).transpose(1, 2)
        k = k.reshape(k.size(0), k.size(1), self.num_heads, self.head_dim).transpose(1, 2)
        v = v.reshape(v.size(0), v.size(1), self.num_heads, self.head_dim).transpose(1, 2)
        if past_key_value is not None:
            k = torch.cat([past_key_value[0], k], dim=2)
            v = torch.cat([past_key_value[1], v], dim=2)
        if use_cache:
            past_key_value = (k, v)
        output = torch.matmul(q, k.transpose(2, 3)) / math.sqrt(self.head_dim)
        if attention_mask is not None:
            output = output + attention_mask
        output = self.dropout(F.softmax(output, dim=-1))
        output = torch.matmul(output, v).transpose(1, 2).contiguous()
        output = output.reshape(output.size(0), output.size(1), -1)
        output = self.Wo(output)
        return output, past_key_value


class MLP(nn.Module):
    def __init__(self, config: Config, **kwargs):
        super().__init__(**kwargs)
        self.linear1 = nn.Linear(config.num_hiddens, config.num_mlp_intermediate)
        self.relu = nn.ReLU()
        self.linear2 = nn.Linear(config.num_mlp_intermediate, config.num_hiddens)

    def forward(self, x: torch.Tensor):
        return self.linear2(self.relu(self.linear1(x)))


class DecoderBlock(nn.Module):
    def __init__(self, config: Config, **kwargs):
        super().__init__(**kwargs)
        self.attention = MultiHeadSelfAttention(config)
        self.add_norm1 = AddNorm(config)
        self.mlp = MLP(config)
        self.add_norm2 = AddNorm(config)

    def forward(self, x: torch.Tensor, attention_mask: torch.Tensor = None, past_key_value=None,
                use_cache: bool = False):
        y, past_key_value = self.attention(x, attention_mask, past_key_value, use_cache)
        x = self.add_norm1(x, y)
        y = self.mlp(x)
        y = self.add_norm2(x, y)
        return y, past_key_value


class TransformerDecoder(nn.Module):
    def __init__(self, config: Config, **kwargs):
        super().__init__(**kwargs)
        self.num_layers = config.num_layers
        self.embedding = nn.Embedding(config.vocab_size, config.num_hiddens)
        self.pos_embedding = PositionEmbedding(config)
        self.decoder_blocks = nn.ModuleList([DecoderBlock(config) for _ in range(config.num_layers)])
        self.post_ln = nn.LayerNorm(config.num_hiddens)

    def forward(self, x: torch.LongTensor, attention_mask: torch.Tensor = None, past_key_values=None,
                use_cache: bool = False):
        embedding = self.embedding(x)
        hidden_states = self.pos_embedding(embedding)
        past_key_values_length = 0
        if past_key_values is not None:
            past_key_values_length = past_key_values[0][0].size(2)
        if attention_mask is None:
            batch_size = x.size(0)
            seq_length = x.size(1)
            seq_length_with_past = seq_length + past_key_values_length
            attention_mask = torch.ones((batch_size, seq_length_with_past), dtype=torch.bool)
        attention_mask = prepare_decoder_attention_mask(attention_mask, (batch_size, seq_length), embedding,
                                                        past_key_values_length)
        if use_cache and past_key_values is None:
            past_key_values = [None] * self.num_layers
        for idx, decoder_block in enumerate(self.decoder_blocks):
            past_key_value = past_key_values[idx] if past_key_values is not None else None
            hidden_states, past_key_value = decoder_block(hidden_states, attention_mask, past_key_value, use_cache)
            if past_key_values is not None:
                past_key_values[idx] = past_key_value
        hidden_states = self.post_ln(hidden_states)
        return hidden_states, past_key_values


class CasualLM(nn.Module):
    def __init__(self, config: Config, **kwargs):
        super().__init__(**kwargs)
        self.decoder = TransformerDecoder(config)
        self.lm_head = nn.Linear(config.num_hiddens, config.vocab_size)

    def forward(self, x: torch.LongTensor, attention_mask: torch.Tensor = None, past_key_values=None,
                use_cache: bool = False) -> CasualLMOutput:
        hidden_states, past_key_values = self.decoder(x, attention_mask, past_key_values, use_cache)
        logits = self.lm_head(hidden_states)
        return CasualLMOutput(logits, past_key_values)

    def generate(self, x: torch.LongTensor, max_new_tokens: int = 10):
        # TODO: Implement
        pass


class RecommendationModel(nn.Module):
    def __init__(self, config: Config, **kwargs):
        super().__init__(**kwargs)
        self.decoder = TransformerDecoder(config)
        self.lm_head = nn.Linear(config.num_hiddens, config.vocab_size)

    def forward(self, x: torch.LongTensor, attention_mask: torch.Tensor = None, past_key_values=None,
                use_cache: bool = False) -> CasualLMOutput:
        hidden_states, past_key_values = self.decoder(x, attention_mask, past_key_values, use_cache)
        logits = self.lm_head(hidden_states)
        return logits[:, -1, :]

    def generate(self, x: torch.LongTensor, max_new_tokens: int = 10):
        # TODO: Implement
        pass
