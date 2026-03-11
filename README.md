# Local Food Recommendation System

美食推荐系统 - 毕业设计项目

## 版本历史

- **V1**: 基础版本 - 用户、店铺、评论、收藏 CRUD
- **V2**: 功能完善版本 - 分类管理、搜索优化、评论删除
- **V3**: 开发中...

## 构建状态

| 状态 | 详情 |
|------|------|
| 编译 | ✅ SUCCESS |
| 测试 | ✅ SUCCESS |
| 打包 | ✅ SUCCESS |
| 构建时间 | 2026-03-11 |

## 技术栈

- **后端**: Java 17, Spring Boot 3, Maven
- **数据库**: MySQL 8
- **ORM**: Spring Data JPA
- **其他**: Lombok

## 项目结构

```
food_wx-1/
├── pom.xml
├── src/
│   └── main/
│       ├── java/
│       │   └── com/
│       │       └── foodrecommendation/
│       │           ├── FoodRecommendationApplication.java
│       │           ├── common/
│       │           │   └── Result.java
│       │           ├── config/
│       │           ├── controller/
│       │           │   ├── UserController.java
│       │           │   ├── ShopController.java
│       │           │   ├── ReviewController.java
│       │           │   ├── CollectionController.java
│       │           │   └── CategoryController.java
│       │           ├── dto/
│       │           │   ├── LoginRequest.java
│       │           │   ├── ReviewRequest.java
│       │           │   └── CollectionRequest.java
│       │           ├── entity/
│       │           │   ├── User.java
│       │           │   ├── Shop.java
│       │           │   ├── Review.java
│       │           │   ├── Collection.java
│       │           │   └── Category.java
│       │           ├── repository/
│       │           ├── service/
│       │           │   └── impl/
│       │           └── vo/
│       │               ├── ShopVO.java
│       │               └── ReviewVO.java
│       └── resources/
│           └── application.yml
├── sql/
│   ├── init.sql
│   └── v2_upgrade.sql
└── README.md
```

## 快速开始

### 1. 准备 MySQL 数据库

创建数据库 `fooddb`：

```sql
CREATE DATABASE fooddb CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 2. 配置数据库连接

编辑 `src/main/resources/application.yml`，确保数据库连接信息正确：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/fooddb
    username: root
    password: root
```

### 3. 启动项目

```bash
cd food_wx-1
mvn spring-boot:run
```

启动后，系统会自动：
- 创建分类数据 (火锅、川菜、粤菜等)
- 插入 12 条测试店铺数据

### 4. API 测试

使用 Postman 或 curl 测试以下接口：

#### 用户模块

**模拟登录**
- POST `http://localhost:8080/api/user/login`
- Body (JSON):
```json
{
  "nickname": "张三"
}
```

**获取用户信息**
- GET `http://localhost:8080/api/user/{id}`

#### 店铺模块

**获取店铺列表**
- GET `http://localhost:8080/api/shops`

**获取店铺详情**
- GET `http://localhost:8080/api/shops/{id}`

**按分类查询店铺**
- GET `http://localhost:8080/api/shops/category/{categoryId}`

**搜索店铺**
- GET `http://localhost:8080/api/shops/search?keyword=火锅`

#### 分类模块

**获取所有分类**
- GET `http://localhost:8080/api/categories`

#### 评论模块

**获取店铺评论**
- GET `http://localhost:8080/api/reviews?shopId=1`

**获取用户评论**
- GET `http://localhost:8080/api/reviews/user/{userId}`

**发布评论**
- POST `http://localhost:8080/api/reviews`
- Body (JSON):
```json
{
  "userId": 1,
  "shopId": 1,
  "rating": 5,
  "content": "味道很好，服务也很不错！"
}
```

**删除评论**
- DELETE `http://localhost:8080/api/reviews/{id}`

#### 收藏模块

**收藏店铺**
- POST `http://localhost:8080/api/collections`
- Body (JSON):
```json
{
  "userId": 1,
  "shopId": 1
}
```

**取消收藏**
- DELETE `http://localhost:8080/api/collections?userId=1&shopId=1`

**获取收藏列表**
- GET `http://localhost:8080/api/collections?userId=1`

## 响应格式

所有接口返回标准 JSON 格式：

```json
{
  "code": 200,
  "message": "success",
  "data": { ... }
}
```

错误响应：

```json
{
  "code": 400,
  "message": "错误信息",
  "data": null
}
```

## V2 版本更新内容

### 新增功能

1. **分类管理**
   - 支持按分类查询店铺
   - 分类数据自动初始化

2. **搜索优化**
   - 支持空关键字搜索（返回空列表）
   - 模糊匹配店铺名称

3. **评论管理**
   - 新增删除评论功能
   - 删除后自动更新店铺评分和评论数

4. **数据一致性**
   - 添加评论自动更新店铺评分
   - 删除评论自动同步店铺数据

### 数据库升级 (V1 -> V2)

如果从 V1 升级，需执行 `sql/v2_upgrade.sql`：

```bash
mysql -u root -p fooddb < sql/v2_upgrade.sql
```

## V3 计划功能

- [ ] 推荐算法集成
- [ ] Redis 缓存
- [ ] JWT 鉴权
- [ ] 评价图片上传
- [ ] 距离计算

## 注意事项

- 所有 Controller 已添加 `@CrossOrigin` 注解，支持微信小程序跨域请求
- 推荐使用 MySQL 8.0+ 以支持 `utf8mb4` 字符集
- 评分保留一位小数，范围 0-5
