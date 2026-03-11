package com.foodrecommendation.controller;

import com.foodrecommendation.common.Result;
import com.foodrecommendation.dto.CollectionRequest;
import com.foodrecommendation.entity.Collection;
import com.foodrecommendation.service.CollectionService;
import com.foodrecommendation.vo.ShopVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * 收藏控制器
 * 提供收藏相关的API接口
 */
@RestController
@RequestMapping("/api/collections")
@CrossOrigin(origins = "*")
public class CollectionController {

    @Autowired
    private CollectionService collectionService;

    /**
     * 根据用户ID获取收藏列表（返回店铺VO列表）
     * GET /api/collections?userId=1
     * @param userId 用户ID
     * @return 店铺VO列表
     */
    @GetMapping
    public Result<List<ShopVO>> getCollectionsByUserId(@RequestParam Long userId) {
        List<ShopVO> shops = collectionService.getShopVOsByUserId(userId);
        return Result.success(shops);
    }

    /**
     * 添加收藏
     * POST /api/collections
     * @param request 收藏请求
     * @return 收藏记录
     */
    @PostMapping
    public Result<Collection> addCollection(@RequestBody CollectionRequest request) {
        Collection collection = collectionService.addCollection(request);
        return Result.success(collection);
    }

    /**
     * 取消收藏
     * DELETE /api/collections?userId=1&shopId=1
     * @param userId 用户ID
     * @param shopId 店铺ID
     * @return 空结果
     */
    @DeleteMapping
    public Result<Void> deleteCollection(@RequestParam Long userId, @RequestParam Long shopId) {
        collectionService.deleteCollection(userId, shopId);
        return Result.success();
    }
}
