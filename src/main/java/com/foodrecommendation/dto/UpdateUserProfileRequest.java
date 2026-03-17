package com.foodrecommendation.dto;

import lombok.Data;

@Data
public class UpdateUserProfileRequest {
    private Long userId;
    private String nickname;
    private String avatar;
}
