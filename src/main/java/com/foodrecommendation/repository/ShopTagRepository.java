package com.foodrecommendation.repository;

import com.foodrecommendation.entity.ShopTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface ShopTagRepository extends JpaRepository<ShopTag, Long> {
    
    List<ShopTag> findByShopId(Long shopId);
}
