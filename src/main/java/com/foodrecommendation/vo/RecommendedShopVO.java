package com.foodrecommendation.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

/**
 * 推荐店铺VO - 用于推荐结果展示
 * 包含店铺基本信息、推荐分数、推荐理由等
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecommendedShopVO {

    /**
     * 店铺ID
     */
    private Long shopId;

    /**
     * 店铺名称
     */
    private String shopName;

    /**
     * 店铺封面图片
     */
    private String coverImage;

    /**
     * 店铺分类名称
     */
    private String categoryName;

    /**
     * 店铺标签列表
     */
    private List<String> tags;

    /**
     * 原始评分
     */
    private Double originalScore;

    /**
     * 距离（千米）
     */
    private Double distance;

    /**
     * 综合推荐分数
     */
    private Double recommendScore;

    /**
     * 推荐理由解释
     */
    private String recommendReason;
}
