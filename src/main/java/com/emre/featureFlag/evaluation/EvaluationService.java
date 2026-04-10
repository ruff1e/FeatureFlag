package com.emre.featureFlag.evaluation;

import com.emre.featureFlag.evaluation.dto.EvaluationRequest;
import com.emre.featureFlag.evaluation.dto.EvaluationResponse;
import com.emre.featureFlag.flag.FeatureFlag;
import com.emre.featureFlag.flag.FlagRepository;
import com.emre.featureFlag.flag.FlagVariant;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EvaluationService {

    private final FlagRepository flagRepository;
    private final EvaluationEngine evaluationEngine;

    @Transactional(readOnly = true)
    public EvaluationResponse evaluate(EvaluationRequest request) {
        FeatureFlag flag = flagRepository.findByKey(request.getFlagKey())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Flag not found: " + request.getFlagKey()
                ));

        if (!flag.isEnabled()) {
            return getDefaultOffResponse(flag);
        }

        FlagVariant variant = evaluationEngine.evaluate(flag, request.getUserContext());
        return EvaluationResponse.from(variant);
    }

    @Transactional(readOnly = true)
    public List<EvaluationResponse> evaluateBatch(List<EvaluationRequest> requests) {
        return requests.stream()
                .map(this::evaluate)
                .toList();
    }

    private EvaluationResponse getDefaultOffResponse(FeatureFlag flag) {
        List<FlagVariant> variants = flag.getVariants();

        return variants.stream()
                .filter(v -> v.getKey().equals("off") || v.getKey().equals("false"))
                .findFirst()
                .or(() -> variants.stream().findFirst())
                .map(EvaluationResponse::from)
                .orElse(new EvaluationResponse("off", null));
    }
}