package com.emre.featureFlag.flag.dto;

import com.emre.featureFlag.flag.RuleOperator;
import com.emre.featureFlag.flag.TargetingRule;
import java.util.UUID;


public record TargetingRuleResponse(
        UUID id,
        Integer priority,
        String attribute,
        RuleOperator operator,
        String value,
        String variantKey
) {
    public static TargetingRuleResponse from(TargetingRule r) {
        return new TargetingRuleResponse(
                r.getId(), r.getPriority(), r.getAttribute(),
                r.getOperator(), r.getValue(), r.getVariantKey()
        );
    }
}