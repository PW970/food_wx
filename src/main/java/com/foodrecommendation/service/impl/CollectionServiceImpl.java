package com.foodrecommendation.service.impl;

import com.foodrecommendation.dto.CollectionRequest;
import com.foodrecommendation.entity.Category;
import com.foodrecommendation.entity.Collection;
import com.foodrecommendation.entity.Shop;
import com.foodrecommendation.repository.CategoryRepository;
import com.foodrecommendation.repository.CollectionRepository;
import com.foodrecommendation.repository.UserRepository;
import com.foodrecommendation.repository.ShopRepository;
import com.foodrecommendation.service.CollectionService;
import com.foodrecommendation.service.TagService;
import com.foodrecommendation.vo.ShopVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 收藏服务实现类
 */
@Service
public class CollectionServiceImpl implements CollectionService {

    @Autowired
    private CollectionRepository collectionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ShopRepository shopRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TagService tagService;

    @Override
    @Transactional
    public Collection addCollection(CollectionRequest request) {
        // 校验是否已收藏
        if (request.getUserId() == null || request.getShopId() == null) {
            throw new IllegalArgumentException("用户ID和店铺ID不能为空");
        }

        // 校验用户存在
        if (!userRepository.existsById(request.getUserId())) {
            throw new IllegalArgumentException("用户不存在");
        }

        // 校验店铺存在
        if (!shopRepository.existsById(request.getShopId())) {
            throw new IllegalArgumentException("店铺不存在");
        }

        boolean exists = collectionRepository.findByUserIdAndShopId(
            request.getUserId(),
            request.getShopId()
        ).isPresent();

        if (exists) {
            throw new IllegalArgumentException("该用户已收藏此店铺");
        }

        Collection collection = new Collection();
        collection.setUserId(request.getUserId());
        collection.setShopId(request.getShopId());
        return collectionRepository.save(collection);
    }

    @Override
    @Transactional
    public void deleteCollection(Long userId, Long shopId) {
        collectionRepository.deleteByUserIdAndShopId(userId, shopId);
    }

    @Override
    public List<ShopVO> getShopVOsByUserId(Long userId) {
        List<Collection> collections = collectionRepository.findByUserId(userId);

        return collections.stream()
                .map(collection -> {
                    Shop shop = shopRepository.findById(collection.getShopId()).orElse(null);
                    if (shop == null) {
                        return null;
                    }
                    return convertToVO(shop, true);
                })
                .filter(vo -> vo != null)
                .collect(Collectors.toList());
    }

    @Override
    public boolean isCollected(Long userId, Long shopId) {
        return collectionRepository.findByUserIdAndShopId(userId, shopId).isPresent();
    }

    /**
     * 将Shop实体转换为ShopVO
     */
    private ShopVO convertToVO(Shop shop, boolean isCollected) {
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
        vo.setIsCollected(isCollected);

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

        return vo;
    }
}
