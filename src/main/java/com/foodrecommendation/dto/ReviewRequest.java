package com.foodrecommendation.dto;

import lombok.Data;

@Data
public class ReviewRequest {
    private Long userId;
    private Long shopId;
    private Integer rating;
    private String content;
}
