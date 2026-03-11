package com.foodrecommendation.service;

import com.foodrecommendation.entity.Category;
import com.foodrecommendation.entity.Shop;
import com.foodrecommendation.vo.ShopVO;
import java.util.List;

/**
 * 店铺服务接口
 */
public interface ShopService {

    /**
     * 获取所有店铺列表
     * @return 店铺列表
     */
    List<Shop> getAllShops();

    /**
     * 根据ID获取店铺
     * @param id 店铺ID
     * @return 店铺
     */
    Shop getShopById(Long id);

    /**
     * 根据分类ID获取店铺列表
     * @param categoryId 分类ID
     * @return 店铺列表
     */
    List<Shop> getShopsByCategoryId(Long categoryId);

    /**
     * 根据关键词搜索店铺（模糊查询）
     * @param keyword 关键词
     * @return 店铺列表
     */
    List<Shop> searchShopsByName(String keyword);

    /**
     * 获取店铺VO列表（支持排序）
     * @param sort 排序方式：score(评分)、perCapita(人均)
     * @return 店铺VO列表
     */
    List<ShopVO> getShopsWithSort(String sort);

    /**
     * 获取店铺详情VO（包含标签、收藏状态等）
     * @param shopId 店铺ID
     * @param userId 用户ID（可选，用于判断收藏状态）
     * @return 店铺VO
     */
    ShopVO getShopVOById(Long shopId, Long userId);

    /**
     * 检查用户是否收藏了指定店铺
     * @param userId 用户ID
     * @param shopId 店铺ID
     * @return 是否收藏
     */
    boolean isCollected(Long userId, Long shopId);
}
