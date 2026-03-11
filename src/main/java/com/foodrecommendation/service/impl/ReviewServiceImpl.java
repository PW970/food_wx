package com.foodrecommendation.service.impl;

import com.foodrecommendation.dto.ReviewRequest;
import com.foodrecommendation.entity.Review;
import com.foodrecommendation.entity.User;
import com.foodrecommendation.repository.ReviewRepository;
import com.foodrecommendation.repository.ShopRepository;
import com.foodrecommendation.repository.UserRepository;
import com.foodrecommendation.service.ReviewService;
import com.foodrecommendation.vo.ReviewVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 评论服务实现类
 */
@Service
public class ReviewServiceImpl implements ReviewService {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private ShopRepository shopRepository;

    @Autowired
    private UserRepository userRepository;

    @Override
    public List<Review> getReviewsByShopId(Long shopId) {
        return reviewRepository.findByShopId(shopId);
    }

    @Override
    public List<ReviewVO> getReviewVOsByShopId(Long shopId) {
        List<Review> reviews = reviewRepository.findByShopId(shopId);
        return reviews.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<ReviewVO> getReviewVOsByUserId(Long userId) {
        List<Review> reviews = reviewRepository.findByUserId(userId);
        return reviews.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public Review addReview(ReviewRequest request) {
        // 校验评分范围
        if (request.getRating() == null || request.getRating() < 1 || request.getRating() > 5) {
            throw new IllegalArgumentException("评分必须在 1-5 之间");
        }

        // 校验必填字段
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

        // 创建评论
        Review review = new Review();
        review.setUserId(request.getUserId());
        review.setShopId(request.getShopId());
        review.setRating(request.getRating());
        review.setContent(request.getContent());
        Review savedReview = reviewRepository.save(review);

        // 同步更新店铺评分和评论数
        updateShopStatistics(request.getShopId());

        return savedReview;
    }

    @Override
    @Transactional
    public void deleteReview(Long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("评论不存在"));

        Long shopId = review.getShopId();
        reviewRepository.deleteById(id);

        // 同步更新店铺评分和评论数
        updateShopStatistics(shopId);
    }

    /**
     * 将Review实体转换为ReviewVO
     */
    private ReviewVO convertToVO(Review review) {
        ReviewVO vo = new ReviewVO();
        vo.setId(review.getId());
        vo.setUserId(review.getUserId());
        vo.setShopId(review.getShopId());
        vo.setRating(review.getRating());
        vo.setContent(review.getContent());
        vo.setCreatedAt(review.getCreatedAt());

        // 查询用户信息
        if (review.getUserId() != null) {
            User user = userRepository.findById(review.getUserId()).orElse(null);
            if (user != null) {
                vo.setNickname(user.getNickname());
                vo.setAvatar(user.getAvatar());
            }
        }

        return vo;
    }

    private void updateShopStatistics(Long shopId) {
        List<Review> reviews = reviewRepository.findByShopId(shopId);

        final double avgRating;
        if (!reviews.isEmpty()) {
            avgRating = reviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);
        } else {
            avgRating = 0.0;
        }

        shopRepository.findById(shopId).ifPresent(shop -> {
            shop.setScore(Math.round(avgRating * 10) / 10.0);
            shop.setReviewCount(reviews.size());
            shopRepository.save(shop);
        });
    }
}
