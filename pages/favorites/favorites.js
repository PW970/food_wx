// pages/favorites/favorites.js - 我的收藏
const app = getApp();
const request = require('../../utils/request.js');
const config = require('../../utils/config.js');

Page({
  data: {
    userId: null,
    loading: true,
    errorMsg: '',
    shops: []
  },

  onLoad: function () {
    this.ensureUserAndLoad();
  },

  onShow: function () {
    this.ensureUserAndLoad();
  },

  ensureUserAndLoad: function () {
    var userId = app.getCurrentUserId();
    if (!userId) {
      this.setData({
        userId: null,
        loading: false,
        errorMsg: '请先在“我的”页面完成微信登录',
        shops: []
      });
      return Promise.resolve();
    }

    this.setData({
      userId: userId
    });
    return this.loadFavorites();
  },

  loadFavorites: function () {
    var that = this;
    if (!that.data.userId) {
      that.setData({
        loading: false,
        errorMsg: '用户信息加载中，请稍后重试',
        shops: []
      });
      return Promise.reject(new Error('missing userId'));
    }

    that.setData({
      loading: true,
      errorMsg: ''
    });

    request
      .get(config.API.COLLECTIONS, { userId: that.data.userId })
      .then(function (data) {
        that.setData({
          shops: that.normalizeShops(data),
          loading: false
        });
      })
      .catch(function (err) {
        console.error('加载收藏失败:', err);
        that.setData({
          loading: false,
          errorMsg: '加载收藏失败，请稍后重试',
          shops: []
        });
      });
  },

  normalizeShops: function (list) {
    if (!Array.isArray(list)) return [];
    return list.map(function (shop) {
      return {
        id: shop.id,
        name: shop.name || '未知店铺',
        coverImage: shop.coverImage || 'https://picsum.photos/seed/defaultshop/600/400',
        categoryName: shop.categoryName || shop.category || '未分类',
        score: typeof shop.score === 'number' ? shop.score.toFixed(1) : (shop.score ? String(shop.score) : '0.0'),
        address: shop.address || '',
        perCapita: shop.perCapita != null && shop.perCapita !== '' ? String(shop.perCapita) : '',
        collectedAt: this.formatDateTime(shop.collectedAt),
        tags: Array.isArray(shop.tags) ? shop.tags.slice(0, 3) : []
      };
    }, this);
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

  onShopTap: function (e) {
    var id = e.currentTarget.dataset.id;
    if (!id) return;
    wx.navigateTo({
      url: '/pages/detail/detail?shopId=' + id
    });
  },

  onCancelCollect: function (e) {
    var that = this;
    var id = e.currentTarget.dataset.id;
    if (!id) return;
    if (!that.data.userId) {
      wx.showToast({ title: '用户信息未就绪', icon: 'none' });
      return;
    }

    request.delete(config.API.COLLECTIONS, {
      userId: that.data.userId,
      shopId: id
    })
      .then(function () {
        wx.showToast({ title: '已取消收藏', icon: 'success' });
        that.loadFavorites();
      })
      .catch(function (err) {
        console.error('取消收藏失败:', err);
      });
  }
});
