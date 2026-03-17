const app = getApp();
const request = require('../../utils/request.js');
const config = require('../../utils/config.js');

Page({
  data: {
    userId: null,
    lat: 31.4912,
    lng: 120.3119,
    scale: 14,
    loading: true,
    errorMsg: '',
    shops: [],
    markers: [],
    selectedShopId: null
  },

  onLoad: function () {
    this.mapCtx = wx.createMapContext('foodMap', this);
    this.setData({
      userId: app.getCurrentUserId() || 0
    });
    this.initLocationAndLoad();
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
          app.globalData.userLocation = { lat: lat, lng: lng };
          that.setData({ lat: lat, lng: lng });
          resolve(res);
        },
        fail: function (err) {
          var fallback = app.globalData.userLocation || {};
          if (fallback.lat && fallback.lng) {
            that.setData({
              lat: fallback.lat,
              lng: fallback.lng
            });
            resolve(fallback);
            return;
          }
          reject(err);
        }
      });
    });
  },

  loadRecommendations: function () {
    var that = this;
    that.setData({
      loading: true,
      errorMsg: ''
    });

    return request.get(config.API.RECOMMENDATIONS, {
      userId: that.data.userId,
      lat: that.data.lat,
      lng: that.data.lng,
      limit: 10
    })
      .then(function (data) {
        var shops = that.normalizeShops(data);
        that.setData({
          shops: shops,
          markers: that.buildMarkers(shops),
          selectedShopId: shops.length ? shops[0].shopId : null,
          loading: false
        });

        if (shops.length) {
          that.focusShop(shops[0].shopId);
        }
      })
      .catch(function (err) {
        console.error('地图推荐加载失败:', err);
        that.setData({
          loading: false,
          errorMsg: '地图推荐加载失败，请稍后重试'
        });
      });
  },

  normalizeShops: function (data) {
    if (!Array.isArray(data)) {
      return [];
    }

    return data.map(function (shop) {
      return {
        shopId: shop.shopId,
        shopName: shop.shopName || '未知店铺',
        coverImage: shop.coverImage || 'https://picsum.photos/seed/defaultshop/600/400',
        categoryName: shop.categoryName || '未分类',
        score: shop.originalScore ? shop.originalScore.toFixed(1) : '0.0',
        distance: typeof shop.distance === 'number' ? shop.distance.toFixed(1) + 'km' : '未知',
        distanceKm: typeof shop.distance === 'number' ? shop.distance : null,
        recommendReason: shop.recommendReason || '暂无推荐理由',
        tags: shop.tags || [],
        latitude: typeof shop.latitude === 'number' ? shop.latitude : null,
        longitude: typeof shop.longitude === 'number' ? shop.longitude : null
      };
    });
  },

  buildMarkers: function (shops) {
    var that = this;
    return shops.map(function (shop, index) {
      var latOffset = 0.006 * Math.sin(index + 1);
      var lngOffset = 0.008 * Math.cos(index + 1);
      var markerLat = shop.latitude != null ? shop.latitude : (that.data.lat + latOffset);
      var markerLng = shop.longitude != null ? shop.longitude : (that.data.lng + lngOffset);

      shop.latitude = markerLat;
      shop.longitude = markerLng;

      return {
        id: Number(shop.shopId),
        latitude: markerLat,
        longitude: markerLng,
        width: 34,
        height: 42,
        callout: {
          content: shop.shopName,
          color: '#1f2520',
          fontSize: 12,
          borderRadius: 12,
          borderWidth: 1,
          borderColor: '#f0dfd1',
          bgColor: '#fffdf9',
          padding: 8,
          display: 'ALWAYS'
        }
      };
    });
  },

  onMarkerTap: function (e) {
    var shopId = e.detail.markerId || e.currentTarget.dataset.shopId;
    this.focusShop(shopId);
  },

  onShopCardTap: function (e) {
    var shopId = e.currentTarget.dataset.shopId;
    this.focusShop(shopId);
  },

  onFocusShopTap: function (e) {
    var shopId = e.currentTarget.dataset.shopId;
    this.focusShop(shopId);
  },

  focusShop: function (shopId) {
    var that = this;
    var targetId = Number(shopId);
    var shop = (that.data.shops || []).find(function (item) {
      return Number(item.shopId) === targetId;
    });

    if (!shop) {
      return;
    }

    that.setData({
      selectedShopId: targetId,
      lat: shop.latitude || that.data.lat,
      lng: shop.longitude || that.data.lng,
      scale: 15
    });
  },

  onGoDetail: function (e) {
    var shopId = e.currentTarget.dataset.shopId;
    wx.navigateTo({
      url: '/pages/detail/detail?shopId=' + shopId
    });
  },

  onRefreshTap: function () {
    this.initLocationAndLoad();
  },

  onRelocate: function () {
    var location = app.globalData.userLocation || {};
    if (location.lat && location.lng) {
      this.setData({
        lat: location.lat,
        lng: location.lng,
        scale: 14
      });
    }
    if (this.mapCtx && this.mapCtx.moveToLocation) {
      this.mapCtx.moveToLocation();
    }
  }
});
