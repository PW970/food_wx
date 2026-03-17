package com.foodrecommendation.dto;

import lombok.Data;

@Data
public class OpenidLoginRequest {
    private String openid;
    private String nickname;
    private String avatar;
}
