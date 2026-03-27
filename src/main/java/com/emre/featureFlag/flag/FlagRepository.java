package com.emre.featureFlag.flag;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;



@Repository
public interface FlagRepository extends JpaRepository<FeatureFlag, UUID> {
    Optional<FeatureFlag> findByKey(String key);
    boolean existsByKey(String key);
}