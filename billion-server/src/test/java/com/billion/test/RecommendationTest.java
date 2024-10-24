package com.billion.test;

import ai.onnxruntime.OrtException;
import com.billion.algrithm.Recommendation;
import com.billion.properties.OnnxProperties;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

public class RecommendationTest {

    @Test
    public void test() throws OrtException {
        OnnxProperties onnxProperties = new OnnxProperties();
        onnxProperties.setDevice("cpu");
        Recommendation recommendation = new Recommendation(onnxProperties);
        long[][] inputs = new long[1][12];
        float[][] result = recommendation.forward(inputs);
        int[][] rank = recommendation.getRecommendation(inputs);
        System.out.println(Arrays.deepToString(result));
        System.out.println(Arrays.deepToString(rank));
        System.out.printf("(%d, %d)", result.length, result[0].length);
    }
}
