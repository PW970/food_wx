// pages/myreviews/myreviews.js - 我的评论
const app = getApp();
const request = require('../../utils/request.js');
const config = require('../../utils/config.js');

Page({
  data: {
    userId: null,
    loading: true,
    errorMsg: '',
    reviews: [],
    stars: [1, 2, 3, 4, 5]
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
        reviews: []
      });
      return Promise.resolve();
    }

    this.setData({
      userId: userId
    });
    return this.loadMyReviews();
  },

  loadMyReviews: function () {
    var that = this;
    if (!that.data.userId) {
      that.setData({
        loading: false,
        errorMsg: '用户信息加载中，请稍后重试',
        reviews: []
      });
      return Promise.reject(new Error('missing userId'));
    }

    that.setData({
      loading: true,
      errorMsg: ''
    });

    request
      .get(config.API.REVIEWS + '/user/' + that.data.userId)
      .then(function (data) {
        var list = Array.isArray(data) ? data : [];
        that.setData({
          reviews: that.normalizeReviews(list),
          loading: false
        });
      })
      .catch(function (err) {
        console.error('加载我的评论失败:', err);
        that.setData({
          loading: false,
          errorMsg: '加载评论失败，请稍后重试',
          reviews: []
        });
      });
  },

  normalizeReviews: function (list) {
    return list.map(function (r) {
      return {
        id: r.id,
        shopId: r.shopId,
        shopName: r.shopName || '',
        shopCoverImage: r.shopCoverImage || 'https://picsum.photos/seed/defaultshop/600/400',
        nickname: r.nickname || '匿名用户',
        rating: r.rating || 0,
        content: r.content || '',
        createdAt: this.formatDateTime(r.createdAt)
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

  onDeleteReview: function (e) {
    var that = this;
    var reviewId = e.currentTarget.dataset.id;
    if (!reviewId) return;

    wx.showModal({
      title: '删除评论',
      content: '删除后将同步更新店铺评分，确认继续吗？',
      success: function (res) {
        if (!res.confirm) return;

        request.delete(config.API.REVIEWS + '/' + reviewId)
          .then(function () {
            wx.showToast({ title: '评论已删除', icon: 'success' });
            that.loadMyReviews();
          })
          .catch(function (err) {
            console.error('删除评论失败:', err);
          });
      }
    });
  },

  onGoDetail: function (e) {
    var shopId = e.currentTarget.dataset.shopId;
    if (!shopId) return;
    wx.navigateTo({
      url: '/pages/detail/detail?shopId=' + shopId
    });
  }
});
