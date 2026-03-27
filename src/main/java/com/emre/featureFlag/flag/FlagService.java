package com.emre.featureFlag.flag;

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
                    "Variant percentages must sum to 100, got: " + total
            );
        }
    }

    @Transactional
    public FlagResponse createFlag(CreateFlagRequest request) {
        if (flagRepository.existsByKey(request.getKey())) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Flag key already exists: " + request.getKey()
            );
        }

        // BOOLEAN flags get auto-generated variants if they didnt provide any
        if (request.getFlagType() == FlagType.BOOLEAN && request.getVariants().isEmpty()) {
            VariantRequest on = new VariantRequest();
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
        for (TargetingRuleRequest rr : request.getTargetingRules()) {
            TargetingRule rule = new TargetingRule();
            rule.setPriority(rr.getPriority());
            rule.setAttribute(rr.getAttribute());
            rule.setOperator(rr.getOperator());
            rule.setValue(rr.getValue());
            rule.setVariantKey(rr.getVariantKey());
            rule.setFlag(flag);
            flag.getTargetingRules().add(rule);
        }

        FeatureFlag saved = flagRepository.save(flag);

        // TODO Phase 7: auditService.record(saved, Action.CREATED, getCurrentUser(), null)

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

            for (TargetingRuleRequest rr : request.getTargetingRules()) {
                TargetingRule rule = new TargetingRule();
                rule.setPriority(rr.getPriority());
                rule.setAttribute(rr.getAttribute());
                rule.setOperator(rr.getOperator());
                rule.setValue(rr.getValue());
                rule.setVariantKey(rr.getVariantKey());
                rule.setFlag(flag);
                flag.getTargetingRules().add(rule);
            }
        }

        return FlagResponse.from(flag);
    }

    @Transactional
    public FlagResponse toggleFlag(String key) {
        FeatureFlag flag = flagRepository.findByKey(key)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Flag not found: " + key
                ));

        flag.setEnabled(!flag.isEnabled());

        return FlagResponse.from(flag);
    }

    @Transactional
    public void deleteFlag(String key) {
        FeatureFlag flag = flagRepository.findByKey(key)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Flag not found: " + key
                ));

        flagRepository.delete(flag);
    }
}