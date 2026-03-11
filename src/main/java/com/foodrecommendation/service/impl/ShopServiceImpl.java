package com.foodrecommendation.service.impl;

import com.foodrecommendation.entity.Category;
import com.foodrecommendation.entity.Shop;
import com.foodrecommendation.repository.CategoryRepository;
import com.foodrecommendation.repository.CollectionRepository;
import com.foodrecommendation.repository.ShopRepository;
import com.foodrecommendation.repository.ShopTagRepository;
import com.foodrecommendation.service.ShopService;
import com.foodrecommendation.service.TagService;
import com.foodrecommendation.vo.ShopVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 店铺服务实现类
 */
@Service
public class ShopServiceImpl implements ShopService {

    @Autowired
    private ShopRepository shopRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ShopTagRepository shopTagRepository;

    @Autowired
    private CollectionRepository collectionRepository;

    @Autowired
    private TagService tagService;

    @Override
    public List<Shop> getAllShops() {
        return shopRepository.findAll();
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
    public List<Shop> searchShopsByName(String keyword) {
        return shopRepository.findByNameContaining(keyword);
    }

    @Override
    public List<ShopVO> getShopsWithSort(String sort) {
        List<Shop> shops = shopRepository.findAll();

        // 排序
        if ("score".equals(sort)) {
            shops = shops.stream()
                    .sorted((s1, s2) -> Double.compare(s2.getScore() != null ? s2.getScore() : 0,
                            s1.getScore() != null ? s1.getScore() : 0))
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

        // 转换为VO
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

        // 设置用户收藏状态
        if (userId != null) {
            shopVO.setIsCollected(isCollected(userId, shopId));
        } else {
            shopVO.setIsCollected(false);
        }

        return shopVO;
    }

    @Override
    public boolean isCollected(Long userId, Long shopId) {
        return collectionRepository.findByUserIdAndShopId(userId, shopId).isPresent();
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

        // 设置分类名称
        if (shop.getCategoryId() != null) {
            Category category = categoryRepository.findById(shop.getCategoryId()).orElse(null);
            if (category != null) {
                vo.setCategoryName(category.getName());
            }
        }

        // 设置标签列表
        List<String> tags = tagService.getTagsByShopId(shop.getId());
        vo.setTags(tags);

        // 默认未收藏
        vo.setIsCollected(false);

        return vo;
    }
}
