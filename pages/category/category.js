// pages/category/category.js - 分类页
const request = require('../../utils/request.js');
const config = require('../../utils/config.js');

Page({
  data: {
    categories: [],
    activeCategoryId: null,
    shops: [],
    loadingCategories: true,
    loadingShops: false,
    errorMsg: '',
    shopErrorMsg: ''
  },

  onLoad: function () {
    this.loadCategories();
  },

  loadCategories: function () {
    var that = this;
    that.setData({
      loadingCategories: true,
      errorMsg: ''
    });

    request
      .get(config.API.CATEGORIES)
      .then(function (data) {
        var list = Array.isArray(data) ? data : [];
        that.setData({
          categories: list,
          loadingCategories: false
        });

        if (list.length > 0) {
          that.setData({ activeCategoryId: list[0].id });
          that.loadShopsByCategory(list[0].id);
        } else {
          that.setData({ shops: [] });
        }
      })
      .catch(function (err) {
        console.error('加载分类失败:', err);
        that.setData({
          loadingCategories: false,
          errorMsg: '加载分类失败，请稍后重试'
        });
      });
  },

  onCategoryTap: function (e) {
    var id = e.currentTarget.dataset.id;
    if (!id || id === this.data.activeCategoryId) return;
    this.setData({ activeCategoryId: id });
    this.loadShopsByCategory(id);
  },

  loadShopsByCategory: function (categoryId) {
    var that = this;
    that.setData({
      loadingShops: true,
      shopErrorMsg: ''
    });

    request
      .get(config.API.SHOPS + '/category/' + categoryId)
      .then(function (data) {
        that.setData({
          shops: that.normalizeShops(data),
          loadingShops: false
        });
      })
      .catch(function (err) {
        console.error('加载分类店铺失败:', err);
        that.setData({
          shops: [],
          loadingShops: false,
          shopErrorMsg: '加载店铺失败，请稍后重试'
        });
      });
  },

  normalizeShops: function (list) {
    if (!Array.isArray(list)) return [];
    return list.map(function (shop) {
      return {
        id: shop.id,
        name: shop.name || '未知店铺',
        coverImage: shop.coverImage || '',
        categoryName: shop.categoryName || shop.category || '未分类',
        score: typeof shop.score === 'number' ? shop.score.toFixed(1) : (shop.score ? String(shop.score) : '0.0'),
        address: shop.address || '',
        perCapita: shop.perCapita != null && shop.perCapita !== '' ? String(shop.perCapita) : ''
      };
    });
  },

  onShopTap: function (e) {
    var id = e.currentTarget.dataset.id;
    if (!id) return;
    wx.navigateTo({
      url: '/pages/detail/detail?shopId=' + id
    });
  }
});
