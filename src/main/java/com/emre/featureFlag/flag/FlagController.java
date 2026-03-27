package com.emre.featureFlag.flag;

import com.emre.featureFlag.flag.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;



@RestController
@RequestMapping("/flags")
@RequiredArgsConstructor
public class FlagController {

    private final FlagService flagService;

    @PostMapping
    public ResponseEntity<FlagResponse> createFlag(@Valid @RequestBody CreateFlagRequest request) {
        FlagResponse response = flagService.createFlag(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<List<FlagResponse>> getAllFlags() {
        return ResponseEntity.ok(flagService.getAllFlags());
    }

    @GetMapping("/{key}")
    public ResponseEntity<FlagResponse> getFlagByKey(@PathVariable String key) {
        return ResponseEntity.ok(flagService.getFlagByKey(key));
    }

    @PutMapping("/{key}")
    public ResponseEntity<FlagResponse> updateFlag(
            @PathVariable String key,
            @Valid @RequestBody UpdateFlagRequest request
    ) {
        return ResponseEntity.ok(flagService.updateFlag(key, request));
    }

    @PatchMapping("/{key}/toggle")
    public ResponseEntity<FlagResponse> toggleFlag(@PathVariable String key) {
        return ResponseEntity.ok(flagService.toggleFlag(key));
    }

    @DeleteMapping("/{key}")
    public ResponseEntity<Void> deleteFlag(@PathVariable String key) {
        flagService.deleteFlag(key);
        return ResponseEntity.noContent().build(); // 204
    }
}
