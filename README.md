# 在线购物系统 (Online Shopping System)

## 项目简介

这是一个基于 Spring Boot + Vue 3 开发的全栈在线购物系统，提供完整的电商功能，包括用户管理、商品管理、购物车、订单处理等核心模块。

## 技术栈

### 后端技术
- **框架**: Spring Boot 3.1.5
- **数据库**: MySQL 8.0
- **ORM**: Spring Data JPA + Hibernate
- **安全**: Spring Security + JWT
- **构建工具**: Maven 3.9.4
- **Java版本**: JDK 17+

### 前端技术
- **框架**: Vue 3 (Composition API)
- **状态管理**: Vuex 4
- **路由**: Vue Router 4
- **UI框架**: Bootstrap 5
- **HTTP客户端**: Axios
- **构建工具**: Vite 5

## 功能模块

### 用户端功能
- ✅ 用户注册/登录
- ✅ 个人中心（信息修改、头像上传）
- ✅ 商品浏览与搜索
- ✅ 购物车管理
- ✅ 订单创建与查询
- ✅ 订单历史记录

### 管理端功能
- ✅ 管理员仪表盘（数据统计、图表展示）
- ✅ 用户管理（查看、编辑、删除）
- ✅ 商品管理（增删改查、库存管理）
- ✅ 分类管理
- ✅ 订单管理（订单处理、状态更新）

## 项目结构

```
OnlineShopping/
├── backend/                          # 后端项目
│   └── OnlineShopping/
│       ├── src/
│       │   └── main/
│       │       ├── java/
│       │       │   └── com/example/onlineshopping/
│       │       │       ├── OnlineShoppingApplication.java  # 启动类
│       │       │       ├── config/                         # 配置类
│       │       │       │   ├── JwtConfig.java             # JWT配置
│       │       │       │   ├── SecurityConfig.java        # 安全配置
│       │       │       │   └── WebConfig.java             # Web配置
│       │       │       ├── controller/                     # 控制器
│       │       │       │   ├── AuthController.java        # 认证接口
│       │       │       │   ├── UserController.java        # 用户接口
│       │       │       │   ├── ProductController.java     # 商品接口
│       │       │       │   ├── CartController.java        # 购物车接口
│       │       │       │   ├── OrderController.java       # 订单接口
│       │       │       │   └── FileUploadController.java  # 文件上传接口
│       │       │       ├── entity/                         # 实体类
│       │       │       ├── repository/                     # 数据访问层
│       │       │       ├── service/                        # 业务逻辑层
│       │       │       ├── dto/                            # 数据传输对象
│       │       │       └── security/                       # 安全组件
│       │       └── resources/
│       │           ├── application.yml                     # 配置文件
│       │           └── application.properties
│       ├── uploads/avatars/                                # 头像上传目录
│       └── pom.xml                                         # Maven配置
│
└── frontend/                         # 前端项目
		└── frontend/
				├── src/
				│   ├── components/
				│   │   ├── admin/                                  # 管理端组件
				│   │   │   ├── AdminDashboard.vue                 # 管理仪表盘
				│   │   │   ├── ProductManagement.vue              # 商品管理
				│   │   │   ├── UserManagement.vue                 # 用户管理
				│   │   │   ├── CategoryManagement.vue             # 分类管理
				│   │   │   └── OrderManagement.vue                # 订单管理
				│   │   ├── user/                                   # 用户端组件
				│   │   │   ├── UserDashboard.vue                  # 用户首页
				│   │   │   ├── ProductList.vue                    # 商品列表
				│   │   │   ├── ShoppingCart.vue                   # 购物车
				│   │   │   ├── OrderHistory.vue                   # 订单历史
				│   │   │   └── PersonalCenter.vue                 # 个人中心
				│   │   └── common/                                 # 公共组件
				│   │       ├── Header.vue                          # 顶部导航
				│   │       ├── Footer.vue                          # 底部信息
				│   │       └── Sidebar.vue                         # 侧边栏
				│   ├── router/
				│   │   └── index.js                                # 路由配置
				│   ├── store/
				│   │   └── index.js                                # Vuex状态管理
				│   ├── utils/
				│   │   ├── request.js                              # Axios封装
				│   │   └── auth.js                                 # 认证工具
				│   ├── views/                                      # 视图页面
				│   │   ├── Login.vue                               # 登录页
				│   │   ├── Register.vue                            # 注册页
				│   │   ├── UserLayout.vue                          # 用户端布局
				│   │   └── AdminLayout.vue                         # 管理端布局
				│   ├── assets/
				│   │   └── admin-common.css                        # 管理端通用样式
				│   ├── App.vue                                     # 根组件
				│   └── main.js                                     # 入口文件
				├── package.json                                    # 依赖配置
				└── vite.config.js                                  # Vite配置
```

## 环境要求

- **JDK**: 17 或更高版本
- **Maven**: 3.6+
- **Node.js**: 16.0+
- **MySQL**: 8.0+
- **浏览器**: Chrome/Firefox/Edge 最新版

## 安装与运行

### 1. 数据库配置

创建数据库并导入初始数据：

```sql
-- 创建数据库
CREATE DATABASE online_shopping CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- 使用数据库
USE online_shopping;

-- 导入数据表（运行 data.sql）
SOURCE /path/to/data.sql;
```

### 2. 后端配置与启动

#### 修改数据库连接

编辑 `backend/OnlineShopping/src/main/resources/application.yml`：

```yaml
spring:
	datasource:
		url: jdbc:mysql://localhost:3306/online_shopping?useSSL=false&serverTimezone=Asia/Shanghai&characterEncoding=utf8
		username: root          # 修改为你的数据库用户名
		password: 123456        # 修改为你的数据库密码
```

#### 启动后端服务

**方式一：使用 IDEA 运行（推荐）**

1. 使用 IntelliJ IDEA 打开 `backend/OnlineShopping` 项目
2. 等待 Maven 依赖下载完成
3. 找到 `src/main/java/com/example/onlineshopping/OnlineShoppingApplication.java`
4. 右键点击该文件，选择 `Run 'OnlineShoppingApplication'`
5. 或者直接点击类名旁边的绿色运行按钮 ▶️

**方式二：使用命令行运行**

```bash
# 进入后端目录
cd backend/OnlineShopping

# 清理并编译项目
mvn clean package -DskipTests

# 启动服务
java -jar target/onlineshopping-1.0.0.jar
```

后端服务将在 `http://localhost:8080` 启动。

### 3. 前端配置与启动

```bash
# 进入前端目录
cd frontend/frontend

# 安装依赖
npm install

# 启动开发服务器
npm run dev
```

前端服务将在 `http://localhost:5174` 启动。

## 默认账号

### 管理员账号
- **用户名**: admin
- **密码**: admin123

### 普通用户账号
- **用户名**: user
- **密码**: user123

## API 接口说明

### 认证接口
- `POST /api/auth/login` - 用户登录
- `POST /api/auth/register` - 用户注册

### 用户接口
- `GET /api/users/me` - 获取当前用户信息
- `PUT /api/users/{id}` - 更新用户信息
- `GET /api/users` - 获取所有用户（管理员）
- `DELETE /api/users/{id}` - 删除用户（管理员）

### 商品接口
- `GET /api/products` - 获取商品列表
- `GET /api/products/{id}` - 获取商品详情
- `POST /api/products` - 创建商品（管理员）
- `PUT /api/products/{id}` - 更新商品（管理员）
- `DELETE /api/products/{id}` - 删除商品（管理员）

### 分类接口
- `GET /api/categories` - 获取分类列表
- `POST /api/categories` - 创建分类（管理员）
- `PUT /api/categories/{id}` - 更新分类（管理员）
- `DELETE /api/categories/{id}` - 删除分类（管理员）

### 购物车接口
- `GET /api/cart` - 获取购物车
- `POST /api/cart/items` - 添加商品到购物车
- `PUT /api/cart/items/{id}` - 更新购物车商品数量
- `DELETE /api/cart/items/{id}` - 删除购物车商品

### 订单接口
- `GET /api/orders` - 获取订单列表
- `GET /api/orders/{id}` - 获取订单详情
- `POST /api/orders` - 创建订单
- `PUT /api/orders/{id}/status` - 更新订单状态（管理员）

### 文件上传接口
- `POST /api/upload/avatar` - 上传头像

### 核心功能说明

### JWT 认证
系统使用 JWT (JSON Web Token) 进行身份认证：
- 登录成功后，服务器返回 JWT token
- 前端将 token 存储在 localStorage 中
- 每次请求时，在 HTTP Header 中携带 token
- 后端通过过滤器验证 token 的有效性

### 密码安全
系统采用 BCrypt 加密算法保护用户密码：
- **新注册用户**：密码自动使用 BCrypt 加密后存储
- **测试账号**：admin 和 user1 的密码也使用 BCrypt 加密存储
- **加密特性**：单向哈希、自动加盐、防暴力破解
- **登录验证**：使用 Spring Security 的 AuthenticationManager 进行密码验证
- 数据库中存储的密码格式示例：`$2a$10$XQw1f2g3h4j5k6l7m8n9o0pQRs...`

### 权限管理
系统支持两种角色：
- **ROLE_USER**: 普通用户，可以浏览商品、管理购物车、创建订单
- **ROLE_ADMIN**: 管理员，拥有所有权限，可以管理用户、商品、分类、订单

### 文件上传
- 支持头像上传功能
- 文件大小限制：2MB
- 支持的格式：jpg, jpeg, png, gif
- 上传路径：`backend/OnlineShopping/uploads/avatars/`
- 访问路径：`http://localhost:8080/uploads/avatars/{filename}`
- 上传超时：30秒（针对文件上传优化）

### 跨域配置
后端已配置 CORS，允许前端跨域访问：
- 允许的源：`http://localhost:5173`, `http://localhost:5174`
- 允许的方法：GET, POST, PUT, DELETE
- 允许携带凭证

## 常见问题

### 1. 后端启动失败
- 检查 JDK 版本是否为 17+
- 检查 MySQL 是否正常运行
- 检查数据库连接配置是否正确
- 检查 8080 端口是否被占用

### 2. 前端启动失败
- 检查 Node.js 版本是否为 16.0+
- 删除 `node_modules` 文件夹，重新运行 `npm install`
- 检查 5174 端口是否被占用

### 3. 登录失败
- 检查后端服务是否正常运行
- 检查数据库中是否有用户数据
- 使用默认账号进行测试

### 4. 图片上传失败
- 检查 `uploads/avatars` 目录是否存在
- 检查文件大小是否超过 2MB
- 检查文件格式是否为图片
- 查看浏览器控制台和后端日志的错误信息

### 5. 跨域问题
- 确认前端和后端都在本地运行
- 检查后端 CORS 配置
- 清除浏览器缓存后重试

## 开发说明

### 添加新的 API 接口
1. 在 `controller` 包中创建新的 Controller
2. 在 `service` 包中创建对应的 Service
3. 在 `repository` 包中创建对应的 Repository
4. 在 `SecurityConfig` 中配置接口的访问权限

### 添加新的前端页面
1. 在 `src/components` 或 `src/views` 中创建新的 Vue 组件
2. 在 `src/router/index.js` 中添加路由配置
3. 在相应的布局文件中添加导航链接

### 样式规范
- 管理端页面使用统一的样式文件 `assets/admin-common.css`
- 使用 Bootstrap 5 工具类进行布局
- 主题色：渐变色 `#667eea` → `#764ba2`
- 圆角：16px
- 阴影：多层次阴影效果

## 部署说明

### 后端部署
1. 修改 `application.yml` 中的数据库配置为生产环境配置
2. 配置 JWT 密钥（`jwt.secret`）
3. 打包项目：`mvn clean package -DskipTests`
4. 上传 jar 包到服务器
5. 使用 `nohup java -jar onlineshopping-1.0.0.jar &` 后台运行

### 前端部署
1. 修改 API 基础路径为生产环境地址
2. 构建生产版本：`npm run build`
3. 将 `dist` 目录下的文件部署到 Nginx 或其他 Web 服务器
4. 配置 Nginx 反向代理到后端服务

## 项目截图

### 用户端
- 登录页面：简洁的登录界面，支持注册跳转
- 商品列表：网格布局展示商品，支持搜索和分类筛选
- 购物车：清晰的购物车界面，支持数量调整和删除
- 个人中心：个人信息管理和头像上传

### 管理端
- 仪表盘：数据统计卡片、图表、最近订单、热销商品
- 商品管理：商品列表、添加/编辑商品、库存管理
- 用户管理：用户列表、用户信息编辑
- 订单管理：订单列表、订单状态更新、订单详情查看

## 更新日志

### v1.0.0 (2025-11-27)
- ✅ 完成基础的用户注册登录功能
- ✅ 完成商品管理模块
- ✅ 完成购物车功能
- ✅ 完成订单管理功能
- ✅ 完成管理员后台界面美化
- ✅ 完成头像上传功能
- ✅ 优化了文件上传超时问题

## 技术支持

如有问题或建议，请通过以下方式联系：
- 提交 Issue
- 发送邮件

## 许可证

本项目仅供学习交流使用。

---

**开发时间**: 2025年11月
**最后更新**: 2025年11月27日
