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


import torch.onnx

from architecture.modeling import RecommendationModel
from architecture.configuration import Config


config = Config(
    vocab_size=26,
    num_hiddens=16,
    num_layers=4,
    num_heads=4,
    num_mlp_intermediate=32,
    pad_token_id=0,
    bos_token_id=None,
    eos_token_id=None,
    dropout=0.01
)
model = RecommendationModel(config)
x = torch.zeros((1, 26), dtype=torch.long)


torch.onnx.export(model, x, "model.onnx",
                  input_names=["inputs"], output_names=["outputs"],
                  dynamic_axes={
                      "inputs": {0: 'batch_size', 1: 'seq_length'},
                      "outputs": {0: 'batch_size'}})
