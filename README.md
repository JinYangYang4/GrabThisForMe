# GrabThisForMe

校园场景的 Android Kotlin 原型项目，覆盖跑腿、社区、商城、二手和消息等核心流程。

## 项目现状

- 架构：`MVVM + XML + DataBinding`，部分局部交互已接入 `Jetpack Compose`
- 导航：主流程与零散业务页拆分为两个 `NavHostFragment`
- 数据层：部分流程仍使用 mock 数据，核心模块已接入 `Room + Hilt + DataStore`
- UI 效果：引入 `io.github.kyant0:backdrop`，用于液态玻璃类 Compose 组件
- 当前阶段：以功能验证和模块化重构并行为主

## 功能概览

- 首页双模式：`我来取` / `找人取`
- 订单：创建、列表、详情、历史
- 社区：帖子流、详情、评论回复、点赞、分享
- 消息：会话列表、好友/群组、聊天（文本/相册/拍照/图片预览）
- 商城：店铺页、商品列表、购物车入口、店铺搜索
- 二手：列表、分类浏览、发布流程
- 账号：登录、注册、找回密码、账号切换
- 个人中心：收藏、话题、设置、个人信息、账号安全
- 搜索：商品/社区/好友群聊/店铺等多场景历史搜索

## 开发环境

- Android Studio（建议稳定版）
- JDK 11
- Android SDK：
  - `compileSdk = 36`
  - `targetSdk = 36`
  - `minSdk = 24`
- Gradle Wrapper：`8.13`
- AGP：`8.11.2`
- Kotlin：`2.3.10`
- Hilt：`2.57.2`
- Backdrop：`1.0.6`

## 近期变更

- `kyant.backdrop` 已从本地源码复制切换为 Maven 依赖：`io.github.kyant0:backdrop:1.0.6`
- Kotlin 升级到 `2.3.10`，并迁移到 `compilerOptions` 配置 `jvmTarget`
- Hilt 升级到 `2.57.2`，用于兼容新 Kotlin metadata
- 搜索历史列表已关闭 `RecyclerView.itemAnimator`，减少删除/刷新时的闪动
- 分类管理底部弹窗的 tab 区域已接入 Compose + LiquidBottomTabs

## 快速运行

Windows:

```powershell
.\gradlew.bat assembleDebug
.\gradlew.bat test
```

macOS / Linux:

```bash
./gradlew assembleDebug
./gradlew test
```

## 核心架构

### UI 层

- 目录组织：`activity/.../view` + `.../viewmodel`
- 状态管理：`ViewModel + LiveData + DataBinding`
- 主要布局目录：`app/src/main/res/layout`

### 导航层

- 主导航：`app/src/main/res/navigation/nav_graph.xml`
- 首页子图：`app/src/main/res/navigation/nav_graph_home.xml`
- 登录流：`app/src/main/res/navigation/nav_graph_login.xml`
- 零散业务流：`app/src/main/res/navigation/nav_new.xml`

### 数据层

- 依赖注入：Hilt（`@HiltAndroidApp` + `di/DatabaseModule.kt`）
- 数据库：`model/AppDataBase/AppDatabase.kt`
  - DB 名称：`grab_this_for_me_core_db`
  - 迁移策略：`fallbackToDestructiveMigration()`
  - 当前版本：`version = 16`
- 已注入 DAO：
  - `SearchDao`
  - `UserDao`
  - `GoodsDao`
  - `MessageDao`
  - `ConversationDao`
  - `OrderDao`
  - `PostDao`
  - `StoreDao`
- 本地偏好：`model/user/data/datastore/UserSettingsDataStore.kt`

## 目录约定

主模型目录：`app/src/main/java/com/example/grabthisforme/model/`

当前主要模块：

- `goods`
- `secondhandGoods`
- `user`
- `store`
- `conversation`
- `messageContent`
- `Order`
- `Post`

说明：

- 历史目录名里存在大小写混用（如 `Order`、`Post`）
- Kotlin 包名应保持小写（如 `model.order`、`model.post`）
- 新增代码请优先遵循现有模块内部的 `data / domain / mapper` 分层

## 关键入口文件

- Application：`app/src/main/java/com/example/grabthisforme/activity/myApp/MyApp.kt`
- 主 Activity：`app/src/main/java/com/example/grabthisforme/activity/mainactivity/view/MainActivity.kt`
- 登录 Activity：`app/src/main/java/com/example/grabthisforme/activity/LoginActivity/view/LoginActivity.kt`
- Hilt DB 模块：`app/src/main/java/com/example/grabthisforme/di/DatabaseModule.kt`
- Room DB 定义：`app/src/main/java/com/example/grabthisforme/model/AppDataBase/AppDatabase.kt`
- AndroidManifest：`app/src/main/AndroidManifest.xml`

## 权限说明

- 相册读取：聊天选图
- 相机：拍照发送
- `FileProvider`：拍照 URI 安全共享

## UI Skill 文档

位置：`docs/skills/`

- 总索引：`docs/skills/README.md`
- UI 分类：`docs/skills/ui/README.md`
- 表单分类：`docs/skills/ui/forms/README.md`
- 创建类表单规范：`docs/skills/ui/forms/create-data-entry/SKILL.md`
- 认证页规范：`docs/skills/ui/forms/auth-login-register-recover/SKILL.md`

