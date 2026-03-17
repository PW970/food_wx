// pages/profile/profile.js - 我的页面（用户中心）
const app = getApp();
const request = require('../../utils/request.js');
const config = require('../../utils/config.js');

Page({
  data: {
    userId: '',
    nickname: '加载中...',
    avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=food-user',
    loginReady: false,
    loginError: '',
    showProfileAuth: false,
    draftNickname: '',
    draftAvatar: '',
    profileSubmitting: false
  },

  onLoad: function () {
    this.syncUser();
  },

  onShow: function () {
    this.syncUser();
  },

  syncUser: function () {
    var user = app.getCurrentUser();
    var needAuth = this.needsProfileAuth(user);

    if (!user || !user.id) {
      this.setData({
        userId: '',
        nickname: '未登录用户',
        avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=food-user',
        loginReady: false,
        loginError: '',
        showProfileAuth: false,
        draftNickname: '',
        draftAvatar: ''
      });
      return;
    }

    this.setData({
      userId: user.id || '',
      nickname: user.nickname || '微信用户',
      avatar: user.avatar || 'https://api.dicebear.com/7.x/avataaars/svg?seed=food-user',
      loginReady: true,
      loginError: '',
      showProfileAuth: needAuth,
      draftNickname: needAuth ? '' : (user.nickname || ''),
      draftAvatar: ''
    });
  },

  needsProfileAuth: function (user) {
    if (!user) return false;
    var nickname = user.nickname || '';
    var avatar = user.avatar || '';
    return nickname.indexOf('微信用户') === 0 || avatar.indexOf('dicebear') >= 0;
  },

  onWechatLoginTap: function () {
    var that = this;
    app.resetLoginState();
    wx.showLoading({ title: '登录中...' });
    app.ensureUserReady()
      .then(function () {
        wx.hideLoading();
        wx.showToast({
          title: '微信登录成功',
          icon: 'success'
        });
        that.syncUser();
      })
      .catch(function (err) {
        wx.hideLoading();
        console.error('微信登录失败:', err);
        wx.showToast({
          title: '微信登录失败',
          icon: 'none'
        });
        that.setData({
          loginReady: false,
          loginError: '微信登录失败，请先在开发者工具开通并部署云开发环境'
        });
      });
  },

  onChooseAvatar: function (e) {
    this.setData({
      draftAvatar: e.detail.avatarUrl || ''
    });
  },

  onNicknameInput: function (e) {
    this.setData({
      draftNickname: e.detail.value || ''
    });
  },

  onSubmitWechatProfile: function () {
    var that = this;
    var userId = Number(that.data.userId || 0);
    var nickname = (that.data.draftNickname || '').trim();
    var avatar = that.data.draftAvatar || '';

    if (!userId) {
      wx.showToast({ title: '请先完成微信登录', icon: 'none' });
      return;
    }
    if (!nickname) {
      wx.showToast({ title: '请填写微信昵称', icon: 'none' });
      return;
    }
    if (!avatar) {
      wx.showToast({ title: '请选择微信头像', icon: 'none' });
      return;
    }

    that.setData({ profileSubmitting: true });
    wx.showLoading({ title: '授权中...' });

    that.uploadAvatarIfNeeded(avatar)
      .then(function (avatarUrl) {
        return request.put(config.API.USER_PROFILE, {
          userId: userId,
          nickname: nickname,
          avatar: avatarUrl
        });
      })
      .then(function (user) {
        app.setCurrentUser(user);
        wx.hideLoading();
        wx.showToast({
          title: '授权成功',
          icon: 'success'
        });
        that.syncUser();
      })
      .catch(function (err) {
        wx.hideLoading();
        console.error('更新微信资料失败:', err);
        wx.showToast({
          title: '授权失败',
          icon: 'none'
        });
      })
      .finally(function () {
        that.setData({ profileSubmitting: false });
      });
  },

  uploadAvatarIfNeeded: function (avatarPath) {
    if (!avatarPath || avatarPath.indexOf('wxfile://') !== 0) {
      return Promise.resolve(avatarPath);
    }

    return new Promise(function (resolve, reject) {
      wx.uploadFile({
        url: config.BASE_URL + config.API.USER_AVATAR,
        filePath: avatarPath,
        name: 'file',
        success: function (res) {
          try {
            var data = JSON.parse(res.data || '{}');
            if ((data.code === 200 || data.code === 0) && data.data && data.data.url) {
              resolve(data.data.url);
              return;
            }
            reject(data);
          } catch (err) {
            reject(err);
          }
        },
        fail: function (err) {
          reject(err);
        }
      });
    });
  },

  onFavoritesTap: function () {
    if (!this.data.loginReady) {
      wx.showToast({ title: '请先完成微信登录', icon: 'none' });
      return;
    }
    wx.navigateTo({
      url: '/pages/favorites/favorites'
    });
  },

  onMyReviewsTap: function () {
    if (!this.data.loginReady) {
      wx.showToast({ title: '请先完成微信登录', icon: 'none' });
      return;
    }
    wx.navigateTo({
      url: '/pages/myreviews/myreviews'
    });
  }
});
