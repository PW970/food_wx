package com.foodrecommendation.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 店铺VO - 用于前端展示
 * 在Shop基础字段上，增加了 tags(标签列表), isCollected(当前用户是否收藏), categoryName(分类名称)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShopVO {

    private Long id;
    private String name;
    private String address;
    private Double latitude;
    private Double longitude;
    private String category;
    private Double score;
    private Integer reviewCount;
    private String coverImage;
    private String phone;
    private String businessHours;
    private BigDecimal perCapita;
    private String description;
    private String status;
    private Long categoryId;
    private String categoryName;
    private LocalDateTime createdAt;

    /**
     * 店铺标签列表
     */
    private List<String> tags;

    /**
     * 当前用户是否已收藏
     */
    private Boolean isCollected;
}
