package com.foodrecommendation.service;

import com.foodrecommendation.entity.User;

public interface UserService {
    User login(String nickname);
    User wxLogin(String code, String nickname, String avatar);
    User openidLogin(String openid, String nickname, String avatar);
    User getUserById(Long id);
    User updateProfile(Long userId, String nickname, String avatar);
}
