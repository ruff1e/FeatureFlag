package com.emre.featureFlag.flag.dto;

import com.emre.featureFlag.flag.FeatureFlag;
import com.emre.featureFlag.flag.FlagType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

// Java record because its immutable and auto-generates constructor /getters etc
public record FlagResponse(
        UUID id,
        String key,
        String name,
        String description,
        boolean enabled,
        FlagType flagType,
        List<VariantResponse> variants,
        List<TargetingRuleResponse> targetingRules,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static FlagResponse from(FeatureFlag flag) {
        return new FlagResponse(
                flag.getId(),
                flag.getKey(),
                flag.getName(),
                flag.getDescription(),
                flag.isEnabled(),
                flag.getFlagType(),
                flag.getVariants().stream().map(VariantResponse::from).toList(),
                flag.getTargetingRules().stream().map(TargetingRuleResponse::from).toList(),
                flag.getCreatedAt(),
                flag.getUpdatedAt()
        );
    }
}
