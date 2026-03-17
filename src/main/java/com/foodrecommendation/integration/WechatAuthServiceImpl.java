package com.foodrecommendation.integration;

import com.foodrecommendation.config.WechatMiniProgramProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

@Service
public class WechatAuthServiceImpl implements WechatAuthService {

    private final RestTemplate restTemplate;
    private final WechatMiniProgramProperties properties;

    public WechatAuthServiceImpl(RestTemplateBuilder restTemplateBuilder,
                                 WechatMiniProgramProperties properties) {
        this.restTemplate = restTemplateBuilder.build();
        this.properties = properties;
    }

    @Override
    public String exchangeOpenid(String code) {
        if (!StringUtils.hasText(code)) {
            throw new IllegalArgumentException("缺少微信登录 code");
        }
        if (!StringUtils.hasText(properties.getAppId()) || !StringUtils.hasText(properties.getSecret())) {
            throw new IllegalStateException("未配置微信小程序 AppID 或 AppSecret");
        }

        String url = UriComponentsBuilder.fromHttpUrl(properties.getCode2sessionUrl())
                .queryParam("appid", properties.getAppId())
                .queryParam("secret", properties.getSecret())
                .queryParam("js_code", code)
                .queryParam("grant_type", "authorization_code")
                .build()
                .toUriString();

        ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
        Map<?, ?> body = response.getBody();
        if (body == null) {
            throw new IllegalStateException("微信登录服务无响应");
        }

        Object errCode = body.get("errcode");
        if (errCode instanceof Number && ((Number) errCode).intValue() != 0) {
            Object errMsg = body.get("errmsg");
            throw new IllegalStateException("微信登录失败: " + errMsg);
        }

        Object openid = body.get("openid");
        if (!(openid instanceof String) || !StringUtils.hasText((String) openid)) {
            throw new IllegalStateException("微信登录未返回 openid");
        }

        return (String) openid;
    }
}
