package com.emre.featureFlag.evaluation;

import com.emre.featureFlag.evaluation.dto.EvaluationRequest;
import com.emre.featureFlag.evaluation.dto.EvaluationResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/evaluate")
@RequiredArgsConstructor
public class EvaluationController {

    private final EvaluationService evaluationService;

    @PostMapping
    public ResponseEntity<EvaluationResponse> evaluate(
            @Valid @RequestBody EvaluationRequest request
    ) {
        return ResponseEntity.ok(evaluationService.evaluate(request));
    }

    @PostMapping("/batch")
    public ResponseEntity<List<EvaluationResponse>> evaluateBatch(
            @RequestBody List<@Valid EvaluationRequest> requests
    ) {
        return ResponseEntity.ok(evaluationService.evaluateBatch(requests));
    }
}