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


from typing import Optional


class Config:
    def __init__(
            self,
            vocab_size: int = 1000,
            num_hiddens: int = 256,
            num_layers: int = 12,
            num_heads: int = 16,
            num_mlp_intermediate: Optional[int] = None,
            max_position_embeddings: int = 1024,
            pad_token_id: Optional[int] = 0,
            bos_token_id: Optional[int] = 1,
            eos_token_id: Optional[int] = 2,
            dropout: float = 0.01
    ):
        assert num_hiddens % num_heads == 0
        self.vocab_size = vocab_size
        self.num_hiddens = num_hiddens
        self.num_layers = num_layers
        self.num_heads = num_heads
        self.num_mlp_intermediate = num_mlp_intermediate if num_mlp_intermediate is not None else 4 * num_hiddens
        self.max_position_embeddings = max_position_embeddings
        self.pad_token_id = pad_token_id
        self.bos_token_id = bos_token_id
        self.eos_token_id = eos_token_id
        self.dropout = dropout
