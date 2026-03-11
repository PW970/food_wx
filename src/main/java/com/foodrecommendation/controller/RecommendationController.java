package com.foodrecommendation.controller;

import com.foodrecommendation.common.Result;
import com.foodrecommendation.entity.Category;
import com.foodrecommendation.entity.Shop;
import com.foodrecommendation.repository.CategoryRepository;
import com.foodrecommendation.repository.ShopRepository;
import com.foodrecommendation.service.RecommendationService;
import com.foodrecommendation.service.TagService;
import com.foodrecommendation.vo.RecommendedShopVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 推荐控制器 - V3 推荐系统API
 * 提供基于混合推荐算法的店铺推荐接口
 */
@RestController
@RequestMapping("/api/recommendations")
@CrossOrigin(origins = "*")
public class RecommendationController {

    @Autowired
    private RecommendationService recommendationService;

    @Autowired
    private TagService tagService;

    @Autowired
    private ShopRepository shopRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    /**
     * 获取推荐店铺列表
     * GET /api/recommendations?userId={userId}&lat={lat}&lng={lng}&limit={limit}
     *
     * @param userId 用户ID（必填）
     * @param lat 用户当前位置纬度（必填）
     * @param lng 用户当前位置经度（必填）
     * @param limit 返回结果数量（非必填，默认10）
     * @return 推荐店铺VO列表
     */
    @GetMapping
    public Result<List<RecommendedShopVO>> getRecommendations(
            @RequestParam Long userId,
            @RequestParam Double lat,
            @RequestParam Double lng,
            @RequestParam(required = false, defaultValue = "10") Integer limit) {

        // 参数校验
        if (userId == null) {
            return Result.error(400, "用户ID不能为空");
        }
        if (lat == null || lng == null) {
            return Result.error(400, "用户位置坐标不能为空");
        }
        if (lat < -90 || lat > 90) {
            return Result.error(400, "纬度范围应为-90到90");
        }
        if (lng < -180 || lng > 180) {
            return Result.error(400, "经度范围应为-180到180");
        }

        // 调用推荐服务获取推荐结果
        List<Map<String, Object>> recommendations = recommendationService.getRecommendations(
                userId, lat, lng, limit
        );

        // 转换为VO
        List<RecommendedShopVO> voList = new ArrayList<>();
        for (Map<String, Object> item : recommendations) {
            RecommendedShopVO vo = new RecommendedShopVO();
            vo.setShopId(((Number) item.get("shopId")).longValue());
            vo.setShopName((String) item.get("shopName"));
            vo.setOriginalScore((Double) item.get("shopScore"));
            vo.setDistance((Double) item.get("distanceKm"));
            vo.setRecommendScore((Double) item.get("recommendScore"));
            vo.setRecommendReason((String) item.get("recommendReason"));

            // 填充额外信息：封面图片、分类名称、标签
            fillShopDetails(vo);

            voList.add(vo);
        }

        return Result.success(voList);
    }

    /**
     * 填充店铺详细信息
     * 根据shopId查询店铺的封面图片、分类名称和标签
     *
     * @param vo 推荐店铺VO
     */
    private void fillShopDetails(RecommendedShopVO vo) {
        Shop shop = shopRepository.findById(vo.getShopId()).orElse(null);
        if (shop == null) {
            return;
        }

        // 设置封面图片
        vo.setCoverImage(shop.getCoverImage());

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
    }
}
