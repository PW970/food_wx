package com.foodrecommendation.service;

import com.foodrecommendation.entity.Category;
import java.util.List;

/**
 * 分类服务接口
 */
public interface CategoryService {

    /**
     * 获取所有分类列表
     * @return 分类列表
     */
    List<Category> getAllCategories();
}
