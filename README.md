# 本地美食推荐小程序

一个基于微信小程序 + Spring Boot + MySQL 的本地美食推荐项目，包含首页推荐、分类浏览、搜索、店铺详情、收藏、评论等完整流程。

## 当前状态

- 小程序主界面已完成一轮现代化 UI 优化，核心页面视觉已经统一。
- 后端 Maven 打包已验证通过。
- 当前本机 `127.0.0.1:3306` 没有 MySQL 服务在运行，所以后端接口暂时还不能直接启动联调。
- 微信开发者工具 CLI 安全服务端口还没有真正打开，命令行预览暂时受限。

## 技术栈

- 小程序原生开发：`WXML`、`WXSS`、`JavaScript`
- 后端：`Spring Boot 3.2`、`Spring Data JPA`
- 数据库：`MySQL 8+`
- 构建工具：`Maven`

## 项目结构

```text
.
├── app.json / app.js / app.wxss
├── pages/                  # 小程序页面
├── utils/                  # 小程序请求与配置
├── src/main/java/          # Spring Boot 后端
├── src/main/resources/     # 后端配置
├── sql/                    # 数据库脚本
└── pom.xml
```

## 启动步骤

### 1. 启动 MySQL

确保本机有一个名为 `fooddb` 的数据库可用，默认配置在 [src/main/resources/application.yml](/Users/makebukepurou/Desktop/food_wx-1/src/main/resources/application.yml)：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/fooddb?useUnicode=true&characterEncoding=utf8&useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true
    username: root
    password:
```

如果你的 MySQL 有密码，记得同步修改这里的 `password`。

### 2. 初始化数据库

执行下面的 SQL 脚本：

- [sql/init.sql](/Users/makebukepurou/Desktop/food_wx-1/sql/init.sql)
- 如果库结构已存在但字段不完整，可再按需执行 [sql/v2_upgrade.sql](/Users/makebukepurou/Desktop/food_wx-1/sql/v2_upgrade.sql)

项目启动后，后端还会通过 `DataInitializer` 自动补充一批测试数据。

### 3. 启动后端

在项目根目录执行：

```bash
mvn spring-boot:run
```

默认启动地址：

- 服务地址：`http://127.0.0.1:8080`
- 推荐接口：`http://127.0.0.1:8080/api/recommendations`

### 4. 启动微信开发者工具

1. 用微信开发者工具打开项目根目录 `/Users/makebukepurou/Desktop/food_wx-1`
2. 确认使用项目内已有的 `appid`
3. 在「设置 -> 本地设置 / 安全设置」里确认：
   - 已关闭或放行 `urlCheck`
   - 已开启服务端口（如果你要使用 CLI）

### 5. 配置小程序接口地址

当前小程序请求配置在 [utils/config.js](/Users/makebukepurou/Desktop/food_wx-1/utils/config.js)：

```js
const BASE_URL = 'http://127.0.0.1:8080';
```

说明：

- 开发者工具模拟器联调时，通常可以直接使用 `127.0.0.1`
- 真机调试时，需要改成你电脑的局域网 IP，例如 `http://192.168.1.xxx:8080`

### 6. 配置腾讯地图增强推荐

项目已经接入腾讯地图 WebService 的附近地点搜索能力，用来增强现有贝叶斯推荐。

启动后端前可配置：

```bash
export TENCENT_MAP_ENABLED=true
export TENCENT_MAP_KEY=你的腾讯地图WebServiceKey
```

后端配置入口在 [src/main/resources/application.yml](/Users/makebukepurou/Desktop/food_wx-1/src/main/resources/application.yml)：

```yaml
tencent:
  map:
    enabled: ${TENCENT_MAP_ENABLED:false}
    key: ${TENCENT_MAP_KEY:}
    base-url: ${TENCENT_MAP_BASE_URL:https://apis.map.qq.com}
```

说明：

- 没配置 `TENCENT_MAP_KEY` 时，会自动回退到原有贝叶斯推荐流程
- 配好 Key 后，会额外叠加附近餐饮 POI 命中度和更可信的位置权重

### 7. 配置微信小程序登录

项目已接入微信小程序登录，链路为：

- 小程序端调用 `wx.login`
- 后端调用微信 `code2Session`
- 用返回的 `openid` 查找或创建本地用户

启动后端前需要配置：

```bash
export WECHAT_MINI_APP_ID=你的小程序AppID
export WECHAT_MINI_APP_SECRET=你的小程序AppSecret
```

对应配置项在 [src/main/resources/application.yml](/Users/makebukepurou/Desktop/food_wx-1/src/main/resources/application.yml)：

```yaml
wechat:
  mini-program:
    app-id: ${WECHAT_MINI_APP_ID:wxa39950b899a41ebd}
    secret: ${WECHAT_MINI_APP_SECRET:}
```

说明：

- `AppID` 可以和 [project.config.json](/Users/makebukepurou/Desktop/food_wx-1/project.config.json) 保持一致
- `AppSecret` 需要去微信公众平台获取，不能写死在小程序前端
- 小程序登录身份来自 `wx.login -> code2Session -> openid`
- 用户头像与昵称建议使用小程序官方授权控件完成补全

## 已完成功能

- 首页推荐列表
- 分类浏览
- 搜索店铺
- 店铺详情展示
- 收藏 / 取消收藏
- 发布评论
- 我的收藏
- 我的评论

## 本次已完成的 UI 优化

- 统一全局色板、圆角、阴影、留白和按钮风格
- 重做首页 Hero 区与推荐卡片层级
- 优化详情页封面、摘要区和底部操作栏
- 搜索页、分类页、个人中心、评论页统一现代化风格
- 修复 `myreviews.wxml` 末尾残留测试文本
- 调整全局导航栏与 tabBar 颜色，和新视觉保持一致

## 已验证事项

- `mvn -q -DskipTests package` 可通过
- 后端可在本地 `mysql` 上启动并响应推荐接口

## 当前待处理事项

- 启动本地 MySQL，让 Spring Boot 接口真正跑起来
- 在微信开发者工具中手动确认开启 CLI 服务端口
- 完成一次开发者工具真机或模拟器联调

## 建议的下一步

1. 先启动 MySQL
2. 再运行 `mvn spring-boot:run`
3. 然后在微信开发者工具里打开项目
4. 最后用首页、搜索、详情、收藏、评论链路做一遍完整联调
