package com.emre.featureFlag.evaluation;

import com.emre.featureFlag.cache.FlagCacheService;
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
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class EvaluationService {

    private final FlagRepository flagRepository;
    private final EvaluationEngine evaluationEngine;
    private final FlagCacheService flagCacheService;

    @Transactional(readOnly = true)
    public EvaluationResponse evaluate(EvaluationRequest request) {
        String flagKey = request.getFlagKey();
        String userId = request.getUserContext().get("userId");

        // first check Redis cache first
        Optional<EvaluationResponse> cached = flagCacheService.get(flagKey, userId);
        if (cached.isPresent()) {
            return cached.get();
        }

        // Cache miss — load from Postgre
        FeatureFlag flag = flagRepository.findByKey(flagKey)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Flag not found: " + flagKey
                ));

        // Flag is disabled — return default off variant
        //  so toggle updates are reflected immediately
        if (!flag.isEnabled()) {
            return getDefaultOffResponse(flag);
        }

        // Run evaluation engine
        FlagVariant variant = evaluationEngine.evaluate(flag, request.getUserContext());
        EvaluationResponse response = EvaluationResponse.from(variant);

        // Store result in Redis with 5 min TTL
        flagCacheService.put(flagKey, userId, response);

        return response;
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