package com.emre.featureFlag.evaluation.dto;

import com.emre.featureFlag.flag.FlagVariant;


public record EvaluationResponse(
        String variantKey,
        String value
) {

    public static EvaluationResponse from(FlagVariant variant) {
        return new EvaluationResponse(
                variant.getKey(),
                variant.getValue()
        );
    }
}
