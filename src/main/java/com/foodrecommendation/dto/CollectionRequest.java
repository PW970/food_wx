package com.foodrecommendation.dto;

import lombok.Data;

@Data
public class CollectionRequest {
    private Long userId;
    private Long shopId;
}
