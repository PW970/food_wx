package com.foodrecommendation.controller;

import com.foodrecommendation.common.Result;
import com.foodrecommendation.entity.Shop;
import com.foodrecommendation.service.ShopService;
import com.foodrecommendation.vo.ShopVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * 店铺控制器
 * 提供店铺相关的API接口
 */
@RestController
@RequestMapping("/api/shops")
@CrossOrigin(origins = "*")
public class ShopController {

    @Autowired
    private ShopService shopService;

    /**
     * 获取所有店铺列表（支持排序）
     * GET /api/shops?sort=score 或 /api/shops?sort=perCapita
     * @param sort 排序方式：score(评分)、perCapita(人均)
     * @return 店铺VO列表
     */
    @GetMapping
    public Result<List<ShopVO>> getAllShops(@RequestParam(required = false) String sort) {
        List<ShopVO> shops;
        if (sort != null && !sort.isEmpty()) {
            shops = shopService.getShopsWithSort(sort);
        } else {
            List<Shop> shopList = shopService.getAllShops();
            shops = shopList.stream()
                    .map(shop -> shopService.getShopVOById(shop.getId(), null))
                    .collect(java.util.stream.Collectors.toList());
        }
        return Result.success(shops);
    }

    /**
     * 根据分类ID获取店铺列表
     * GET /api/shops/category/{categoryId}
     * @param categoryId 分类ID
     * @return 店铺VO列表
     */
    @GetMapping("/category/{categoryId}")
    public Result<List<ShopVO>> getShopsByCategoryId(@PathVariable Long categoryId) {
        List<Shop> shops = shopService.getShopsByCategoryId(categoryId);
        List<ShopVO> shopVOs = shops.stream()
                .map(shop -> shopService.getShopVOById(shop.getId(), null))
                .collect(java.util.stream.Collectors.toList());
        return Result.success(shopVOs);
    }

    /**
     * 搜索店铺（按名称模糊查询）
     * GET /api/shops/search?keyword=xxx
     * @param keyword 关键词
     * @return 店铺VO列表
     */
    @GetMapping("/search")
    public Result<List<ShopVO>> searchShops(@RequestParam(required = false, defaultValue = "") String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return Result.success(List.of());
        }
        List<Shop> shops = shopService.searchShopsByName(keyword);
        List<ShopVO> shopVOs = shops.stream()
                .map(shop -> shopService.getShopVOById(shop.getId(), null))
                .collect(java.util.stream.Collectors.toList());
        return Result.success(shopVOs);
    }

    /**
     * 获取店铺详情
     * GET /api/shops/{id}?userId={userId}
     * @param id 店铺ID
     * @param userId 用户ID（可选，用于判断收藏状态）
     * @return 店铺VO
     */
    @GetMapping("/{id}")
    public Result<ShopVO> getShopById(@PathVariable Long id,
                                       @RequestParam(required = false) Long userId) {
        ShopVO shopVO = shopService.getShopVOById(id, userId);
        if (shopVO == null) {
            return Result.error(404, "店铺不存在");
        }
        return Result.success(shopVO);
    }
}
