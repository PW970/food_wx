package com.foodrecommendation.integration;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 腾讯地图附近地点简化模型
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TencentNearbyPlace {

    private String title;
    private String address;
    private String category;
    private Double distanceMeters;
}
