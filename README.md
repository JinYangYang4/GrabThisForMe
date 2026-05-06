# GrabThisForMe

一个基于 Android Kotlin 的校园代取/跑腿 + 社区 + 商城 + 二手 + 消息应用原型。

## 快速上下文（给新同事/新对话AI）
- 技术形态：以 `XML + DataBinding + MVVM` 为主，`Compose` 已开启但不是主要 UI 实现。
- 入口 Activity：`MainActivity`（主流程）+ `LoginActivity`（登录流程）。
- 导航策略：主底栏与零散业务页面分成两个 `NavHostFragment`（同屏切换）。
- 数据层现状：核心业务大量使用 mock，部分模块已做 Room 持久化与分层改造。
- 最近新增规范：`docs/skills/` 下维护可复用 UI skill 文档（表单生成统一风格）。

## 功能概览
- 首页双模式：`我来取` / `找人取`
- 订单：创建订单、任务列表、订单详情、历史
- 社区：帖子流、帖子详情、评论/回复、点赞、分享
- 消息：会话列表、好友/群组、聊天（文本/相册/拍照/图片预览）
- 商城：店铺页、商品列表、加购面板、店铺搜索
- 二手：二手商品列表、分类浏览、发布二手商品
- 账户：登录、注册、找回密码、切换账号
- 个人中心：我的喜欢、我的话题、设置、个人信息、账户安全
- 搜索：商品/社区/好友群聊/店铺 多场景搜索历史

## 环境与构建
### 基础要求
- Android Studio（建议稳定版）
- JDK 11（项目编译目标）
- Android SDK：`compileSdk=36`，`targetSdk=36`，`minSdk=24`

### 本地运行
```powershell
.\gradlew.bat assembleDebug
.\gradlew.bat test
```

如果出现 SDK 路径错误，在项目根目录 `local.properties` 配置：
```properties
sdk.dir=你的AndroidSdk绝对路径
```

## 核心架构
### UI 层
- 组织方式：`activity/.../view` + `viewModel`
- 状态驱动：`ViewModel + LiveData + DataBinding`
- 主要布局目录：`app/src/main/res/layout`

### 导航层
- 主导航（底栏四大页）：`app/src/main/res/navigation/nav_graph.xml`
- 登录导航：`app/src/main/res/navigation/nav_graph_login.xml`
- 杂项业务导航（创建订单/帖子详情/聊天等）：`app/src/main/res/navigation/nav_new.xml`
- 关键实现：`MainActivity` 中维护两个 `NavHostFragment`，通过 `openNewFragment` 状态控制显示。

### 数据层
- DI：Hilt（`@HiltAndroidApp` + `di/DatabaseModule.kt`）
- Room 数据库：`model/AppDataBase/AppDatabase.kt`
  - DB 名称：`grab_this_for_me_core_db`
  - 迁移策略：`fallbackToDestructiveMigration()`
  - 当前版本：`version = 9`
- 已接入 DAO：`SearchDao`、`UserDao`、`GoodsDao`、`MessageDao`、`ConversationDao`、`OrderDao`
- DataStore：`model/user/data/datastore/UserSettingsDataStore.kt`

## 模型分层与目录约定
模型目录位于 `app/src/main/java/com/example/grabthisforme/model/`，当前采用“模块内分层”：
- `data/dto`：网络/传输模型
- `data/entity`：Room 实体
- `data/dao`：数据库访问
- `domain`：业务模型
- `mapper`：模型转换（常见链路：`dto -> domain <-> entity`）

当前已较完整分层的模块（示例）：
- `goods`
- `secondhandGoods`
- `user`
- `store`（DTO + Domain，不含 Entity）
- `conversation`
- `messageContent`
- `Order`
- `Post`

注意：目录名存在历史大小写混用（例如 `model/Post`、`model/Order`），但 Kotlin 包名使用小写 `model.post`、`model.order`。新增代码请保持包名小写并遵循现有模块结构。

## 关键入口文件索引
- Application：`app/src/main/java/com/example/grabthisforme/activity/myApp/MyApp.kt`
- 主 Activity：`app/src/main/java/com/example/grabthisforme/activity/mainactivity/view/MainActivity.kt`
- 登录 Activity：`app/src/main/java/com/example/grabthisforme/activity/LoginActivity/view/LoginActivity.kt`
- DB 定义：`app/src/main/java/com/example/grabthisforme/model/AppDataBase/AppDatabase.kt`
- Hilt DB 注入：`app/src/main/java/com/example/grabthisforme/di/DatabaseModule.kt`

## UI Skill 文档（批量生成同风格页面）
位于 `docs/skills/`：
- 总索引：`docs/skills/README.md`
- UI 分类：`docs/skills/ui/README.md`
- 表单分类：`docs/skills/ui/forms/README.md`
- 创建类表单规范：`docs/skills/ui/forms/create-data-entry/SKILL.md`
- 认证页规范（登录/注册/找回）：`docs/skills/ui/forms/auth-login-register-recover/SKILL.md`

后续新增 skill 建议结构：
- `docs/skills/ui/<子域>/<skill-id>/SKILL.md`

## 权限说明
- 相册读取：聊天选图
- 相机：拍照发送
- FileProvider：拍照 URI 安全共享

## 当前阶段说明
- 项目偏原型验证，部分数据与流程仍为 mock。
- 存在一定历史代码风格差异（命名、目录层级、大小写），重构按模块渐进进行。

## License
仓库当前未声明开源 License。
