package com.emre.featureFlag.flag.dto;

import com.emre.featureFlag.flag.RuleOperator;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;


@Data
public class TargetingRuleRequest {

    // Example of a filled Targeting Request:
    // priority: 1 (most priority)
    // attribute: country
    // operator: IN
    // value: US, CA, UK
    // variantKey: on
    // the targeting rule for that flag ends up being the users in US, CA, UK. So you can enable that flag for specific users to test it.

    @NotNull
    private Integer priority; // 1,2,3,4,5... 1 has the first priority

    @NotBlank
    private String attribute; // example "country", "plan"

    @NotNull
    private RuleOperator operator; // example "IN", an operator from "RuleOperator"

    @NotBlank
    private String value; // example "premium", "internal"

    @NotBlank
    private String variantKey; // "on"
}
