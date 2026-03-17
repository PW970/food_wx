package com.foodrecommendation.service.impl;

import com.foodrecommendation.entity.Category;
import com.foodrecommendation.entity.Shop;
import com.foodrecommendation.integration.TencentMapService;
import com.foodrecommendation.integration.TencentNearbyPlace;
import com.foodrecommendation.repository.CategoryRepository;
import com.foodrecommendation.repository.CollectionRepository;
import com.foodrecommendation.repository.ShopRepository;
import com.foodrecommendation.repository.ShopTagRepository;
import com.foodrecommendation.service.ShopService;
import com.foodrecommendation.service.TagService;
import com.foodrecommendation.utils.GeoUtils;
import com.foodrecommendation.vo.ShopVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 店铺服务实现类
 */
@Service
public class ShopServiceImpl implements ShopService {

    private static final String TENCENT_SOURCE = "TENCENT";
    private static final int DEFAULT_RADIUS_METERS = 5000;
    private static final long CACHE_TTL_MINUTES = 30;

    @Autowired
    private ShopRepository shopRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private CollectionRepository collectionRepository;

    @Autowired
    private TagService tagService;

    @Autowired
    private TencentMapService tencentMapService;

    @Override
    public List<Shop> getAllShops() {
        List<Shop> realShops = getRealShops(null, null, DEFAULT_RADIUS_METERS);
        if (!realShops.isEmpty()) {
            return realShops;
        }
        return shopRepository.findAll();
    }

    @Override
    @Transactional
    public List<Shop> getRealShops(Double lat, Double lng, Integer radiusMeters) {
        List<Shop> realShops = shopRepository.findByDataSource(TENCENT_SOURCE);
        if (!hasFreshTencentCache(realShops)) {
            syncNearbyTencentShops("美食", lat, lng, radiusMeters);
            realShops = shopRepository.findByDataSource(TENCENT_SOURCE);
        }
        return sortByDistance(realShops, lat, lng);
    }

    @Override
    public Shop getShopById(Long id) {
        return shopRepository.findById(id).orElse(null);
    }

    @Override
    public List<Shop> getShopsByCategoryId(Long categoryId) {
        return shopRepository.findByCategoryId(categoryId);
    }

    @Override
    @Transactional
    public List<Shop> getRealShopsByCategoryId(Long categoryId, Double lat, Double lng, Integer radiusMeters) {
        Category category = categoryRepository.findById(categoryId).orElse(null);
        if (category == null) {
            return List.of();
        }

        List<Shop> shops = shopRepository.findByCategoryIdAndDataSource(categoryId, TENCENT_SOURCE);
        if (shops.isEmpty() || !hasFreshTencentCache(shops)) {
            syncNearbyTencentShops(category.getName(), lat, lng, radiusMeters);
            shops = shopRepository.findByCategoryIdAndDataSource(categoryId, TENCENT_SOURCE);
        }
        return sortByDistance(shops, lat, lng);
    }

    @Override
    public List<Shop> searchShopsByName(String keyword) {
        return shopRepository.findByNameContaining(keyword);
    }

    @Override
    @Transactional
    public List<Shop> searchRealShopsByKeyword(String keyword, Double lat, Double lng, Integer radiusMeters) {
        List<Shop> realShops = shopRepository.findByDataSource(TENCENT_SOURCE).stream()
                .filter(shop -> containsIgnoreCase(shop.getName(), keyword)
                        || containsIgnoreCase(shop.getAddress(), keyword)
                        || containsIgnoreCase(shop.getCategory(), keyword))
                .collect(Collectors.toList());
        if (realShops.isEmpty() || !hasFreshTencentCache(realShops)) {
            syncNearbyTencentShops(keyword, lat, lng, radiusMeters);
            realShops = shopRepository.findByDataSource(TENCENT_SOURCE).stream()
                    .filter(shop -> containsIgnoreCase(shop.getName(), keyword)
                            || containsIgnoreCase(shop.getAddress(), keyword)
                            || containsIgnoreCase(shop.getCategory(), keyword))
                    .collect(Collectors.toList());
        }
        return sortByDistance(realShops, lat, lng);
    }

    @Override
    public List<ShopVO> getShopsWithSort(String sort) {
        List<Shop> shops = getAllShops();

        if ("score".equals(sort)) {
            shops = shops.stream()
                    .sorted((s1, s2) -> Double.compare(
                            s2.getScore() != null ? s2.getScore() : 0.0,
                            s1.getScore() != null ? s1.getScore() : 0.0))
                    .collect(Collectors.toList());
        } else if ("perCapita".equals(sort)) {
            shops = shops.stream()
                    .sorted((s1, s2) -> {
                        double p1 = s1.getPerCapita() != null ? s1.getPerCapita().doubleValue() : Double.MAX_VALUE;
                        double p2 = s2.getPerCapita() != null ? s2.getPerCapita().doubleValue() : Double.MAX_VALUE;
                        return Double.compare(p1, p2);
                    })
                    .collect(Collectors.toList());
        }

        return shops.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public ShopVO getShopVOById(Long shopId, Long userId) {
        Shop shop = shopRepository.findById(shopId).orElse(null);
        if (shop == null) {
            return null;
        }

        ShopVO shopVO = convertToVO(shop);
        shopVO.setIsCollected(userId != null && isCollected(userId, shopId));
        return shopVO;
    }

    @Override
    public boolean isCollected(Long userId, Long shopId) {
        return collectionRepository.findByUserIdAndShopId(userId, shopId).isPresent();
    }

    @Transactional
    protected void syncNearbyTencentShops(String keyword, Double lat, Double lng, Integer radiusMeters) {
        if (lat == null || lng == null) {
            return;
        }

        int safeRadius = radiusMeters != null && radiusMeters > 0 ? radiusMeters : DEFAULT_RADIUS_METERS;
        List<TencentNearbyPlace> places = (keyword == null || keyword.isBlank())
                ? tencentMapService.searchNearbyFoodPlaces(lat, lng, safeRadius)
                : tencentMapService.searchFoodPlacesByKeyword(keyword, lat, lng, safeRadius);

        if (places == null || places.isEmpty()) {
            return;
        }

        Map<String, Category> categoriesByName = categoryRepository.findAll().stream()
                .collect(Collectors.toMap(Category::getName, category -> category, (left, right) -> left, HashMap::new));

        for (TencentNearbyPlace place : places) {
            upsertTencentShop(place, categoriesByName);
        }
    }

    private void upsertTencentShop(TencentNearbyPlace place, Map<String, Category> categoriesByName) {
        if (place == null || place.getPoiId() == null || place.getPoiId().isBlank()) {
            return;
        }

        Category category = resolveCategory(place, categoriesByName);
        Shop shop = shopRepository.findByExternalPoiId(place.getPoiId()).orElseGet(Shop::new);

        shop.setExternalPoiId(place.getPoiId());
        shop.setDataSource(TENCENT_SOURCE);
        shop.setName(defaultString(place.getTitle(), "未知店铺"));
        shop.setAddress(defaultString(place.getAddress(), ""));
        shop.setLatitude(place.getLatitude());
        shop.setLongitude(place.getLongitude());
        shop.setCategory(category != null ? category.getName() : fallbackCategory(place));
        shop.setCategoryId(category != null ? category.getId() : null);
        shop.setPhone(defaultString(place.getPhone(), ""));
        shop.setCoverImage(defaultCoverImage(category != null ? category.getName() : fallbackCategory(place)));
        shop.setDescription(buildDescription(place));
        shop.setBusinessHours(defaultString(shop.getBusinessHours(), ""));
        shop.setStatus("OPEN");
        if (shop.getScore() == null || shop.getScore() <= 0) {
            shop.setScore(4.2);
        }
        if (shop.getReviewCount() == null || shop.getReviewCount() < 0) {
            shop.setReviewCount(0);
        }
        shop.setLastSyncedAt(LocalDateTime.now());

        shopRepository.save(shop);
    }

    private Category resolveCategory(TencentNearbyPlace place, Map<String, Category> categoriesByName) {
        String normalized = normalizeText(place.getCategory()) + " " + normalizeText(place.getTitle());
        if (normalized.contains("火锅")) {
            return categoriesByName.get("火锅");
        }
        if (normalized.contains("烧烤") || normalized.contains("烤肉") || normalized.contains("烤串")) {
            return categoriesByName.get("烧烤");
        }
        if (normalized.contains("面") || normalized.contains("拉面") || normalized.contains("粉")) {
            return categoriesByName.get("面馆");
        }
        if (normalized.contains("甜品") || normalized.contains("奶茶") || normalized.contains("咖啡") || normalized.contains("冰淇淋")) {
            return categoriesByName.get("甜品");
        }
        if (normalized.contains("小吃") || normalized.contains("快餐") || normalized.contains("炸鸡")) {
            return categoriesByName.get("小吃");
        }
        return categoriesByName.get("本帮菜");
    }

    private List<Shop> sortByDistance(List<Shop> shops, Double lat, Double lng) {
        if (lat == null || lng == null) {
            return shops;
        }

        return shops.stream()
                .sorted(Comparator.comparingDouble(shop -> GeoUtils.calculateDistance(
                        lat, lng, shop.getLatitude(), shop.getLongitude())))
                .collect(Collectors.toList());
    }

    private boolean hasFreshTencentCache(List<Shop> shops) {
        if (shops == null || shops.isEmpty()) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now();
        return shops.stream()
                .map(Shop::getLastSyncedAt)
                .filter(value -> value != null)
                .anyMatch(value -> ChronoUnit.MINUTES.between(value, now) <= CACHE_TTL_MINUTES);
    }

    /**
     * 将Shop实体转换为ShopVO
     */
    private ShopVO convertToVO(Shop shop) {
        ShopVO vo = new ShopVO();
        vo.setId(shop.getId());
        vo.setName(shop.getName());
        vo.setAddress(shop.getAddress());
        vo.setLatitude(shop.getLatitude());
        vo.setLongitude(shop.getLongitude());
        vo.setCategory(shop.getCategory());
        vo.setScore(shop.getScore());
        vo.setReviewCount(shop.getReviewCount());
        vo.setCoverImage(shop.getCoverImage());
        vo.setPhone(shop.getPhone());
        vo.setBusinessHours(shop.getBusinessHours());
        vo.setPerCapita(shop.getPerCapita());
        vo.setDescription(shop.getDescription());
        vo.setStatus(shop.getStatus());
        vo.setCategoryId(shop.getCategoryId());
        vo.setCreatedAt(shop.getCreatedAt());

        if (shop.getCategoryId() != null) {
            Category category = categoryRepository.findById(shop.getCategoryId()).orElse(null);
            if (category != null) {
                vo.setCategoryName(category.getName());
            }
        }

        List<String> tags = tagService.getTagsByShopId(shop.getId());
        vo.setTags(tags);
        vo.setIsCollected(false);

        return vo;
    }

    private String buildDescription(TencentNearbyPlace place) {
        String category = fallbackCategory(place);
        String address = defaultString(place.getAddress(), "附近");
        return "腾讯地图 POI 同步店铺，分类：" + category + "，地址：" + address;
    }

    private String fallbackCategory(TencentNearbyPlace place) {
        return defaultString(place.getCategory(), "美食");
    }

    private String defaultCoverImage(String categoryName) {
        String safeCategory = categoryName == null || categoryName.isBlank() ? "food" : categoryName;
        return "https://picsum.photos/seed/" + safeCategory + "/600/400";
    }

    private String defaultString(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    private boolean containsIgnoreCase(String source, String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return true;
        }
        if (source == null || source.isBlank()) {
            return false;
        }
        return source.toLowerCase(Locale.ROOT).contains(keyword.trim().toLowerCase(Locale.ROOT));
    }

    private String normalizeText(String text) {
        if (text == null) {
            return "";
        }
        return text.replaceAll("[\\s()（）·\\-_,，。/]", "").toLowerCase(Locale.ROOT);
    }
}
