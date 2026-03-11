package com.foodrecommendation.repository;

import com.foodrecommendation.entity.Collection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface CollectionRepository extends JpaRepository<Collection, Long> {
    List<Collection> findByUserId(Long userId);
    Optional<Collection> findByUserIdAndShopId(Long userId, Long shopId);
    @Modifying
    void deleteByUserIdAndShopId(Long userId, Long shopId);
}
