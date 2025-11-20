# 医院预约系统 - Hospital Appointment System

## 项目介绍

医院预约系统是一个基于前后端分离架构的综合性医疗预约平台，旨在简化医院预约流程，提高就医效率。系统支持患者在线预约挂号、医生排班管理、管理员后台管理等核心功能，并提供健康论坛模块促进医患交流。

## 技术栈

### 前端技术栈
- **Vue 3**: 渐进式JavaScript框架
- **Vue Router 4**: 路由管理
- **Vuex 4**: 状态管理
- **Element Plus**: UI组件库
- **Axios**: HTTP请求库
- **ECharts**: 数据可视化
- **Vite**: 构建工具
- **Sass**: CSS预处理器

### 后端技术栈
- **Spring Boot 3.2.3**: Java后端框架
- **Spring Security**: 安全认证授权
- **MyBatis**: ORM框架
- **Spring Data JPA**: 数据访问层
- **MySQL 8.0.33**: 关系型数据库
- **JWT**: 令牌认证
- **Lombok**: 减少样板代码
- **Swagger/OpenAPI**: API文档

## 功能特性

### 患者端功能
- 用户注册与登录
- 快速预约挂号
- 一键预约功能
- 我的预约管理
- 个人中心设置
- 健康论坛浏览与参与
- 帖子收藏

### 医生端功能
- 预约管理（查看、处理预约请求）
- 个人排班管理
- 健康论坛参与

### 管理员功能
- 用户管理（学生、教师、医生账户管理）
- 科室管理
- 医生管理
- 排班管理
- 预约管理
- 论坛内容管理
- 分类管理

## 项目结构

```
├── appointment-vue/         # 前端Vue项目
│   ├── public/              # 静态资源
│   ├── src/                 # 源代码
│   │   ├── api/             # API请求
│   │   ├── assets/          # 静态资源
│   │   ├── components/      # 公共组件
│   │   ├── layout/          # 布局组件
│   │   ├── router/          # 路由配置
│   │   ├── store/           # Vuex状态管理
│   │   ├── styles/          # 全局样式
│   │   ├── utils/           # 工具函数
│   │   ├── views/           # 页面组件
│   │   ├── App.vue          # 根组件
│   │   └── main.js          # 入口文件
│   ├── vite.config.js       # Vite配置
│   └── package.json         # 项目依赖
├── appointment/             # 后端Spring Boot项目
│   ├── src/                 # 源代码
│   │   ├── main/java/com/example/appointment/
│   │   │   ├── controller/  # 控制器
│   │   │   ├── service/     # 服务层
│   │   │   ├── mapper/      # MyBatis映射器
│   │   │   ├── entity/      # 实体类
│   │   │   ├── dto/         # 数据传输对象
│   │   │   ├── config/      # 配置类
│   │   │   ├── security/    # 安全相关
│   │   │   └── utils/       # 工具类
│   │   └── main/resources/  # 配置文件
│   ├── pom.xml              # Maven依赖
│   └── mvnw                 # Maven包装器
└── README.md                # 项目说明文档
```

## 核心模块

### 1. 预约管理模块
- 支持多步骤预约和一键快速预约
- 实时检查医生可用性
- 预约状态跟踪（待确认、已确认、已完成、已取消等）
- 支持按日期、医生、科室筛选预约

### 2. 用户管理模块
- 基于角色的权限控制系统（患者、医生、管理员）
- 用户信息管理
- JWT令牌认证
- 密码加密与安全登录

### 3. 排班管理模块
- 医生排班设置
- 时间段管理
- 可用性检查

### 4. 健康论坛模块
- 帖子发布与浏览
- 分类管理
- 收藏功能
- 评论系统
- 内容审核（管理员）

## 安装部署

### 前端部署

1. 安装依赖
```bash
cd appointment-vue
npm install
```

2. 开发环境运行
```bash
npm run dev
```

3. 构建生产版本
```bash
npm run build
```

4. 预览构建结果
```bash
npm run preview
```

### 后端部署

1. 配置数据库
   - 创建MySQL数据库 `hospital1`
   - 配置 `application.yml` 中的数据库连接信息

2. 运行应用
```bash
cd appointment
mvn spring-boot:run
```

3. 构建JAR包
```bash
mvn clean package
```

4. 运行JAR包
```bash
java -jar target/appointment-0.0.1-SNAPSHOT.jar
```

## API文档

后端项目集成了Swagger/OpenAPI，启动应用后可通过以下地址访问API文档：
```
http://localhost:8080/swagger-ui/index.html
```

## 环境要求

### 前端
- Node.js 16.x 或更高版本
- npm 8.x 或更高版本

### 后端
- JDK 17
- Maven 3.6+ 或更高版本
- MySQL 8.0 或更高版本

## 配置说明

### 前端配置
主要配置文件位于 `appointment-vue/.env` 文件（需自行创建）：
```
VITE_APP_API_BASE_URL=http://localhost:8080/api
```

### 后端配置
主要配置文件位于 `appointment/src/main/resources/application.yml`：
- 数据库连接配置
- JWT密钥配置
- 日志级别配置
- MyBatis配置

## 注意事项

1. 默认数据库配置：
   - 数据库名：`hospital1`
   - 用户名：`root`
   - 密码：`root`
   - 端口：`3306`

2. JWT密钥需修改为安全的随机字符串（至少32个字符）

3. 生产环境部署前建议修改所有默认密码

4. 前端默认运行端口：`5173`

5. 后端默认运行端口：`8080`

## 系统架构

系统采用典型的前后端分离架构：
- 前端：单页应用（SPA）基于Vue 3 + Element Plus构建
- 后端：RESTful API基于Spring Boot构建
- 数据库：MySQL关系型数据库
- 认证：JWT无状态认证机制

## License

[MIT](LICENSE)

## 作者

联系作者qq：2244548157

## 联系方式

如有问题或建议，请通过GitHub Issues反馈。
