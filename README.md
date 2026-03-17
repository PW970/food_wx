# 本地美食推荐小程序

一个基于微信小程序原生开发 + Spring Boot + MySQL 的本地美食推荐项目。当前项目已经包含推荐、分类、搜索、地图查看、店铺详情、收藏、评论、个人中心、微信登录和云开发 `openid` 登录链路，并已接入腾讯地图真实餐饮 POI 同步能力。

## 项目简介

这个项目的目标是做一个“附近美食推荐”小程序，前端负责展示和交互，后端负责数据、推荐和用户行为管理。

当前已经接入的核心能力包括：

- 首页个性化推荐
- 分类浏览与搜索
- 店铺详情与评论展示
- 收藏 / 取消收藏
- 发布评论 / 删除评论
- 个人中心
- 腾讯地图位置检索与附近推荐
- 腾讯地图真实 POI 同步入库
- 小程序云开发获取 `openid`
- 后端用户系统与本地用户资料管理

## 技术栈

### 小程序端

- 微信小程序原生：`WXML`、`WXSS`、`JavaScript`
- 原生地图组件：`map`
- 小程序云开发：`wx.cloud`

### 后端

- `Spring Boot 3`
- `Spring Data JPA`
- `MySQL 8+`
- `Maven`

### 第三方服务

- 腾讯位置服务 WebService
- 微信小程序云开发环境

## 当前实现状态

目前仓库内已经完成了以下内容：

- 主要页面 UI 已统一到简约现代化风格
- 后端接口可编译、可启动
- 数据库初始化脚本已提供
- 收藏、评论、推荐、地图页面已接通主链路
- 首页、地图页、分类页、搜索页已支持按用户位置拉取腾讯地图真实 POI
- 真实 POI 会同步为本地 `shop` 记录，以兼容详情、收藏、评论链路
- 已增加腾讯地图请求失败日志与 POI 缓存保护，避免额度被重复打满
- 小程序登录主链路已切到“云开发获取 `openid` -> 后端 `openid-login`”
- 个人中心已收敛为简洁信息卡片，仅保留登录 / 退出登录

当前真实 POI 接入状态说明：

- 腾讯地图 WebService 可作为真实餐饮数据来源
- 店铺同步依赖腾讯地图 `WebServiceAPI`
- 如果 Key 未开通 `WebServiceAPI`，后端会回退到本地测试店铺
- 如果 Key 当日配额耗尽，后端同样会回退到本地测试店铺
- 当前代码已经支持这套回退逻辑，并会在后端日志里打印腾讯地图失败原因

当前限制：

- 真实 POI 同步成功前，推荐、分类、搜索仍可能展示初始化库中的店铺
- POI 同步后的图片、标签、评分仍有一部分是本地兜底字段，不是腾讯地图原始字段
- 真实 POI 目前是按请求懒同步，不是后台定时全量同步

当前登录方案说明：

- 主推荐方案：小程序通过云函数拿 `openid`，再调用后端 `/api/user/openid-login`
- 备用方案：后端仍保留 `/api/user/wx-login`，支持 `wx.login -> code2session`
- 但前端当前主路径走的是云开发 `openid` 登录

## 项目结构

```text
.
├── app.js / app.json / app.wxss              # 小程序全局入口与全局样式
├── pages/                                    # 小程序页面
│   ├── home/                                 # 首页推荐
│   ├── map/                                  # 地图页
│   ├── search/                               # 搜索页
│   ├── detail/                               # 店铺详情
│   ├── review/                               # 发布评论
│   ├── category/                             # 分类页
│   ├── favorites/                            # 我的收藏
│   ├── myreviews/                            # 我的评论
│   └── profile/                              # 个人中心
├── utils/
│   ├── config.js                             # 小程序接口与云环境配置
│   └── request.js                            # 请求封装
├── cloudfunctions/
│   └── quickstartFunctions/                  # 云函数，用于获取 openid
├── sql/
│   ├── init.sql                              # 初始化脚本
│   ├── v2_upgrade.sql                        # 升级脚本
│   └── fix_encoding.sql                      # 编码修复脚本
├── src/main/java/com/foodrecommendation/
│   ├── controller/                           # 控制器
│   ├── service/                              # 业务服务
│   ├── repository/                           # 数据访问层
│   ├── entity/                               # 实体类
│   ├── dto/                                  # 请求 DTO
│   ├── vo/                                   # 返回 VO
│   ├── integration/                          # 微信 / 腾讯地图集成
│   └── config/                               # 配置与初始化
├── src/main/resources/
│   └── application.yml                       # 后端配置
├── uploads/                                  # 本地头像上传目录
├── pom.xml                                   # Maven 配置
└── project.config.json                       # 微信开发者工具项目配置
```

## 功能总览

### 小程序页面

- 首页：推荐列表、推荐理由、快速入口
- 地图页：地图标点、推荐店铺联动
- 搜索页：按关键词搜索店铺
- 分类页：按分类浏览店铺
- 详情页：店铺信息、评论、收藏、写评论
- 评论页：发布评论
- 个人中心：微信登录、退出登录、收藏入口、评论入口
- 我的收藏：查看和取消收藏
- 我的评论：查看和删除评论

### 后端接口能力

- 用户：登录、`openid` 登录、获取用户、更新资料、头像上传
- 推荐：基于用户、位置、偏好和腾讯地图位置数据输出推荐结果
- 店铺：真实 POI 同步、列表、分类、搜索、详情
- 收藏：查询、添加、取消
- 评论：查询、发布、删除

## 真实 POI 数据流说明

项目当前不是直接让前端消费腾讯地图 POI 原始结果，而是走下面这条链路：

1. 小程序拿到当前位置 `lat/lng`
2. 前端把坐标传给后端推荐、分类、搜索接口
3. 后端调用腾讯地图 WebService 搜索附近餐饮 POI
4. 后端把 POI 同步为本地 `shop` 数据，并记录：
   - `external_poi_id`
   - `data_source`
   - `last_synced_at`
5. 前端继续使用本地 `shopId` 展示详情、收藏、评论

这样做的原因是：

- 详情页、收藏、评论都依赖本地 `shopId`
- 如果前端直接使用腾讯地图原始 `poi id`，现有收藏和评论体系会全部断开
- 现在这套方式既能接入真实 POI，又不需要重写收藏和评论模型

当前同步策略：

- 首页推荐：优先读取真实 POI 同步数据
- 地图页：优先读取真实 POI 同步数据
- 分类页：按分类关键词触发 POI 同步
- 搜索页：按搜索关键词触发 POI 同步
- 30 分钟内优先复用已同步的真实 POI，避免重复消耗腾讯地图额度
- 如果腾讯地图不可用，则自动回退到本地 `shop` 数据

## 环境要求

建议环境：

- `JDK 17`
- `Maven 3.8+`
- `MySQL 8+`
- 微信开发者工具 Stable
- Node.js：仅在处理云函数依赖时需要

## 快速开始

### 1. 克隆并进入项目

```bash
git clone https://github.com/PW970/food_wx.git
cd food_wx
```

### 2. 启动 MySQL

确保本机有可访问的 MySQL 实例，默认连接配置见 [src/main/resources/application.yml](/Users/makebukepurou/Desktop/food_wx-1/src/main/resources/application.yml)：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/fooddb?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: root
    password:
```

如果你的 MySQL 用户名或密码不同，请先修改这里。

### 3. 初始化数据库

先执行：

- [sql/init.sql](/Users/makebukepurou/Desktop/food_wx-1/sql/init.sql)

如果你是从旧版本升级，可按需再执行：

- [sql/v2_upgrade.sql](/Users/makebukepurou/Desktop/food_wx-1/sql/v2_upgrade.sql)
- [sql/fix_encoding.sql](/Users/makebukepurou/Desktop/food_wx-1/sql/fix_encoding.sql)

说明：

- 后端启动后，`DataInitializer` 会在空库场景补一批基础分类、用户和本地兜底店铺
- 这些初始化数据主要用于未同步到真实 POI 时的回退展示
- 如果你已经有数据，不建议反复重跑初始化脚本

### 4. 启动后端

在项目根目录运行：

```bash
mvn spring-boot:run
```

默认服务地址：

- `http://127.0.0.1:8080`

建议先验证几个接口：

- `GET /api/categories`
- `GET /api/shops/search?keyword=火锅&lat=31.4912&lng=120.3119&radiusMeters=5000`
- `GET /api/recommendations?userId=0&lat=31.4912&lng=120.3119&limit=3`

### 5. 打开微信开发者工具

使用微信开发者工具打开项目根目录：

`/Users/makebukepurou/Desktop/food_wx-1`

当前项目配置见 [project.config.json](/Users/makebukepurou/Desktop/food_wx-1/project.config.json)：

- `appid`: `wxa39950b899a41ebd`
- `cloudfunctionRoot`: `cloudfunctions/`

### 6. 配置小程序请求地址

当前请求配置在 [utils/config.js](/Users/makebukepurou/Desktop/food_wx-1/utils/config.js)：

```js
const BASE_URL = 'http://192.168.1.91:8080';
```

这是一台具体开发机器的局域网地址，只适合当前机器真机调试。

你自己的开发环境请按下面规则修改：

- 开发者工具模拟器联调：可以改成 `http://127.0.0.1:8080`
- 真机调试：改成你电脑当前局域网 IP，例如 `http://192.168.1.xxx:8080`

如果你不确定本机 IP，可以在 macOS 里运行：

```bash
ipconfig getifaddr en0
```

或：

```bash
ipconfig getifaddr en1
```

### 7. 配置云开发环境

当前前端主登录链路依赖云函数获取 `openid`。

配置位置在 [utils/config.js](/Users/makebukepurou/Desktop/food_wx-1/utils/config.js)：

```js
const CLOUD = {
  ENV_ID: 'cloud1-1g8r543l9ec140f6',
  LOGIN_FUNCTION: 'quickstartFunctions'
};
```

当前项目里写死的是某个现有环境：

- 环境 ID：`cloud1-1g8r543l9ec140f6`
- 云函数名：`quickstartFunctions`

如果你要迁移到自己的环境，需要：

1. 在微信开发者工具中开通云开发
2. 把环境 ID 改成你自己的
3. 上传并部署云函数 [cloudfunctions/quickstartFunctions](/Users/makebukepurou/Desktop/food_wx-1/cloudfunctions/quickstartFunctions)

### 8. 安装云函数依赖并部署

云函数目录：

- [cloudfunctions/quickstartFunctions](/Users/makebukepurou/Desktop/food_wx-1/cloudfunctions/quickstartFunctions)

如果本地还没装依赖，进入该目录执行：

```bash
cd cloudfunctions/quickstartFunctions
npm install
```

然后在微信开发者工具中：

1. 右键 `cloudfunctions/quickstartFunctions`
2. 选择“上传并部署：云端安装依赖”

如果这一步没做，小程序登录会报：

- `FunctionName parameter could not be found`
- `FUNCTION_NOT_FOUND`

### 9. 编译并联调

到这里以后，正常的联调顺序是：

1. MySQL 已启动
2. 后端 `8080` 已启动
3. 小程序 `BASE_URL` 已改对
4. 云函数已部署
5. 微信开发者工具重新编译

## 登录方案说明

项目里目前同时保留两套微信登录能力。

### 方案 A：云开发 `openid` 登录

当前前端主要使用这套：

1. 小程序调用云函数 `quickstartFunctions`
2. 云函数返回当前用户 `openid`
3. 前端请求后端 `/api/user/openid-login`
4. 后端按 `openid` 查找或创建本地用户

对应文件：

- [app.js](/Users/makebukepurou/Desktop/food_wx-1/app.js)
- [pages/profile/profile.js](/Users/makebukepurou/Desktop/food_wx-1/pages/profile/profile.js)
- [cloudfunctions/quickstartFunctions/index.js](/Users/makebukepurou/Desktop/food_wx-1/cloudfunctions/quickstartFunctions/index.js)
- [src/main/java/com/foodrecommendation/controller/UserController.java](/Users/makebukepurou/Desktop/food_wx-1/src/main/java/com/foodrecommendation/controller/UserController.java)

优点：

- 不依赖后端 `AppSecret`
- 真机环境更自然
- 登录身份获取更稳定

### 方案 B：`wx.login -> code2session`

后端仍保留这一套接口：

- `POST /api/user/wx-login`

配置在 [src/main/resources/application.yml](/Users/makebukepurou/Desktop/food_wx-1/src/main/resources/application.yml)：

```yaml
wechat:
  mini-program:
    app-id: ${WECHAT_MINI_APP_ID:wxa39950b899a41ebd}
    secret: ${WECHAT_MINI_APP_SECRET:}
    code2session-url: ${WECHAT_CODE2SESSION_URL:https://api.weixin.qq.com/sns/jscode2session}
```

如果你将来想切回这条链路，需要补：

```bash
export WECHAT_MINI_APP_ID=你的小程序AppID
export WECHAT_MINI_APP_SECRET=你的小程序AppSecret
```

但当前小程序前端默认不走这一套。

## 腾讯地图配置

项目已经接入腾讯地图真实 POI 同步和基于位置的推荐。

配置在 [src/main/resources/application.yml](/Users/makebukepurou/Desktop/food_wx-1/src/main/resources/application.yml)：

```yaml
tencent:
  map:
    enabled: ${TENCENT_MAP_ENABLED:true}
    key: ${TENCENT_MAP_KEY:VC6BZ-HZBL3-6PQ37-OMPSG-U7TLQ-Z6FHW}
    base-url: ${TENCENT_MAP_BASE_URL:https://apis.map.qq.com}
```

当前仓库里已经存在默认 Key 和开启状态。  
如果你部署到自己的环境，建议改成环境变量管理，而不是继续把 Key 写在配置文件中。

推荐做法：

```bash
export TENCENT_MAP_ENABLED=true
export TENCENT_MAP_KEY=你的腾讯地图Key
```

腾讯地图 Key 的要求：

- 必须开通 `WebServiceAPI`
- 必须允许调用地点搜索接口
- 建议单独准备开发 Key，不要和其他项目混用
- 如果你用的是腾讯位置服务控制台新建的 Key，记得确认启用了 WebService 相关产品

### 真实 POI 接入验证

先直接验证腾讯地图：

```bash
curl -s 'https://apis.map.qq.com/ws/place/v1/search?key=你的Key&keyword=%E7%BE%8E%E9%A3%9F&orderby=_distance&page_size=5&boundary=nearby(31.4912,120.3119,5000)'
```

成功时应看到：

- `status: 0`
- `data` 中包含真实店铺名称、地址、坐标、电话等字段

再验证本地后端：

```bash
curl -s 'http://127.0.0.1:8080/api/recommendations?userId=1&lat=31.4912&lng=120.3119&limit=5'
curl -s 'http://127.0.0.1:8080/api/shops/search?keyword=%E7%81%AB%E9%94%85&lat=31.4912&lng=120.3119&radiusMeters=5000'
curl -s 'http://127.0.0.1:8080/api/shops/category/1?lat=31.4912&lng=120.3119&radiusMeters=5000'
```

如果 POI 同步成功：

- 返回结果会逐步出现附近真实门店
- 数据库 `shop` 表里会出现：
  - `data_source = TENCENT`
  - `external_poi_id` 有值
  - `last_synced_at` 有值

可用下面的 SQL 检查：

```sql
select id, name, data_source, external_poi_id, last_synced_at
from shop
order by id desc
limit 20;
```

## 小程序权限说明

当前 [app.json](/Users/makebukepurou/Desktop/food_wx-1/app.json) 已声明定位用途：

```json
"permission": {
  "scope.userLocation": {
    "desc": "用于展示你附近的推荐店铺和交互地图"
  }
}
```

并声明了：

```json
"requiredPrivateInfos": [
  "getLocation"
]
```

说明：

- 地图页和推荐页会使用位置能力
- 如果用户拒绝定位，需要在页面中引导用户重新授权

## 核心接口清单

### 用户

- `POST /api/user/login`
- `POST /api/user/wx-login`
- `POST /api/user/openid-login`
- `PUT /api/user/profile`
- `POST /api/user/avatar`
- `GET /api/user/{id}`

### 推荐

- `GET /api/recommendations?userId={userId}&lat={lat}&lng={lng}&limit={limit}`

### 店铺

- `GET /api/shops?lat={lat}&lng={lng}&radiusMeters={radiusMeters}`
- `GET /api/shops/category/{categoryId}?lat={lat}&lng={lng}&radiusMeters={radiusMeters}`
- `GET /api/shops/search?keyword=关键词&lat={lat}&lng={lng}&radiusMeters={radiusMeters}`
- `GET /api/shops/{id}?userId={userId}`

说明：

- `lat/lng` 传入后，后端会优先尝试同步腾讯地图真实 POI
- `radiusMeters` 默认使用附近 5000 米
- 如果不传坐标，或腾讯地图不可用，后端会回退到本地店铺数据

### 收藏

- `GET /api/collections?userId={userId}`
- `POST /api/collections`
- `DELETE /api/collections?userId={userId}&shopId={shopId}`

### 评论

- `GET /api/reviews?shopId={shopId}`
- `GET /api/reviews/user/{userId}`
- `POST /api/reviews`
- `DELETE /api/reviews/{id}`

## 主要配置文件说明

### 小程序配置

- [app.json](/Users/makebukepurou/Desktop/food_wx-1/app.json)：页面注册、tabBar、定位权限
- [app.js](/Users/makebukepurou/Desktop/food_wx-1/app.js)：全局登录、位置初始化、云开发初始化
- [utils/config.js](/Users/makebukepurou/Desktop/food_wx-1/utils/config.js)：接口地址、云环境 ID、云函数名
- [utils/request.js](/Users/makebukepurou/Desktop/food_wx-1/utils/request.js)：请求封装

### 后端配置

- [src/main/resources/application.yml](/Users/makebukepurou/Desktop/food_wx-1/src/main/resources/application.yml)：数据库、腾讯地图、微信配置
- [src/main/java/com/foodrecommendation/config/DataInitializer.java](/Users/makebukepurou/Desktop/food_wx-1/src/main/java/com/foodrecommendation/config/DataInitializer.java)：初始化演示数据
- [src/main/java/com/foodrecommendation/config/WebMvcConfig.java](/Users/makebukepurou/Desktop/food_wx-1/src/main/java/com/foodrecommendation/config/WebMvcConfig.java)：上传资源静态映射

## 常见问题

### 1. 微信登录失败：`FunctionName parameter could not be found`

原因：

- 云函数 `quickstartFunctions` 没有部署到当前云环境

处理方法：

1. 确认 [utils/config.js](/Users/makebukepurou/Desktop/food_wx-1/utils/config.js) 中环境 ID 正确
2. 右键部署 [cloudfunctions/quickstartFunctions](/Users/makebukepurou/Desktop/food_wx-1/cloudfunctions/quickstartFunctions)
3. 选择“上传并部署：云端安装依赖”
4. 重新编译小程序

### 2. 真机调试全是网络错误

常见原因：

- `BASE_URL` 还写着 `127.0.0.1`
- 手机和电脑不在同一个 Wi-Fi
- 后端没有启动
- 防火墙拦截了 `8080`

处理方法：

- 把 [utils/config.js](/Users/makebukepurou/Desktop/food_wx-1/utils/config.js) 中的 `BASE_URL` 改成电脑局域网 IP

### 3. 取消收藏报 `Required request parameter 'userId' ... is not present`

原因：

- 旧版前端把 `DELETE` 参数放在请求体里，没有拼到 URL query

当前状态：

- 已在 [utils/request.js](/Users/makebukepurou/Desktop/food_wx-1/utils/request.js) 修复

### 4. 首页提示“未配置微信小程序 AppID 或 AppSecret”

原因：

- 这是旧版 `wx-login` 链路报错

当前主方案：

- 前端默认已经改成云开发 `openid` 登录

如果你仍然看到这类错误，请检查是否还在走旧代码或旧缓存。

### 5. 评论提交后详情页没有立刻刷新

当前状态：

- 已修复
- 评论发布成功后，返回详情页会自动刷新当前店铺数据和评论列表

### 6. 开发者工具提示“请在编辑器云函数根目录选择一个云环境”

检查项：

- [project.config.json](/Users/makebukepurou/Desktop/food_wx-1/project.config.json) 是否有 `cloudfunctionRoot`
- 微信开发者工具当前项目是否绑定了正确云环境
- 云开发控制台当前环境是否就是 `cloud1-1g8r543l9ec140f6` 或你自己的环境

### 7. 腾讯地图返回 `此key未开启WebserviceAPI功能`

原因：

- 当前腾讯地图 Key 没有开通 `WebServiceAPI`

处理方法：

1. 登录腾讯位置服务控制台
2. 找到当前 Key
3. 开启 `WebServiceAPI`
4. 确认地点搜索相关能力已启用
5. 重启后端后重新验证

### 8. 腾讯地图返回 `此key每日调用量已达到上限`

原因：

- 当前 Key 在当天的配额已经耗尽

当前系统行为：

- 后端会记录警告日志
- 店铺同步会失败
- 推荐、搜索、分类会自动回退到本地测试店铺

处理方法：

1. 更换一个还有配额的 Key
2. 等第二天额度恢复
3. 申请更高配额
4. 尽量复用已同步数据，避免频繁刷新首页、地图、搜索

### 9. 我已经开了 WebService，但还是不是实时真 POI

常见原因：

- Key 已开通，但额度已经用完
- 小程序没传 `lat/lng`
- 后端正在回退到本地数据库
- 数据库里还没有成功同步出 `data_source = TENCENT` 的记录

建议排查：

1. 先直接 `curl` 腾讯地图接口，看是否 `status = 0`
2. 再查后端日志，看腾讯地图失败原因
3. 再查数据库 `shop` 表里是否已有 `TENCENT` 数据
4. 最后再看小程序页面是否拿到了位置权限

## 已完成的 UI 优化

这轮 UI 已做过统一升级，重点包括：

- 全局浅暖色系统
- 卡片圆角与阴影统一
- 首页 Hero 与推荐卡重构
- 详情页封面和摘要区优化
- 搜索页、分类页、评论页、个人中心统一风格
- 地图页补充现代化交互样式

主要涉及：

- [app.wxss](/Users/makebukepurou/Desktop/food_wx-1/app.wxss)
- [pages/home/home.wxss](/Users/makebukepurou/Desktop/food_wx-1/pages/home/home.wxss)
- [pages/detail/detail.wxss](/Users/makebukepurou/Desktop/food_wx-1/pages/detail/detail.wxss)
- [pages/search/search.wxss](/Users/makebukepurou/Desktop/food_wx-1/pages/search/search.wxss)
- [pages/profile/profile.wxss](/Users/makebukepurou/Desktop/food_wx-1/pages/profile/profile.wxss)

## 建议联调顺序

建议按下面顺序排查和联调：

1. 启动 MySQL
2. 运行 `mvn spring-boot:run`
3. 验证后端接口可访问
4. 确认 `BASE_URL` 正确
5. 确认云环境 ID 正确
6. 部署 `quickstartFunctions`
7. 在微信开发者工具重新编译
8. 按“首页 -> 详情 -> 收藏 -> 评论 -> 我的收藏 -> 我的评论 -> 登录”顺序回归

## 验证建议

推荐至少验证这些场景：

- 首页推荐是否能正常出数据
- 地图页 marker 是否正常展示
- 搜索是否能返回结果
- 详情页是否能显示评论和收藏状态
- 收藏 / 取消收藏是否正常
- 发布评论后详情页是否立即刷新
- 我的收藏 / 我的评论是否正常拉取
- 云开发登录是否成功

## 后续可继续完善的方向

- 给真实 POI 增加更稳定的封面图来源，而不是临时占位图
- 给真实 POI 同步补充标签生成策略
- 把腾讯地图 POI 同步做成定时任务或后台预热
- 做真正基于真实定位和真实 POI 的附近饭店推荐
- 增加评论编辑能力
- 增加收藏和评论数量统计
- 完善真机登录与授权体验
- 把配置统一迁移到环境变量，减少仓库内明文配置
