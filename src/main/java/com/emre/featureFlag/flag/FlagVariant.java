package com.emre.featureFlag.flag;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.UUID;

@Entity
@Table(name = "flag_variants")
@Data
@NoArgsConstructor
public class FlagVariant {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "flag_id", nullable = false)
    private FeatureFlag flag;

    @Column(nullable = false)
    private String key;

    @Column(nullable = false)
    private Integer percentage;

    @Column(columnDefinition = "jsonb") // to store the flag in json
    private String value; // for a complex flag you can have, example: {"color": "#FF0000", "text": "Buy Now"}
}
