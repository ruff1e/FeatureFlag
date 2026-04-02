package com.emre.featureFlag.evaluation;

public class EvaluationEngine {

}
/*
    CLASS EvaluationEngine

    METHOD evaluate(flag, userContext):

    //is the flag even on?
    IF flag is not enabled:
    find the variant in flag.variants where key == "off"
            return it

            // Step 2 — get userId out of userContext, you'll need it for hashing
            userId = userContext.get("userId")

    // Step 3 — targeting rules, checked in priority order
    sort flag.targetingRules by priority ascending

    FOR each rule in sortedRules:

    userAttribute = userContext.get(rule.attribute)
    // if the user doesn't have this attribute, skip this rule entirely

    IF rule.operator == EQUALS:
    does userAttribute equal rule.value?

    IF rule.operator == IN:
    split rule.value by "," into a list
    does that list contain userAttribute?

    IF rule.operator == NOT_IN:
    split rule.value by "," into a list
    does that list NOT contain userAttribute?

    IF rule.operator == PERCENTAGE:
    hash the userId → number 0-99
    is that number less than Integer.parseInt(rule.value)?

    IF the rule matched:
    find the variant in flag.variants where key == rule.variantKey
            return it immediately, stop checking rules

    // Step 4 — no rules matched, use percentage buckets
    hash = Math.abs(userId.hashCode()) % 100

    cumulative = 0
    FOR each variant in flag.variants:
    cumulative += variant.percentage
    IF hash < cumulative:
            return this variant
*/
