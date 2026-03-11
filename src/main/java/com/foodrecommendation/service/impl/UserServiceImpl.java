package com.foodrecommendation.service.impl;

import com.foodrecommendation.entity.User;
import com.foodrecommendation.repository.UserRepository;
import com.foodrecommendation.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public User login(String nickname) {
        User user = new User();
        user.setOpenid("mock_openid_" + UUID.randomUUID().toString().substring(0, 8));
        user.setNickname(nickname);
        user.setAvatar("https://api.dicebear.com/7.x/avataaars/svg?seed=" + nickname);
        return userRepository.save(user);
    }

    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }
}
