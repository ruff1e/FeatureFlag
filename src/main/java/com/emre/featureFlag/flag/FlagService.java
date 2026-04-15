package com.emre.featureFlag.flag;

import com.emre.featureFlag.cache.FlagCacheService;
import com.emre.featureFlag.flag.dto.*;
import com.emre.featureFlag.user.User;
import com.emre.featureFlag.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FlagService {

    private final FlagRepository flagRepository;
    private final UserRepository userRepository;
    private final FlagCacheService flagCacheService;

    // get the User entity for whoever is making the request
    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    }

    // validate that variant percentages sum to 100
    private void validateVariantPercentages(List<VariantRequest> variants) {
        int total = variants.stream().mapToInt(VariantRequest::getPercentage).sum();
        if (total != 100) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Variant percentages must sum to 100, currently it is: " + total
            );
        }
    }

    // Transactional wraps the entire method. Everything either succeeds or fails so you don't get half created flag in db.
    // For example if you save a flag successfully but get an error saving a variant, this makes the flag rollback as well.
    @Transactional
    public FlagResponse createFlag(CreateFlagRequest request) {
        if (flagRepository.existsByKey(request.getKey())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Flag key already exists: " + request.getKey()
            );
        }

        // BOOLEAN flags get auto-generated variants if they didn't provide any
        if (request.getFlagType() == FlagType.BOOLEAN && request.getVariants().isEmpty()) {
            VariantRequest on = new VariantRequest();

            // by default 0 percent of the users get the flag enabled so percentage is 0
            on.setKey("on");
            on.setPercentage(0);

            VariantRequest off = new VariantRequest();
            off.setKey("off");
            off.setPercentage(100);

            request.getVariants().add(on);
            request.getVariants().add(off);
        }

        validateVariantPercentages(request.getVariants());

        FeatureFlag flag = new FeatureFlag();
        flag.setKey(request.getKey());
        flag.setName(request.getName());
        flag.setDescription(request.getDescription());
        flag.setFlagType(request.getFlagType());
        flag.setCreatedBy(getCurrentUser());

        // Map variant requests → entities, linking each back to its flag
        for (VariantRequest vr : request.getVariants()) {
            FlagVariant variant = new FlagVariant();
            variant.setKey(vr.getKey());
            variant.setPercentage(vr.getPercentage());
            variant.setValue(vr.getValue());
            variant.setFlag(flag);
            flag.getVariants().add(variant);
        }

        // Map targeting rule requests → entities
        for (TargetingRuleRequest trr : request.getTargetingRules()) {
            TargetingRule rule = new TargetingRule();
            rule.setPriority(trr.getPriority());
            rule.setAttribute(trr.getAttribute());
            rule.setOperator(trr.getOperator());
            rule.setValue(trr.getValue());
            rule.setVariantKey(trr.getVariantKey());
            rule.setFlag(flag);
            flag.getTargetingRules().add(rule);
        }

        FeatureFlag saved = flagRepository.save(flag);

        return FlagResponse.from(saved);
    }

    @Transactional(readOnly = true) // readOnly = true for performance
    public List<FlagResponse> getAllFlags() {
        return flagRepository.findAll().stream()
                .map(FlagResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public FlagResponse getFlagByKey(String key) {
        FeatureFlag flag = flagRepository.findByKey(key)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Flag not found: " + key
                ));
        return FlagResponse.from(flag);
    }

    @Transactional
    public FlagResponse updateFlag(String key, UpdateFlagRequest request) {
        FeatureFlag flag = flagRepository.findByKey(key)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Flag not found: " + key
                ));

        if (request.getName() != null) flag.setName(request.getName());
        if (request.getDescription() != null) flag.setDescription(request.getDescription());

        if (request.getVariants() != null) {
            validateVariantPercentages(request.getVariants());

            // will delete the old rows from the DB automatically
            flag.getVariants().clear();

            for (VariantRequest vr : request.getVariants()) {
                FlagVariant variant = new FlagVariant();
                variant.setKey(vr.getKey());
                variant.setPercentage(vr.getPercentage());
                variant.setValue(vr.getValue());
                variant.setFlag(flag);
                flag.getVariants().add(variant);
            }
        }

        if (request.getTargetingRules() != null) {
            flag.getTargetingRules().clear();

            for (TargetingRuleRequest trr : request.getTargetingRules()) {
                TargetingRule rule = new TargetingRule();
                rule.setPriority(trr.getPriority());
                rule.setAttribute(trr.getAttribute());
                rule.setOperator(trr.getOperator());
                rule.setValue(trr.getValue());
                rule.setVariantKey(trr.getVariantKey());
                rule.setFlag(flag);
                flag.getTargetingRules().add(rule);
            }
        }

        flagCacheService.invalidate(key);
        return FlagResponse.from(flag);
    }

    @Transactional
    public FlagResponse toggleFlag(String key) {
        FeatureFlag flag = flagRepository.findByKey(key)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Flag not found: " + key
                ));

        flag.setEnabled(!flag.isEnabled());

        flagCacheService.invalidate(key);
        return FlagResponse.from(flag);
    }

    @Transactional
    public void deleteFlag(String key) {
        FeatureFlag flag = flagRepository.findByKey(key)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Flag not found: " + key
                ));

        flagRepository.delete(flag);
        flagCacheService.invalidate(key);
    }
}