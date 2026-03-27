package com.emre.featureFlag.flag.dto;

import com.emre.featureFlag.flag.FlagType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.ArrayList;
import java.util.List;



@Data
public class CreateFlagRequest {

    @NotBlank(message = "Flag key is required")
    private String key;

    @NotBlank(message = "Flag name is required")
    private String name;

    private String description;

    @NotNull
    private FlagType flagType;

    @Valid
    private List<VariantRequest> variants = new ArrayList<>();

    @Valid
    private List<TargetingRuleRequest> targetingRules = new ArrayList<>();
}
