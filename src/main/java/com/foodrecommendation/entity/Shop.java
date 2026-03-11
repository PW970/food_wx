package com.foodrecommendation.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "shop")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Shop {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", length = 200)
    private String name;

    @Column(name = "address", length = 255)
    private String address;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "category", length = 100)
    private String category;

    @Column(name = "score")
    private Double score;

    @Column(name = "review_count")
    private Integer reviewCount;

    @Column(name = "cover_image", length = 500)
    private String coverImage;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "business_hours", length = 200)
    private String businessHours;

    @Column(name = "per_capita", precision = 10, scale = 2)
    private BigDecimal perCapita;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "status", length = 20)
    private String status = "OPEN";

    @Column(name = "category_id")
    private Long categoryId;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (reviewCount == null) {
            reviewCount = 0;
        }
        if (score == null) {
            score = 0.0;
        }
        if (status == null) {
            status = "OPEN";
        }
    }
}
