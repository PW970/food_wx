package com.foodrecommendation.service.impl;

import com.foodrecommendation.entity.ShopTag;
import com.foodrecommendation.repository.ShopTagRepository;
import com.foodrecommendation.service.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 店铺标签服务实现类
 */
@Service
public class TagServiceImpl implements TagService {

    @Autowired
    private ShopTagRepository shopTagRepository;

    @Override
    public List<String> getTagsByShopId(Long shopId) {
        return shopTagRepository.findByShopId(shopId)
                .stream()
                .map(ShopTag::getTagName)
                .collect(Collectors.toList());
    }

    @Override
    public List<ShopTag> getShopTagsByShopId(Long shopId) {
        return shopTagRepository.findByShopId(shopId);
    }
}
