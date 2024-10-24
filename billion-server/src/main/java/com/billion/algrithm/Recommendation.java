package com.billion.algrithm;

import ai.onnxruntime.*;
import com.billion.properties.OnnxProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class Recommendation {
    private OnnxProperties onnxProperties;

    private OrtEnvironment env;

    private OrtSession session;

    public Recommendation(OnnxProperties onnxProperties) throws OrtException {
        this.onnxProperties = onnxProperties;
        this.env = OrtEnvironment.getEnvironment();
        this.session = env.createSession("/mnt/user/model/bge/onnx/model.onnx", new OrtSession.SessionOptions());
    }

    public float[][] forward(long[][] inputs) {
        try {
            int batchSize = inputs.length;
            OnnxTensor tensor = OnnxTensor.createTensor(env, inputs);

            Map<String, OnnxTensor> map = new HashMap<>();
            map.put("inputs", tensor);
            OrtSession.Result outputs = session.run(map);
            for (Map.Entry<String, OnnxValue> r : outputs) {
                OnnxValue resultValue = r.getValue();
                OnnxTensor resultTensor = (OnnxTensor) resultValue;
                float[] resultArray = resultTensor.getFloatBuffer().array();
                int itemNum = resultArray.length / batchSize;
                float[][] batchArray = new float[batchSize][itemNum];
                for (int i = 0; i < batchSize; i++) {
                    System.arraycopy(resultArray, i * itemNum, batchArray[i], 0, itemNum);
                }
                return batchArray;
            }
        } catch (OrtException e) {
            e.printStackTrace();
        }
        return null;
    }

    public int[][] getRecommendation(long[][] inputs) {
        float[][] batchOutputs = this.forward(inputs);
        int batchDim = batchOutputs.length;
        int classDim = batchOutputs[0].length;
        int[][] result = new int[batchDim][classDim];
        for (int i = 0; i < batchDim; i++) {
            result[i] = argSort(batchOutputs[i], true);
        }
        return result;
    }


    private int[] argSort(float[] arr, boolean desc) {
        float temp;
        int index;
        int k = arr.length;
        int[] Index = new int[k];
        for (int i = 0; i < k; i++) {
            Index[i] = i;
        }

        for (int i = 0; i < arr.length; i++) {
            for (int j = 0; j < arr.length - i - 1; j++) {
                if (desc) {
                    if (arr[j] < arr[j + 1]) {
                        temp = arr[j];
                        arr[j] = arr[j + 1];
                        arr[j + 1] = temp;

                        index = Index[j];
                        Index[j] = Index[j + 1];
                        Index[j + 1] = index;
                    }
                } else {
                    if (arr[j] > arr[j + 1]) {
                        temp = arr[j];
                        arr[j] = arr[j + 1];
                        arr[j + 1] = temp;

                        index = Index[j];
                        Index[j] = Index[j + 1];
                        Index[j + 1] = index;
                    }
                }
            }
        }
        return Index;
    }
}
