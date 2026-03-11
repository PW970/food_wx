package com.foodrecommendation.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * 评论VO - 用于前端展示
 * 在Review基础字段上，增加了 nickname(用户昵称), avatar(用户头像)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewVO {

    private Long id;
    private Long userId;
    private Long shopId;
    private Integer rating;
    private String content;
    private LocalDateTime createdAt;

    /**
     * 评论用户昵称
     */
    private String nickname;

    /**
     * 评论用户头像
     */
    private String avatar;
}
