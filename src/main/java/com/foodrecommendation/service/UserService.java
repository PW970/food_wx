package com.foodrecommendation.service;

import com.foodrecommendation.entity.User;

public interface UserService {
    User login(String nickname);
    User getUserById(Long id);
}
