package com.emre.featureFlag.flag.dto;

import jakarta.validation.Valid;
import lombok.Data;
import java.util.List;



@Data
public class UpdateFlagRequest {
    private String name;
    private String description;

    @Valid
    private List<VariantRequest> variants;

    @Valid
    private List<TargetingRuleRequest> targetingRules;
}