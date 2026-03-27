package com.emre.featureFlag.flag;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Entity
@Table(name = "targeting_rules")
@Data
@NoArgsConstructor
public class TargetingRule {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flag_id", nullable = false)
    private FeatureFlag flag;

    @Column(nullable = false)
    private Integer priority;

    @Column(nullable = false)
    private String attribute;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RuleOperator operator;

    @Column(nullable = false)
    private String value;

    @Column(nullable = false)
    private String variantKey;
}