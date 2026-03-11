package com.foodrecommendation.controller;

import com.foodrecommendation.common.Result;
import com.foodrecommendation.dto.ReviewRequest;
import com.foodrecommendation.entity.Review;
import com.foodrecommendation.service.ReviewService;
import com.foodrecommendation.vo.ReviewVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

/**
 * 评论控制器
 * 提供评论相关的API接口
 */
@RestController
@RequestMapping("/api/reviews")
@CrossOrigin(origins = "*")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    /**
     * 根据店铺ID获取评论列表（返回VO，包含用户信息）
     * GET /api/reviews?shopId=1
     * @param shopId 店铺ID
     * @return 评论VO列表
     */
    @GetMapping
    public Result<List<ReviewVO>> getReviewsByShopId(@RequestParam Long shopId) {
        List<ReviewVO> reviews = reviewService.getReviewVOsByShopId(shopId);
        return Result.success(reviews);
    }

    /**
     * 根据用户ID获取评论列表（我的评论）
     * GET /api/reviews/user/{userId}
     * @param userId 用户ID
     * @return 评论VO列表
     */
    @GetMapping("/user/{userId}")
    public Result<List<ReviewVO>> getReviewsByUserId(@PathVariable Long userId) {
        List<ReviewVO> reviews = reviewService.getReviewVOsByUserId(userId);
        return Result.success(reviews);
    }

    /**
     * 添加评论
     * POST /api/reviews
     * @param request 评论请求
     * @return 评论
     */
    @PostMapping
    public Result<Review> addReview(@RequestBody ReviewRequest request) {
        Review review = reviewService.addReview(request);
        return Result.success(review);
    }

    /**
     * 删除评论
     * DELETE /api/reviews/{id}
     * @param id 评论ID
     * @return 空结果
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteReview(@PathVariable Long id) {
        reviewService.deleteReview(id);
        return Result.success();
    }
}
