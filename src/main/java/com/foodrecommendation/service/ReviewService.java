package com.foodrecommendation.service;

import com.foodrecommendation.dto.ReviewRequest;
import com.foodrecommendation.entity.Review;
import com.foodrecommendation.vo.ReviewVO;
import java.util.List;

/**
 * 评论服务接口
 */
public interface ReviewService {

    /**
     * 根据店铺ID获取评论列表
     * @param shopId 店铺ID
     * @return 评论列表
     */
    List<Review> getReviewsByShopId(Long shopId);

    /**
     * 根据店铺ID获取评论VO列表（带用户信息）
     * @param shopId 店铺ID
     * @return 评论VO列表
     */
    List<ReviewVO> getReviewVOsByShopId(Long shopId);

    /**
     * 根据用户ID获取评论列表
     * @param userId 用户ID
     * @return 评论VO列表
     */
    List<ReviewVO> getReviewVOsByUserId(Long userId);

    /**
     * 添加评论
     * @param request 评论请求
     * @return 评论
     */
    Review addReview(ReviewRequest request);

    /**
     * 删除评论
     * @param id 评论ID
     */
    void deleteReview(Long id);
}
