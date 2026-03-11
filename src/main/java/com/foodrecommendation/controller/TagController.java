package com.foodrecommendation.controller;

import com.foodrecommendation.common.Result;
import com.foodrecommendation.entity.ShopTag;
import com.foodrecommendation.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * 标签控制器
 * 提供店铺标签相关的API接口
 */
@RestController
@RequestMapping("/api/tags")
@CrossOrigin(origins = "*")
public class TagController {

    @Autowired
    private TagService tagService;

    /**
     * 根据店铺ID获取标签列表
     * GET /api/tags/shop/{shopId}
     */
    @GetMapping("/shop/{shopId}")
    public Result<List<String>> getTagsByShopId(@PathVariable Long shopId) {
        List<String> tags = tagService.getTagsByShopId(shopId);
        return Result.success(tags);
    }
}
