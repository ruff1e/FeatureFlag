package com.emre.featureFlag.flag.dto;

import com.emre.featureFlag.flag.RuleOperator;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;


@Data
public class TargetingRuleRequest {

    @NotNull
    private Integer priority;

    @NotBlank
    private String attribute; // example "country", "plan"

    @NotNull
    private RuleOperator operator;

    @NotBlank
    private String value; // example "US,UK"

    @NotBlank
    private String variantKey;
}