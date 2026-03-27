package com.emre.featureFlag.flag.dto;

import jakarta.validation.constraints.*;
import lombok.Data;


@Data
public class VariantRequest {

    @NotBlank(message = "Variant key is required")
    private String key;

    @NotNull
    @Min(0) @Max(100)
    private Integer percentage;

    private String value;
}