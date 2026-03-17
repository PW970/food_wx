// pages/review/review.js - 发布评论页
const app = getApp();
const request = require('../../utils/request.js');
const config = require('../../utils/config.js');

Page({
  data: {
    userId: null,
    shopId: null,

    rating: 0,
    content: '',

    submitting: false,

    // 评分星星数组
    stars: [1, 2, 3, 4, 5]
  },

  onLoad: function (options) {
    var that = this;
    var shopId = options && options.shopId ? Number(options.shopId) : null;
    if (!shopId) {
      wx.showToast({ title: '缺少 shopId', icon: 'none' });
      setTimeout(function () {
        wx.navigateBack();
      }, 800);
      return;
    }
    this.setData({
      shopId: shopId,
      userId: app.getCurrentUserId()
    });
    if (!app.getCurrentUserId()) {
      wx.showToast({ title: '请先在“我的”页面完成微信登录', icon: 'none' });
    }
  },

  onSelectRating: function (e) {
    var v = Number(e.currentTarget.dataset.value) || 0;
    this.setData({ rating: v });
  },

  onContentInput: function (e) {
    this.setData({ content: e.detail.value });
  },

  onSubmit: function () {
    var that = this;
    if (that.data.submitting) return;
    if (!that.data.userId) {
      wx.showToast({ title: '用户信息未就绪', icon: 'none' });
      return;
    }

    var rating = that.data.rating;
    var content = (that.data.content || '').trim();

    if (rating < 1 || rating > 5) {
      wx.showToast({ title: '请选择 1-5 星评分', icon: 'none' });
      return;
    }
    if (!content) {
      wx.showToast({ title: '请输入评论内容', icon: 'none' });
      return;
    }
    if (content.length < 2) {
      wx.showToast({ title: '评论内容太短啦', icon: 'none' });
      return;
    }

    that.setData({ submitting: true });
    wx.showLoading({ title: '提交中...' });

    request
      .post(config.API.REVIEWS, {
        userId: that.data.userId,
        shopId: that.data.shopId,
        rating: rating,
        content: content
      })
      .then(function () {
        wx.hideLoading();
        wx.showToast({ title: '发布成功', icon: 'success' });
        wx.setStorageSync('detailReviewRefreshShopId', that.data.shopId);

        setTimeout(function () {
          wx.navigateBack();
        }, 600);
      })
      .catch(function (err) {
        wx.hideLoading();
        console.error('发布评论失败:', err);
      })
      .finally(function () {
        that.setData({ submitting: false });
      });
  }
});
