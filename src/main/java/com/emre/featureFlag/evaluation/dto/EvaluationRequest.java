package com.emre.featureFlag.evaluation.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.Map;


@Data
public class EvaluationRequest {

    @NotBlank(message = "Flag key is required")
    private String flagKey;

    @NotNull(message = "The user context can not be empty")
    private Map<String, String> userContext;

}
