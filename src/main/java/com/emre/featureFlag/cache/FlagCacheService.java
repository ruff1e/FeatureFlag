package com.emre.featureFlag.cache;

import com.emre.featureFlag.evaluation.dto.EvaluationResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class FlagCacheService {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    private static final long TTL_MINUTES = 5;

    // Public API
    public Optional<EvaluationResponse> get(String flagKey, String userId) {
        String cacheKey = buildKey(flagKey, userId);
        String cached = redisTemplate.opsForValue().get(cacheKey);

        if (cached == null) {
            log.debug("Cache MISS for key: {}", cacheKey);
            return Optional.empty();
        }

        log.debug("Cache HIT for key: {}", cacheKey);
        return Optional.ofNullable(deserialize(cached));
    }

    public void put(String flagKey, String userId, EvaluationResponse response) {
        String cacheKey = buildKey(flagKey, userId);
        String serialized = serialize(response);

        if (serialized == null) return;

        redisTemplate.opsForValue().set(cacheKey, serialized, TTL_MINUTES, TimeUnit.MINUTES);
        log.debug("Cached evaluation for key: {}", cacheKey);
    }

    // Called when a flag is updated/toggled/deleted
    // Deletes ALL cached evaluations for this flag across all users
    public void invalidate(String flagKey) {
        String pattern = "flag:" + flagKey + ":user:*";
        Set<String> keys = redisTemplate.keys(pattern);

        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
            log.info("Invalidated {} cache entries for flag: {}", keys.size(), flagKey);
        }
    }


    private String buildKey(String flagKey, String userId) {
        return "flag:" + flagKey + ":user:" + userId;
    }

    private String serialize(EvaluationResponse response) {
        try {
            return objectMapper.writeValueAsString(response);
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize EvaluationResponse", e);
            return null;
        }
    }

    private EvaluationResponse deserialize(String json) {
        try {
            return objectMapper.readValue(json, EvaluationResponse.class);
        } catch (JsonProcessingException e) {
            log.error("Failed to deserialize EvaluationResponse from cache", e);
            return null;
        }
    }
}