// pages/search/search.js - 搜索页
const request = require('../../utils/request.js');
const config = require('../../utils/config.js');

Page({
  data: {
    keyword: '',
    loading: false,
    errorMsg: '',
    results: [],
    hasSearched: false
  },

  onLoad: function () {},

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
      .get(config.API.SHOPS + '/search', { keyword: kw })
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

