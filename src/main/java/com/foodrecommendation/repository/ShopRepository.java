package com.foodrecommendation.repository;

import com.foodrecommendation.entity.Shop;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface ShopRepository extends JpaRepository<Shop, Long> {

    List<Shop> findByCategoryId(Long categoryId);

    List<Shop> findByNameContaining(String name);

    List<Shop> findByDataSource(String dataSource);

    List<Shop> findByCategoryIdAndDataSource(Long categoryId, String dataSource);

    Optional<Shop> findByExternalPoiId(String externalPoiId);
}
