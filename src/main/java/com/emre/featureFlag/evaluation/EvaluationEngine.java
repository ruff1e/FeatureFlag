package com.emre.featureFlag.evaluation;

import com.emre.featureFlag.flag.FeatureFlag;
import com.emre.featureFlag.flag.FlagVariant;
import com.emre.featureFlag.flag.RuleOperator;
import com.emre.featureFlag.flag.TargetingRule;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

@Component
public class EvaluationEngine {

    public FlagVariant evaluate(FeatureFlag flag, Map<String, String> userContext) {
        String userId = userContext.get("userId");

        if (userId == null || userId.isBlank()) {
            throw new IllegalArgumentException(
                    "userContext must contain a 'userId' field"
            );
        }

        int userHash = hashUserId(userId);

        List<TargetingRule> sortedRules = flag.getTargetingRules().stream()
                .sorted(Comparator.comparingInt(TargetingRule::getPriority))
                .toList();

        for (TargetingRule rule : sortedRules) {
            if (matchesRule(rule, userContext, userHash)) {
                return findVariantByKey(flag.getVariants(), rule.getVariantKey());
            }
        }

        return assignByPercentage(flag.getVariants(), userHash);
    }

    private boolean matchesRule(
            TargetingRule rule,
            Map<String, String> userContext,
            int userHash
    ) {
        RuleOperator operator = rule.getOperator();
        String ruleValue = rule.getValue();

        if (operator == RuleOperator.PERCENTAGE) {
            int threshold = Integer.parseInt(ruleValue);
            return userHash < threshold;
        }

        String userValue = userContext.get(rule.getAttribute());

        if (userValue == null) return false;

        return switch (operator) {
            case EQUALS -> userValue.equalsIgnoreCase(ruleValue);
            case IN -> Arrays.stream(ruleValue.split(","))
                    .map(String::trim)
                    .anyMatch(v -> v.equalsIgnoreCase(userValue));
            case NOT_IN -> Arrays.stream(ruleValue.split(","))
                    .map(String::trim)
                    .noneMatch(v -> v.equalsIgnoreCase(userValue));
            default -> false;
        };
    }

    private FlagVariant findVariantByKey(List<FlagVariant> variants, String variantKey) {
        return variants.stream()
                .filter(v -> v.getKey().equals(variantKey))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "No variant found with key: " + variantKey
                ));
    }

    private FlagVariant assignByPercentage(List<FlagVariant> variants, int userHash) {
        if (variants == null || variants.isEmpty()) {
            throw new IllegalStateException("Flag has no variants configured");
        }

        int cumulative = 0;
        for (FlagVariant variant : variants) {
            cumulative += variant.getPercentage();
            if (userHash < cumulative) {
                return variant;
            }
        }

        return variants.get(variants.size() - 1);
    }

    private int hashUserId(String userId) {
        return Math.abs(userId.hashCode()) % 100;
    }
}