package com.foodrecommendation.utils;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 地理工具类 - 用于计算两点之间的直线距离
 * 使用 Haversine 公式计算地球表面两点之间的球面距离
 */
public class GeoUtils {

    /**
     * 地球平均半径（单位：千米）
     */
    private static final double EARTH_RADIUS_KM = 6371.0;

    /**
     * 使用 Haversine 公式计算两个经纬度坐标之间的球面距离
     *
     * @param lat1 第一个点的纬度
     * @param lon1 第一个点的经度
     * @param lat2 第二个点的纬度
     * @param lon2 第二个点的经度
     * @return 两点之间的距离（单位：千米），保留2位小数
     */
    public static double calculateDistance(Double lat1, Double lon1, Double lat2, Double lon2) {
        // 如果任一坐标为空，返回最大距离（不影响推荐结果）
        if (lat1 == null || lon1 == null || lat2 == null || lon2 == null) {
            return Double.MAX_VALUE;
        }

        // 将角度转换为弧度
        double lat1Rad = Math.toRadians(lat1);
        double lon1Rad = Math.toRadians(lon1);
        double lat2Rad = Math.toRadians(lat2);
        double lon2Rad = Math.toRadians(lon2);

        // Haversine 公式计算球面距离
        double dLat = lat2Rad - lat1Rad;
        double dLon = lon2Rad - lon1Rad;

        // 计算 a 值
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(lat1Rad) * Math.cos(lat2Rad)
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);

        // 计算 c 值
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        // 计算距离（单位：千米）
        double distance = EARTH_RADIUS_KM * c;

        // 保留2位小数
        return BigDecimal.valueOf(distance)
                .setScale(2, RoundingMode.HALF_UP)
                .doubleValue();
    }

    /**
     * 计算距离分数
     * 公式：distanceScore = 1 / (1 + distance_km)
     * 距离越近，分数越高，范围 0~1
     *
     * @param distanceKm 距离（千米）
     * @return 距离分数
     */
    public static double calculateDistanceScore(double distanceKm) {
        // 如果距离无效（最大距离），返回0分
        if (distanceKm == Double.MAX_VALUE || distanceKm < 0) {
            return 0.0;
        }
        return 1.0 / (1.0 + distanceKm);
    }
}
