package com.foodrecommendation.service;

import com.foodrecommendation.dto.CollectionRequest;
import com.foodrecommendation.entity.Collection;
import com.foodrecommendation.vo.ShopVO;
import java.util.List;

/**
 * 收藏服务接口
 */
public interface CollectionService {

    /**
     * 添加收藏
     * @param request 收藏请求
     * @return 收藏记录
     */
    Collection addCollection(CollectionRequest request);

    /**
     * 取消收藏
     * @param userId 用户ID
     * @param shopId 店铺ID
     */
    void deleteCollection(Long userId, Long shopId);

    /**
     * 根据用户ID获取收藏列表（返回店铺VO列表）
     * @param userId 用户ID
     * @return 店铺VO列表
     */
    List<ShopVO> getShopVOsByUserId(Long userId);

    /**
     * 检查用户是否收藏了指定店铺
     * @param userId 用户ID
     * @param shopId 店铺ID
     * @return 是否收藏
     */
    boolean isCollected(Long userId, Long shopId);
}
