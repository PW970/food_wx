package com.foodrecommendation.service.impl;

import com.foodrecommendation.entity.Shop;
import com.foodrecommendation.entity.ShopTag;
import com.foodrecommendation.repository.CollectionRepository;
import com.foodrecommendation.repository.ReviewRepository;
import com.foodrecommendation.repository.ShopRepository;
import com.foodrecommendation.repository.ShopTagRepository;
import com.foodrecommendation.service.RecommendationService;
import com.foodrecommendation.utils.GeoUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 推荐服务实现类 - V3 推荐系统核心
 * 实现基于贝叶斯评分、地理距离和用户偏好的混合推荐算法
 *
 * 推荐算法公式：
 * recommendScore = 0.5 * bayesianScore + 0.3 * distanceScore + 0.2 * preferenceScore
 */
@Service
public class RecommendationServiceImpl implements RecommendationService {

    /**
     * 贝叶斯评分最小评论阈值
     */
    private static final int MIN_REVIEW_THRESHOLD = 10;

    /**
     * 贝叶斯评分权重
     */
    private static final double BAYESIAN_WEIGHT = 0.5;

    /**
     * 距离评分权重
     */
    private static final double DISTANCE_WEIGHT = 0.3;

    /**
     * 偏好评分权重
     */
    private static final double PREFERENCE_WEIGHT = 0.2;

    @Autowired
    private ShopRepository shopRepository;

    @Autowired
    private CollectionRepository collectionRepository;

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ShopTagRepository shopTagRepository;

    /**
     * 缓存全平台平均评分
     */
    private Double globalAverageScoreCache = null;

    @Override
    public List<Map<String, Object>> getRecommendations(Long userId, Double userLat, Double userLon, Integer limit) {
        // 参数安全校验
        if (userId == null) {
            userId = 0L; // 防止空指针，按新用户处理
        }

        // 1. 获取全平台平均评分（C值）
        double globalAverageScore = getGlobalAverageScore();

        // 2. 获取用户偏好标签权重
        Map<String, Double> userPreferenceWeights = getUserPreferenceWeights(userId);

        // 3. 获取所有店铺
        List<Shop> allShops = shopRepository.findAll();

        // 处理空店铺列表的情况
        if (allShops == null || allShops.isEmpty()) {
            return new ArrayList<>();
        }

        // 4. 为每个店铺计算推荐分数
        List<Map<String, Object>> recommendations = new ArrayList<>();

        for (Shop shop : allShops) {
            // 跳过无效店铺（无评分或已关闭）
            if (shop.getScore() == null || shop.getScore() <= 0) {
                continue;
            }
            if (shop.getStatus() != null && "CLOSED".equals(shop.getStatus())) {
                continue;
            }

            // ============ 步骤1: 计算贝叶斯评分 ============
            double bayesianScore = calculateBayesianScoreForShop(shop, globalAverageScore);

            // ============ 步骤2: 计算距离评分 ============
            double distanceScore = 0.0;
            double distanceKm = Double.MAX_VALUE;
            if (userLat != null && userLon != null && shop.getLatitude() != null && shop.getLongitude() != null) {
                distanceKm = GeoUtils.calculateDistance(userLat, userLon, shop.getLatitude(), shop.getLongitude());
                distanceScore = GeoUtils.calculateDistanceScore(distanceKm);
            }

            // ============ 步骤3: 计算偏好评分 ============
            double preferenceScore = calculatePreferenceScore(shop, userPreferenceWeights);

            // ============ 步骤4: 计算总推荐分数 ============
            double totalScore = BAYESIAN_WEIGHT * bayesianScore
                    + DISTANCE_WEIGHT * distanceScore
                    + PREFERENCE_WEIGHT * preferenceScore;

            // ============ 步骤5: 生成推荐解释 ============
            String recommendReason = generateRecommendReason(
                    bayesianScore, distanceScore, preferenceScore,
                    shop.getScore(), distanceKm, userPreferenceWeights, shop.getId()
            );

            // 构建结果
            Map<String, Object> result = new HashMap<>();
            result.put("shopId", shop.getId());
            result.put("shopName", shop.getName());
            result.put("shopScore", shop.getScore());
            result.put("shopReviewCount", shop.getReviewCount());
            result.put("recommendScore", Math.round(totalScore * 100.0) / 100.0);
            result.put("bayesianScore", Math.round(bayesianScore * 100.0) / 100.0);
            result.put("distanceScore", Math.round(distanceScore * 100.0) / 100.0);
            result.put("preferenceScore", Math.round(preferenceScore * 100.0) / 100.0);
            result.put("distanceKm", distanceKm == Double.MAX_VALUE ? null : distanceKm);
            result.put("recommendReason", recommendReason);

            recommendations.add(result);
        }

        // 6. 按推荐分数降序排序
        if (recommendations.isEmpty()) {
            return recommendations;
        }
        recommendations.sort((r1, r2) -> {
            double score1 = ((Number) r1.get("recommendScore")).doubleValue();
            double score2 = ((Number) r2.get("recommendScore")).doubleValue();
            return Double.compare(score2, score1);
        });

        // 7. 返回前 limit 条结果
        if (limit != null && limit > 0) {
            return recommendations.stream().limit(limit).collect(Collectors.toList());
        }

        return recommendations;
    }

    @Override
    public double getGlobalAverageScore() {
        // 如果缓存存在，直接返回
        if (globalAverageScoreCache != null) {
            return globalAverageScoreCache;
        }

        // 计算全平台所有店铺的平均评分
        List<Shop> allShops = shopRepository.findAll();
        double totalScore = 0.0;
        int validShopCount = 0;

        for (Shop shop : allShops) {
            if (shop.getScore() != null && shop.getScore() > 0) {
                totalScore += shop.getScore();
                validShopCount++;
            }
        }

        // 如果没有有效店铺，返回默认评分 3.0
        if (validShopCount == 0) {
            globalAverageScoreCache = 3.0;
            return globalAverageScoreCache;
        }

        globalAverageScoreCache = totalScore / validShopCount;
        return globalAverageScoreCache;
    }

    @Override
    public double calculateBayesianScore(Long shopId) {
        Shop shop = shopRepository.findById(shopId).orElse(null);
        if (shop == null) {
            return 0.0;
        }
        return calculateBayesianScoreForShop(shop, getGlobalAverageScore());
    }

    /**
     * 计算单个店铺的贝叶斯评分
     *
     * 公式：weightedRating = (v / (v + m)) * R + (m / (v + m)) * C
     * - R = 该店铺真实平均分 (shop.score)
     * - v = 该店铺总评论数 (shop.reviewCount)
     * - m = 最小评论阈值（10）
     * - C = 全平台所有店铺的平均分
     *
     * @param shop 店铺实体
     * @param globalAverageScore 全平台平均评分
     * @return 贝叶斯评分
     */
    private double calculateBayesianScoreForShop(Shop shop, double globalAverageScore) {
        double R = shop.getScore() != null ? shop.getScore() : 0.0;
        int v = shop.getReviewCount() != null ? shop.getReviewCount() : 0;
        int m = MIN_REVIEW_THRESHOLD;
        double C = globalAverageScore;

        // 贝叶斯公式
        double weightedRating = (v / (double) (v + m)) * R + (m / (double) (v + m)) * C;

        // 归一化到 0~1 范围（假设评分范围是 0~5）
        return Math.min(weightedRating / 5.0, 1.0);
    }

    /**
     * 获取用户偏好标签权重
     * 基于用户收藏和评论过的店铺，统计标签频次并转为权重
     *
     * @param userId 用户ID
     * @return 标签名称 -> 权重（0~1）
     */
    private Map<String, Double> getUserPreferenceWeights(Long userId) {
        // 参数安全校验
        if (userId == null || userId <= 0) {
            return new HashMap<>();
        }

        Map<String, Integer> tagFrequency = new HashMap<>();

        // 1. 获取用户收藏的店铺ID列表
        List<Long> collectedShopIds = collectionRepository.findByUserId(userId)
                .stream()
                .map(c -> c.getShopId())
                .collect(Collectors.toList());

        // 2. 获取用户评论过的店铺ID列表
        List<Long> reviewedShopIds = reviewRepository.findByUserId(userId)
                .stream()
                .map(r -> r.getShopId())
                .collect(Collectors.toList());

        // 3. 合并店铺ID（去重）
        Set<Long> userShopIds = new HashSet<>();
        userShopIds.addAll(collectedShopIds);
        userShopIds.addAll(reviewedShopIds);

        // 4. 如果用户没有行为数据，返回空map（新用户）
        if (userShopIds.isEmpty()) {
            return new HashMap<>();
        }

        // 5. 获取这些店铺的所有标签
        for (Long shopId : userShopIds) {
            List<ShopTag> tags = shopTagRepository.findByShopId(shopId);
            for (ShopTag tag : tags) {
                if (tag.getTagName() != null && !tag.getTagName().isEmpty()) {
                    tagFrequency.merge(tag.getTagName(), 1, Integer::sum);
                }
            }
        }

        // 6. 将频次转为权重（归一化到 0~1）
        if (tagFrequency.isEmpty()) {
            return new HashMap<>();
        }

        int maxFrequency = tagFrequency.values().stream().max(Integer::compare).orElse(1);
        Map<String, Double> weights = new HashMap<>();
        for (Map.Entry<String, Integer> entry : tagFrequency.entrySet()) {
            weights.put(entry.getKey(), entry.getValue() / (double) maxFrequency);
        }

        return weights;
    }

    /**
     * 计算候选店铺的偏好评分
     * 将店铺标签与用户偏好权重进行匹配求和
     *
     * @param shop               候选店铺
     * @param userPreferenceWeights 用户偏好权重
     * @return 偏好评分（0~1）
     */
    private double calculatePreferenceScore(Shop shop, Map<String, Double> userPreferenceWeights) {
        // 如果用户没有偏好（新用户），返回0
        if (userPreferenceWeights.isEmpty()) {
            return 0.0;
        }

        // 获取候选店铺的标签
        List<ShopTag> shopTags = shopTagRepository.findByShopId(shop.getId());
        if (shopTags.isEmpty()) {
            return 0.0;
        }

        // 计算匹配分数
        double matchScore = 0.0;
        for (ShopTag tag : shopTags) {
            String tagName = tag.getTagName();
            if (tagName != null && userPreferenceWeights.containsKey(tagName)) {
                matchScore += userPreferenceWeights.get(tagName);
            }
        }

        // 归一化到 0~1 范围（除以最大可能分数：所有用户偏好的总和）
        double maxPossibleScore = userPreferenceWeights.values().stream().mapToDouble(Double::doubleValue).sum();
        if (maxPossibleScore > 0) {
            return Math.min(matchScore / maxPossibleScore, 1.0);
        }

        return 0.0;
    }

    /**
     * 生成推荐解释
     * 根据三项得分中最高的一项来生成解释文本
     *
     * @param bayesianScore      贝叶斯评分
     * @param distanceScore      距离评分
     * @param preferenceScore    偏好评分
     * @param originalScore      原始评分
     * @param distanceKm         距离（千米）
     * @param userPreferences    用户偏好
     * @param shopId             店铺ID
     * @return 推荐解释文本
     */
    private String generateRecommendReason(double bayesianScore, double distanceScore,
                                           double preferenceScore, Double originalScore,
                                           double distanceKm, Map<String, Double> userPreferences,
                                           Long shopId) {
        // 确定最高得分的项
        String highestFactor;
        if (bayesianScore >= distanceScore && bayesianScore >= preferenceScore) {
            highestFactor = "bayesian";
        } else if (distanceScore >= bayesianScore && distanceScore >= preferenceScore) {
            highestFactor = "distance";
        } else {
            highestFactor = "preference";
        }

        // 根据最高得分的项生成解释
        switch (highestFactor) {
            case "bayesian":
                // 基于评分生成的解释
                if (originalScore != null && originalScore >= 4.5) {
                    return "该店评分极高，口碑优秀";
                } else if (originalScore != null && originalScore >= 4.0) {
                    return "该店评分较高，值得推荐";
                } else if (originalScore != null && originalScore >= 3.5) {
                    return "该店评分不错，是不错的选择";
                } else {
                    return "该店评分较好，符合大众口味";
                }

            case "distance":
                // 基于距离生成的解释
                if (distanceKm != Double.MAX_VALUE && distanceKm <= 1.0) {
                    return String.format("距离您仅%.1fkm，非常方便", distanceKm);
                } else if (distanceKm != Double.MAX_VALUE && distanceKm <= 3.0) {
                    return String.format("距离您仅%.1fkm，交通便利", distanceKm);
                } else if (distanceKm != Double.MAX_VALUE && distanceKm <= 5.0) {
                    return String.format("距离您%.1fkm，还在可接受范围内", distanceKm);
                } else {
                    return "该店位置合理，值得一去";
                }

            case "preference":
                // 基于偏好生成的解释
                if (!userPreferences.isEmpty()) {
                    // 找出用户最偏好的标签
                    String topTag = userPreferences.entrySet().stream()
                            .max(Map.Entry.comparingByValue())
                            .map(Map.Entry::getKey)
                            .orElse("");

                    if (!topTag.isEmpty()) {
                        return String.format("极其符合您对「%s」的偏好", topTag);
                    }
                }
                return "符合您的口味偏好";

            default:
                return "综合推荐";
        }
    }
}
