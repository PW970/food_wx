// pages/detail/detail.js - 店铺详情页
const app = getApp();
const request = require('../../utils/request.js');
const config = require('../../utils/config.js');

Page({
  data: {
    userId: null,
    shopId: null,

    loading: true,
    errorMsg: '',

    shop: null,
    reviews: [],
    reviewsLoading: true,

    // 用于展示评分的 5 颗星
    stars: [1, 2, 3, 4, 5]
  },

  onLoad: function (options) {
    var that = this;
    var shopId = options && options.shopId ? Number(options.shopId) : null;
    if (!shopId) {
      this.setData({
        loading: false,
        errorMsg: '缺少 shopId 参数'
      });
      return;
    }

    this.setData({
      shopId: shopId,
      userId: app.getCurrentUserId()
    });
    this.loadAll();
  },

  onPullDownRefresh: function () {
    var that = this;
    this.loadAll()
      .finally(function () {
        wx.stopPullDownRefresh();
      });
  },

  onShow: function () {
    var refreshShopId = Number(wx.getStorageSync('detailReviewRefreshShopId') || 0);
    if (!refreshShopId || refreshShopId !== Number(this.data.shopId)) {
      return;
    }

    wx.removeStorageSync('detailReviewRefreshShopId');
    this.loadAll();
  },

  loadAll: function () {
    var that = this;
    that.setData({
      loading: true,
      errorMsg: '',
      reviewsLoading: true
    });

    return Promise.all([that.loadShopDetail(), that.loadReviews()])
      .finally(function () {
        that.setData({ loading: false });
      });
  },

  loadShopDetail: function () {
    var that = this;
    var url = config.API.SHOPS + '/' + that.data.shopId;
    return request
      .get(url, { userId: that.data.userId })
      .then(function (data) {
        that.setData({
          shop: that.normalizeShop(data)
        });
      })
      .catch(function (err) {
        console.error('加载店铺详情失败:', err);
        that.setData({
          errorMsg: '加载店铺详情失败，请稍后重试'
        });
      });
  },

  loadReviews: function () {
    var that = this;
    return request
      .get(config.API.REVIEWS, { shopId: that.data.shopId })
      .then(function (data) {
        that.setData({
          reviews: that.normalizeReviews(data),
          reviewsLoading: false
        });
      })
      .catch(function (err) {
        console.error('加载评论失败:', err);
        that.setData({
          reviews: [],
          reviewsLoading: false
        });
      });
  },

  normalizeShop: function (shop) {
    if (!shop) return null;
    return {
      id: shop.id,
      name: shop.name || '未知店铺',
      coverImage: shop.coverImage || '',
      score: typeof shop.score === 'number' ? shop.score.toFixed(1) : (shop.score ? String(shop.score) : '0.0'),
      categoryName: shop.categoryName || shop.category || '未分类',
      address: shop.address || '暂无地址',
      phone: shop.phone || '',
      businessHours: shop.businessHours || '',
      perCapita: shop.perCapita != null && shop.perCapita !== '' ? String(shop.perCapita) : '',
      description: shop.description || '',
      tags: Array.isArray(shop.tags) ? shop.tags : [],
      isCollected: !!shop.isCollected
    };
  },

  normalizeReviews: function (list) {
    var that = this;
    if (!Array.isArray(list)) return [];
    return list.map(function (r) {
      return {
        id: r.id,
        nickname: r.nickname || '匿名用户',
        avatar: r.avatar || 'https://picsum.photos/seed/default-avatar/120',
        rating: r.rating || 0,
        content: r.content || '',
        createdAt: that.formatDateTime(r.createdAt)
      };
    });
  },

  formatDateTime: function (value) {
    if (!value) return '';
    if (Array.isArray(value)) {
      var year = value[0];
      var month = this.padNumber(value[1]);
      var day = this.padNumber(value[2]);
      var hour = this.padNumber(value[3] || 0);
      var minute = this.padNumber(value[4] || 0);
      return year + '-' + month + '-' + day + ' ' + hour + ':' + minute;
    }
    return String(value).replace('T', ' ').slice(0, 16);
  },

  padNumber: function (value) {
    var str = String(value);
    return str.length < 2 ? '0' + str : str;
  },

  onRetry: function () {
    this.loadAll();
  },

  // 收藏/取消收藏
  onToggleCollect: function () {
    var that = this;
    if (!that.data.shop) return;
    if (!that.data.userId) {
      wx.showToast({ title: '用户信息未就绪', icon: 'none' });
      return;
    }

    var isCollected = that.data.shop.isCollected;
    var payload = {
      userId: that.data.userId,
      shopId: that.data.shopId
    };

    if (!isCollected) {
      // 收藏：POST /api/collections
      wx.showLoading({ title: '收藏中...' });
      request
        .post(config.API.COLLECTIONS, payload)
        .then(function () {
          wx.hideLoading();
          wx.showToast({ title: '已收藏', icon: 'success' });
          that.setData({
            'shop.isCollected': true
          });
        })
        .catch(function (err) {
          wx.hideLoading();
          console.error('收藏失败:', err);
        });
    } else {
      // 取消收藏：DELETE /api/collections（传参 {userId, shopId}）
      wx.showLoading({ title: '取消中...' });
      request
        .delete(config.API.COLLECTIONS, payload)
        .then(function () {
          wx.hideLoading();
          wx.showToast({ title: '已取消', icon: 'success' });
          that.setData({
            'shop.isCollected': false
          });
        })
        .catch(function (err) {
          wx.hideLoading();
          console.error('取消收藏失败:', err);
        });
    }
  },

  // 去评论
  onGoReview: function () {
    var shopId = this.data.shopId;
    if (!this.data.userId) {
      wx.showToast({ title: '用户信息未就绪', icon: 'none' });
      return;
    }
    wx.navigateTo({
      url: '/pages/review/review?shopId=' + shopId
    });
  }
});
