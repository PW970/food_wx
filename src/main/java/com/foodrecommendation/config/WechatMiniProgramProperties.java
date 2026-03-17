package com.foodrecommendation.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "wechat.mini-program")
public class WechatMiniProgramProperties {
    private String appId;
    private String secret;
    private String code2sessionUrl = "https://api.weixin.qq.com/sns/jscode2session";
}
