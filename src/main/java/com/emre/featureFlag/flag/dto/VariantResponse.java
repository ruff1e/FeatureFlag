package com.emre.featureFlag.flag.dto;

import com.emre.featureFlag.flag.FlagVariant;
import java.util.UUID;



public record VariantResponse(UUID id, String key, Integer percentage, String value) {
    public static VariantResponse from(FlagVariant v) {
        return new VariantResponse(v.getId(), v.getKey(), v.getPercentage(), v.getValue());
    }
}
