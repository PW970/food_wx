// pages/search/search.js - 搜索页
const app = getApp();
const request = require('../../utils/request.js');
const config = require('../../utils/config.js');

Page({
  data: {
    keyword: '',
    lat: 31.4912,
    lng: 120.3119,
    loading: false,
    errorMsg: '',
    results: [],
    hasSearched: false
  },

  onLoad: function () {
    this.resolveUserLocation().catch(function () {
      return null;
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
          app.globalData.userLocation = { lat: lat, lng: lng };
          that.setData({ lat: lat, lng: lng });
          resolve(res);
        },
        fail: function (err) {
          var fallback = app.globalData.userLocation || {};
          if (fallback.lat && fallback.lng) {
            that.setData({ lat: fallback.lat, lng: fallback.lng });
            resolve(fallback);
            return;
          }
          reject(err);
        }
      });
    });
  },

  onKeywordInput: function (e) {
    this.setData({
      keyword: e.detail.value
    });
  },

  onClearKeyword: function () {
    this.setData({
      keyword: '',
      results: [],
      errorMsg: '',
      hasSearched: false
    });
  },

  onSearch: function () {
    var that = this;
    var kw = (that.data.keyword || '').trim();
    if (!kw) {
      wx.showToast({ title: '请输入关键字', icon: 'none' });
      return;
    }

    that.setData({
      loading: true,
      errorMsg: '',
      hasSearched: true
    });

    request
      .get(config.API.SHOPS + '/search', {
        keyword: kw,
        lat: that.data.lat,
        lng: that.data.lng,
        radiusMeters: 5000
      })
      .then(function (data) {
        that.setData({
          results: that.normalizeList(data),
          loading: false
        });
      })
      .catch(function (err) {
        console.error('搜索失败:', err);
        that.setData({
          loading: false,
          errorMsg: '搜索失败，请稍后重试'
        });
      });
  },

  normalizeList: function (list) {
    if (!Array.isArray(list)) return [];
    return list.map(function (shop) {
      return {
        id: shop.id,
        name: shop.name || '未知店铺',
        coverImage: shop.coverImage || '',
        categoryName: shop.categoryName || shop.category || '未分类',
        score: typeof shop.score === 'number' ? shop.score.toFixed(1) : (shop.score ? String(shop.score) : '0.0'),
        address: shop.address || '',
        perCapita: shop.perCapita != null && shop.perCapita !== '' ? String(shop.perCapita) : '',
        tags: Array.isArray(shop.tags) ? shop.tags : []
      };
    });
  },

  onResultTap: function (e) {
    var id = e.currentTarget.dataset.id;
    wx.navigateTo({
      url: '/pages/detail/detail?shopId=' + id
    });
  }
});
