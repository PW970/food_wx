package com.foodrecommendation.service.impl;

import com.foodrecommendation.entity.User;
import com.foodrecommendation.integration.WechatAuthService;
import com.foodrecommendation.repository.UserRepository;
import com.foodrecommendation.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Optional;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WechatAuthService wechatAuthService;

    @Override
    public User login(String nickname) {
        User user = new User();
        user.setOpenid("mock_openid_" + UUID.randomUUID().toString().substring(0, 8));
        user.setNickname(resolveNickname(nickname));
        user.setAvatar(buildDefaultAvatar(user.getNickname()));
        return userRepository.save(user);
    }

    @Override
    public User wxLogin(String code, String nickname, String avatar) {
        String openid = wechatAuthService.exchangeOpenid(code);
        return saveOrUpdateWechatUser(openid, nickname, avatar);
    }

    @Override
    public User openidLogin(String openid, String nickname, String avatar) {
        if (!StringUtils.hasText(openid)) {
            throw new IllegalArgumentException("缺少微信 openid");
        }
        return saveOrUpdateWechatUser(openid, nickname, avatar);
    }

    private User saveOrUpdateWechatUser(String openid, String nickname, String avatar) {
        Optional<User> existingUser = userRepository.findByOpenid(openid);
        User user = existingUser.orElseGet(User::new);

        user.setOpenid(openid);
        user.setNickname(resolveNickname(nickname));
        user.setAvatar(StringUtils.hasText(avatar) ? avatar : buildDefaultAvatar(user.getNickname()));
        return userRepository.save(user);
    }

    @Override
    public User getUserById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    @Override
    public User updateProfile(Long userId, String nickname, String avatar) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("用户不存在"));

        if (StringUtils.hasText(nickname)) {
            user.setNickname(nickname.trim());
        }
        if (StringUtils.hasText(avatar)) {
            user.setAvatar(avatar.trim());
        }
        return userRepository.save(user);
    }

    private String resolveNickname(String nickname) {
        if (StringUtils.hasText(nickname)) {
            return nickname.trim();
        }
        return "微信用户" + UUID.randomUUID().toString().substring(0, 6);
    }

    private String buildDefaultAvatar(String nickname) {
        return "https://api.dicebear.com/7.x/avataaars/svg?seed=" + nickname;
    }
}
