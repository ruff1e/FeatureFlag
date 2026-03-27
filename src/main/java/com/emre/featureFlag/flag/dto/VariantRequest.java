package com.emre.featureFlag.flag.dto;

import jakarta.validation.constraints.*;
import lombok.Data;


@Data
public class VariantRequest {

    @NotBlank(message = "Variant key is required")
    private String key;

    @NotNull
    @Min(0) @Max(100)
    private Integer percentage; // variant percentage example:
                                // "on" : 10%
                                // "off": 90%
                                // 10 percent of the total users get this variant, every user has a hash value of 0-100 depending on their id
                                // so users with hash value 0-10 get this variant, for example red checkout button.

    private String value;
}
