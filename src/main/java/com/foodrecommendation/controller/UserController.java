package com.foodrecommendation.controller;

import com.foodrecommendation.common.Result;
import com.foodrecommendation.dto.LoginRequest;
import com.foodrecommendation.dto.OpenidLoginRequest;
import com.foodrecommendation.dto.UpdateUserProfileRequest;
import com.foodrecommendation.dto.WxLoginRequest;
import com.foodrecommendation.entity.User;
import com.foodrecommendation.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/user")
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/login")
    public Result<User> login(@RequestBody LoginRequest request) {
        User user = userService.login(request.getNickname());
        return Result.success(user);
    }

    @PostMapping("/wx-login")
    public Result<User> wxLogin(@RequestBody WxLoginRequest request) {
        try {
            User user = userService.wxLogin(request.getCode(), request.getNickname(), request.getAvatar());
            return Result.success(user);
        } catch (IllegalArgumentException e) {
            return Result.error(400, e.getMessage());
        } catch (IllegalStateException e) {
            return Result.error(500, e.getMessage());
        }
    }

    @PostMapping("/openid-login")
    public Result<User> openidLogin(@RequestBody OpenidLoginRequest request) {
        try {
            User user = userService.openidLogin(request.getOpenid(), request.getNickname(), request.getAvatar());
            return Result.success(user);
        } catch (IllegalArgumentException e) {
            return Result.error(400, e.getMessage());
        }
    }

    @PutMapping("/profile")
    public Result<User> updateProfile(@RequestBody UpdateUserProfileRequest request) {
        try {
            User user = userService.updateProfile(request.getUserId(), request.getNickname(), request.getAvatar());
            return Result.success(user);
        } catch (IllegalArgumentException e) {
            return Result.error(400, e.getMessage());
        }
    }

    @PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<Map<String, String>> uploadAvatar(@RequestPart("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return Result.error(400, "头像文件不能为空");
        }

        try {
            String originalName = file.getOriginalFilename();
            String suffix = ".png";
            if (originalName != null && originalName.lastIndexOf('.') >= 0) {
                suffix = originalName.substring(originalName.lastIndexOf('.'));
            }

            Path uploadDir = Paths.get("uploads", "avatars");
            Files.createDirectories(uploadDir);

            String filename = UUID.randomUUID() + suffix;
            Path target = uploadDir.resolve(filename);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

            String url = ServletUriComponentsBuilder.fromCurrentContextPath()
                    .path("/uploads/avatars/")
                    .path(filename)
                    .toUriString();

            return Result.success(Map.of("url", url));
        } catch (IOException e) {
            return Result.error(500, "头像上传失败");
        }
    }

    @GetMapping("/{id}")
    public Result<User> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        if (user == null) {
            return Result.error(404, "用户不存在");
        }
        return Result.success(user);
    }
}
