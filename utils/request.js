/**
 * utils/request.js - 网络请求封装
 */

const config = require('./config.js');

function appendQuery(url, data) {
  if (!data || typeof data !== 'object') {
    return url;
  }

  var query = Object.keys(data)
    .filter(function (key) {
      return data[key] !== undefined && data[key] !== null && data[key] !== '';
    })
    .map(function (key) {
      return encodeURIComponent(key) + '=' + encodeURIComponent(data[key]);
    })
    .join('&');

  if (!query) {
    return url;
  }

  return url + (url.indexOf('?') === -1 ? '?' : '&') + query;
}

/**
 * 封装 wx.request 为 Promise 风格
 * @param {Object} options - 请求配置
 * @param {string} options.url - 请求路径（会自动拼接 BASE_URL）
 * @param {string} options.method - 请求方法，默认 GET
 * @param {Object} options.data - 请求参数
 * @param {Object} options.header - 请求头
 * @returns {Promise} 返回 Promise 对象
 */
function request(options) {
  return new Promise(function (resolve, reject) {
    // 拼接完整 URL
    var method = options.method || 'GET';
    var requestData = options.data || {};
    var fullUrl = config.BASE_URL + options.url;

    if (method === 'GET' || method === 'DELETE') {
      fullUrl = appendQuery(fullUrl, requestData);
    }

    // 默认请求配置
    var defaultOptions = {
      url: fullUrl,
      method: method,
      data: method === 'GET' || method === 'DELETE' ? {} : requestData,
      header: options.header || {
        'Content-Type': 'application/json;charset=UTF-8',
        'Accept-Charset': 'UTF-8'
      },
      success: function (res) {
        // 根据业务需求处理响应
        // 假设后端返回格式为 { code: xxx, data: xxx, message: xxx }
        if (res.statusCode >= 200 && res.statusCode < 300) {
          var result = res.data;
          // 如果后端返回成功（code === 0 或 200）
          if (result.code === 0 || result.code === 200) {
            resolve(result.data);
          } else {
            // 业务错误
            wx.showToast({
              title: result.message || '请求失败',
              icon: 'none'
            });
            reject(result);
          }
        } else {
          // HTTP 错误
          wx.showToast({
            title: '网络错误',
            icon: 'none'
          });
          reject(res);
        }
      },
      fail: function (err) {
        // 请求失败
        wx.showToast({
          title: '网络请求失败',
          icon: 'none'
        });
        reject(err);
      }
    };

    // 发起请求
    wx.request(defaultOptions);
  });
}

/**
 * GET 请求
 */
function get(url, data, header) {
  return request({
    url: url,
    method: 'GET',
    data: data,
    header: header
  });
}

/**
 * POST 请求
 */
function post(url, data, header) {
  return request({
    url: url,
    method: 'POST',
    data: data,
    header: header
  });
}

/**
 * PUT 请求
 */
function put(url, data, header) {
  return request({
    url: url,
    method: 'PUT',
    data: data,
    header: header
  });
}

/**
 * DELETE 请求
 */
function del(url, data, header) {
  return request({
    url: url,
    method: 'DELETE',
    data: data,
    header: header
  });
}

module.exports = {
  request: request,
  get: get,
  post: post,
  put: put,
  delete: del
};
