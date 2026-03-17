/**
 * utils/config.js - 小程序配置文件
 */

// ==================== 重要：真机调试配置 ====================
// 当前已切换为本机局域网地址，便于真机调试
// 查看局域网IP方法：终端运行 `ifconfig` 或 `ipconfig`
// 示例：const BASE_URL = 'http://192.168.1.xxx:8080'
// ============================================================

// 后端服务器基础地址
const BASE_URL = 'http://192.168.1.91:8080';

// API 接口路径
const API = {
  // 推荐接口
  RECOMMENDATIONS: '/api/recommendations',
  // 分类接口
  CATEGORIES: '/api/categories',
  // 店铺接口
  SHOPS: '/api/shops',
  // 标签接口
  TAGS: '/api/tags',
  // 收藏接口
  COLLECTIONS: '/api/collections',
  // 评价接口
  REVIEWS: '/api/reviews',
  // 用户接口
  USER: '/api/user',
  // 微信登录接口
  WX_LOGIN: '/api/user/wx-login',
  // 云开发 openid 登录接口
  OPENID_LOGIN: '/api/user/openid-login',
  // 用户资料更新
  USER_PROFILE: '/api/user/profile',
  // 用户头像上传
  USER_AVATAR: '/api/user/avatar'
};

const CLOUD = {
  ENV_ID: 'cloud1-1g8r543l9ec140f6',
  LOGIN_FUNCTION: 'quickstartFunctions'
};

module.exports = {
  BASE_URL: BASE_URL,
  API: API,
  CLOUD: CLOUD
};
