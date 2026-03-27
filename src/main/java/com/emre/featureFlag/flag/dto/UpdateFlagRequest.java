package com.emre.featureFlag.flag.dto;

import jakarta.validation.Valid;
import lombok.Data;
import java.util.List;



@Data
public class UpdateFlagRequest {
    private String name;
    private String description;

    @Valid
    private List<VariantRequest> variants; // example Flag variants: red button, blue button, green button.
                                           // you can also use these variants on specific users with targetRule below

    @Valid
    private List<TargetingRuleRequest> targetingRules; // example, target people in US,UK, and/or user with PLAN:PREMIUM, PLAN:INTERNAL
}
