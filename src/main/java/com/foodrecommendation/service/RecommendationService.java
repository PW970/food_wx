package com.foodrecommendation.service;

import java.util.List;
import java.util.Map;

/**
 * 推荐服务接口 - V3 推荐系统核心
 * 提供基于贝叶斯评分、地理距离和用户偏好的混合推荐算法
 */
public interface RecommendationService {

    /**
     * 为指定用户生成推荐店铺列表
     *
     * @param userId     用户ID
     * @param userLat    用户当前位置纬度（可选，为空时忽略距离因子）
     * @param userLon    用户当前位置经度（可选，为空时忽略距离因子）
     * @param limit      返回结果数量限制
     * @return 推荐结果列表，每个元素包含 shopId, recommendScore, recommendReason
     */
    List<Map<String, Object>> getRecommendations(Long userId, Double userLat, Double userLon, Integer limit);

    /**
     * 获取全平台平均评分（C值）
     * 用于贝叶斯评分计算
     *
     * @return 全平台平均评分
     */
    double getGlobalAverageScore();

    /**
     * 计算单个店铺的贝叶斯评分
     *
     * @param shopId 店铺ID
     * @return 贝叶斯评分
     */
    double calculateBayesianScore(Long shopId);
}
