package com.foodrecommendation.integration;

public interface WechatAuthService {
    String exchangeOpenid(String code);
}
