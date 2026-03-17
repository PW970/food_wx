package com.foodrecommendation.integration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 腾讯地图 WebService 封装
 * 仅在配置了 key 后启用，用于增强附近餐饮 POI 检索。
 */
@Service
public class TencentMapService {

    private static final int DEFAULT_RADIUS = 5000;
    private static final int DEFAULT_PAGE_SIZE = 20;

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${tencent.map.enabled:false}")
    private boolean enabled;

    @Value("${tencent.map.key:}")
    private String key;

    @Value("${tencent.map.base-url:https://apis.map.qq.com}")
    private String baseUrl;

    public boolean isEnabled() {
        return enabled && key != null && !key.isBlank();
    }

    /**
     * 检索用户附近的餐饮 POI。
     */
    @SuppressWarnings("unchecked")
    public List<TencentNearbyPlace> searchNearbyFoodPlaces(Double lat, Double lng, int radiusMeters) {
        if (!isEnabled() || lat == null || lng == null) {
            return List.of();
        }

        int safeRadius = radiusMeters > 0 ? radiusMeters : DEFAULT_RADIUS;
        String url = UriComponentsBuilder
                .fromHttpUrl(baseUrl + "/ws/place/v1/search")
                .queryParam("key", key)
                .queryParam("keyword", "美食")
                .queryParam("orderby", "_distance")
                .queryParam("page_size", DEFAULT_PAGE_SIZE)
                .queryParam("boundary", String.format("nearby(%s,%s,%d)", lat, lng, safeRadius))
                .encode()
                .build()
                .toUriString();

        try {
            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);
            Map<String, Object> body = response.getBody();
            if (body == null) {
                return List.of();
            }

            Object status = body.get("status");
            if (!(status instanceof Number) || ((Number) status).intValue() != 0) {
                return List.of();
            }

            Object data = body.get("data");
            if (!(data instanceof List<?> items)) {
                return List.of();
            }

            List<TencentNearbyPlace> places = new ArrayList<>();
            for (Object item : items) {
                if (!(item instanceof Map<?, ?> map)) {
                    continue;
                }

                String title = asString(map.get("title"));
                String address = asString(map.get("address"));
                String category = asString(map.get("category"));
                Double distance = asDouble(map.get("_distance"));

                if (title == null || title.isBlank()) {
                    continue;
                }

                places.add(new TencentNearbyPlace(title, address, category, distance));
            }
            return places;
        } catch (Exception ex) {
            return List.of();
        }
    }

    private String asString(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private Double asDouble(Object value) {
        if (value instanceof Number number) {
            return number.doubleValue();
        }
        if (value == null) {
            return null;
        }
        try {
            return Double.parseDouble(String.valueOf(value));
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
