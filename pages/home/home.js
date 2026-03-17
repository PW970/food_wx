// pages/home/home.js - 首页逻辑
const app = getApp();
const request = require('../../utils/request.js');
const config = require('../../utils/config.js');

Page({
  data: {
    // 搜索框
    searchPlaceholder: '搜索美食、店铺...',
    // 推荐列表
    shops: [],
    // 加载状态
    loading: true,
    // 空状态
    isEmpty: false,
    // 错误信息
    errorMsg: '',
    // 用户参数
    userId: null,
    lat: 31.4912,
    lng: 120.3119,
    limit: 10,
    locationReady: false
  },

  onLoad: function (options) {
    this.setData({
      userId: app.getCurrentUserId() || 0
    });
    this.initLocationAndLoad();
  },

  onShow: function () {
    // 页面显示时刷新数据（可选）
  },

  onPullDownRefresh: function () {
    this.initLocationAndLoad().finally(function () {
      wx.stopPullDownRefresh();
    });
  },

  initLocationAndLoad: function () {
    var that = this;
    return this.resolveUserLocation()
      .catch(function () {
        return null;
      })
      .finally(function () {
        that.loadRecommendations();
      });
  },

  resolveUserLocation: function () {
    var that = this;
    return new Promise(function (resolve, reject) {
      wx.getLocation({
        type: 'gcj02',
        success: function (res) {
          var lat = Number(res.latitude);
          var lng = Number(res.longitude);
          if (!lat || !lng) {
            reject(new Error('invalid location'));
            return;
          }

          app.globalData.userLocation = {
            lat: lat,
            lng: lng
          };

          that.setData({
            lat: lat,
            lng: lng,
            locationReady: true
          });
          resolve(res);
        },
        fail: function (err) {
          var fallbackLocation = app.globalData.userLocation || {};
          if (fallbackLocation.lat && fallbackLocation.lng) {
            that.setData({
              lat: fallbackLocation.lat,
              lng: fallbackLocation.lng,
              locationReady: true
            });
            resolve(fallbackLocation);
            return;
          }

          that.setData({
            locationReady: false
          });
          reject(err);
        }
      });
    });
  },

  // 加载推荐数据
  loadRecommendations: function () {
    var that = this;

    that.setData({
      loading: true,
      errorMsg: ''
    });

    // 构建请求参数
    var params = {
      userId: that.data.userId,
      lat: that.data.lat,
      lng: that.data.lng,
      limit: that.data.limit
    };

    // 调用推荐接口
    return request.get(config.API.RECOMMENDATIONS, params)
      .then(function (data) {
        // 处理返回数据
        var shopList = that.processShopData(data);
        that.setData({
          shops: shopList,
          loading: false,
          isEmpty: shopList.length === 0
        });
      })
      .catch(function (err) {
        console.error('加载推荐数据失败:', err);
        that.setData({
          loading: false,
          errorMsg: '加载失败，请稍后重试',
          isEmpty: true
        });
      });
  },

  // 处理店铺数据
  processShopData: function (data) {
    var that = this;
    if (!data || !Array.isArray(data)) {
      return [];
    }

    return data.map(function (shop) {
      return {
        shopId: shop.shopId,
        shopName: shop.shopName || '未知店铺',
        coverImage: shop.coverImage || '',
        categoryName: shop.categoryName || '未分类',
        score: shop.originalScore ? shop.originalScore.toFixed(1) : '0.0',
        distance: typeof shop.distance === 'number' ? shop.distance.toFixed(1) + 'km' : '未知',
        recommendReason: shop.recommendReason || '暂无推荐理由',
        tags: shop.tags || []
      };
    });
  },

  // 点击搜索框（暂时只有UI）
  onSearchTap: function () {
    wx.navigateTo({
      url: '/pages/search/search'
    });
  },

  onMapTap: function () {
    wx.navigateTo({
      url: '/pages/map/map'
    });
  },

  // 点击店铺卡片
  onShopTap: function (e) {
    var shopId = e.currentTarget.dataset.shopId;
    wx.navigateTo({
      url: '/pages/detail/detail?shopId=' + shopId
    });
  },

  // 重新加载
  onRetry: function () {
    this.loadRecommendations();
  }
});
