// app.js - 小程序逻辑入口
const request = require('./utils/request.js');
const config = require('./utils/config.js');

App({
  globalData: {
    userId: null,
    currentUser: null,
    // 用户位置（演示用固定值：苏州大学附近）
    userLocation: {
      lat: 31.4912,
      lng: 120.3119
    }
  },

  onLaunch() {
    console.log('小程序启动');
    this.userReadyPromise = this.restoreCurrentUser();
    this.initCloud();
    this.initLocation();
  },

  initCloud: function () {
    if (!wx.cloud) {
      console.warn('当前基础库不支持云开发');
      return;
    }

    var cloudOptions = {
      traceUser: true
    };

    if (config.CLOUD && config.CLOUD.ENV_ID) {
      cloudOptions.env = config.CLOUD.ENV_ID;
    }

    wx.cloud.init(cloudOptions);
  },

  initLocation: function () {
    var that = this;
    wx.getLocation({
      type: 'gcj02',
      success: function (res) {
        that.globalData.userLocation = {
          lat: res.latitude,
          lng: res.longitude
        };
      }
    });
  },

  restoreCurrentUser: function () {
    var that = this;
    var cachedUserId = wx.getStorageSync('currentUserId');

    if (cachedUserId) {
      return request.get(config.API.USER + '/' + cachedUserId)
        .then(function (user) {
          that.setCurrentUser(user);
          return user;
        })
        .catch(function () {
          that.setCurrentUser(null);
          return null;
        });
    }

    that.setCurrentUser(null);
    return Promise.resolve(null);
  },

  wxLogin: function () {
    var that = this;
    if (!wx.cloud) {
      return Promise.reject(new Error('当前微信环境不支持云开发'));
    }

    return new Promise(function (resolve, reject) {
      wx.cloud.callFunction({
        name: config.CLOUD.LOGIN_FUNCTION,
        data: {
          type: 'getOpenId'
        },
        success: function (res) {
          var result = res && res.result ? res.result : {};
          if (!result.openid) {
            reject(new Error('云函数未返回 openid'));
            return;
          }

          request.post(config.API.OPENID_LOGIN, {
            openid: result.openid,
            nickname: '',
            avatar: ''
          }).then(function (user) {
            that.setCurrentUser(user);
            resolve(user);
          }).catch(reject);
        },
        fail: function (err) {
          reject(err);
        }
      });
    });
  },

  setCurrentUser: function (user) {
    var userId = user && user.id ? Number(user.id) : null;
    this.globalData.userId = userId;
    this.globalData.currentUser = user || null;

    if (userId) {
      wx.setStorageSync('currentUserId', userId);
    } else {
      wx.removeStorageSync('currentUserId');
    }
  },

  ensureUserReady: function () {
    if (!this.userReadyPromise) {
      this.userReadyPromise = this.wxLogin();
    }

    var that = this;
    return this.userReadyPromise
      .then(function (user) {
        if (!user || !user.id) {
          throw new Error('用户初始化失败');
        }
        return user;
      })
      .catch(function (err) {
        that.userReadyPromise = null;
        throw err;
      });
  },

  getCurrentUser: function () {
    return this.globalData.currentUser || null;
  },

  getCurrentUserId: function () {
    return this.globalData.userId || null;
  },

  resetLoginState: function () {
    this.userReadyPromise = null;
    this.setCurrentUser(null);
  }
});
