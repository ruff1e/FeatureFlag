package com.emre.featureFlag.flag;

import com.emre.featureFlag.user.User;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "feature_flags")
@Data
@NoArgsConstructor
public class FeatureFlag {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String key; // Key of a flag example : "new-checkout-flow", basically a unique identifier name

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(nullable = false)
    private boolean enabled = false;

    @Enumerated(EnumType.STRING) // To actually store "BOOLEAN" OR "MULTIVARIATE" in the db ->
    @Column(nullable = false)// -> Because Java enums are int by default
    private FlagType flagType;

    @ManyToOne(fetch = FetchType.LAZY) // Don't load the user unless you specifically access it.
    @JoinColumn(name = "created_by")
    private User createdBy;

    // if you remove a variant from the list, it gets deleted from the database as well
    @OneToMany(mappedBy = "flag", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FlagVariant> variants = new ArrayList<>(); // One flag has many variants

    @OneToMany(mappedBy = "flag", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TargetingRule> targetingRules = new ArrayList<>();

    @Column(updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    private LocalDateTime updatedAt = LocalDateTime.now();


    // JPA lifecycle hook, runs automatically before every update query so the updated at stays current for db
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
