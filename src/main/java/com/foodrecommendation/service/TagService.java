package com.foodrecommendation.service;

import com.foodrecommendation.entity.ShopTag;
import java.util.List;

/**
 * 店铺标签服务接口
 */
public interface TagService {

    /**
     * 根据店铺ID获取标签列表
     * @param shopId 店铺ID
     * @return 标签列表
     */
    List<String> getTagsByShopId(Long shopId);

    /**
     * 根据店铺ID获取标签实体列表
     * @param shopId 店铺ID
     * @return 标签实体列表
     */
    List<ShopTag> getShopTagsByShopId(Long shopId);
}
