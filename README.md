# GrabThisForMe

GrabThisForMe 是一个面向校园生活场景的 Android Kotlin 原型项目，覆盖跑腿订单、校园社区、商品商城、二手交易、聊天消息、好友/聊群、搜索和个人中心等流程。项目当前重点是功能闭环验证、Room 本地数据链路完善、领域模型拆分和页面体验优化，部分页面仍处于 mock 数据向 Repository/Room 迁移的阶段。

## 项目概览

主要功能模块：

- 首页：跑腿入口、接单/发单场景、常用入口、店铺和用户相关展示。
- 订单：创建订单、订单列表、订单详情、历史订单。
- 社区：帖子流、发布帖子、帖子详情、评论、回复、点赞和分享入口。
- 商城：商品列表、商品详情、店铺页、店铺搜索、店主后台、商品创建和商品分类。
- 二手：二手商品列表、分类浏览、二手商品详情和交易信息。
- 消息：会话列表、聊天页、图片选择、拍照、好友列表、聊群、用户详情和聊群详情。
- 搜索：商品、社区、联系人/聊群/会话、店铺等多场景搜索。
- 用户：登录、注册、找回密码、账号切换、个人信息、设置、收藏、喜欢。

## 技术栈

- 语言：Kotlin
- UI：XML Layout、DataBinding、RecyclerView、ConstraintLayout、Material Components
- 局部新 UI：Jetpack Compose
- 架构：渐进式 MVVM，按 `view / viewModel / data / domain / mapper` 分层推进
- 导航：Jetpack Navigation、Safe Args、多 NavHostFragment
- 依赖注入：Hilt
- 本地数据库：Room
- 本地偏好：DataStore Preferences
- 图片加载：Glide
- 图片查看：PhotoView
- 图表：MPAndroidChart
- 弹窗选择器：Android-PickerView
- 布局辅助：FlexboxLayout
- Compose 液态玻璃效果：`io.github.kyant0:backdrop`

当前主要版本：

- `compileSdk = 36`
- `targetSdk = 36`
- `minSdk = 24`
- Gradle Wrapper：`8.13`
- Kotlin：`2.3.10`
- Hilt：`2.57.2`
- Room：`2.8.4`
- Navigation：`2.7.x`
- Backdrop：`1.0.6`
- Java / Kotlin JVM Target：`11`

## 快速运行

Windows：

```powershell
.\gradlew.bat assembleDebug
.\gradlew.bat :app:compileDebugKotlin
.\gradlew.bat :app:kaptDebugKotlin
```

macOS / Linux：

```bash
./gradlew assembleDebug
./gradlew :app:compileDebugKotlin
./gradlew :app:kaptDebugKotlin
```

建议使用 Android Studio 打开项目。当前项目使用 Room、Hilt、DataBinding 和 Safe Args，修改数据库实体、DAO、依赖注入或导航参数后，优先运行 `:app:kaptDebugKotlin` 或 `:app:compileDebugKotlin` 做快速校验。

## 目录结构

主要源码路径：

```text
app/src/main/java/com/example/grabthisforme/
├── activity/      Activity、Fragment、页面 Adapter、页面 ViewModel
├── di/            Hilt 依赖注入模块
├── extension/     Kotlin 扩展方法
├── model/         Room、Repository、领域模型、Mapper
├── ui/            通用 UI、Compose 组件、自定义 View
└── util/          工具类
```

主要资源路径：

```text
app/src/main/res/
├── layout/        XML 页面和 RecyclerView item
├── navigation/    Navigation 图
├── drawable/      图标、背景、shape
├── values/        颜色、字符串、主题
└── xml/           FileProvider、备份配置
```

当前导航图：

- `nav_graph.xml`：主 Tab 级页面。
- `nav_graph_home.xml`：首页内部子导航。
- `nav_graph_login.xml`：登录注册流程。
- `nav_new.xml`：零散业务页面，如聊天、详情、店铺、设置等。

## 架构约定

项目整体采用渐进式 MVVM：

- `Fragment / Activity` 负责页面绑定、点击事件、RecyclerView 初始化和系统交互。
- `ViewModel` 负责页面状态、输入校验、UI 事件转业务请求。
- `Repository` 负责屏蔽数据来源，当前主要连接 Room，也保留部分 mock fallback。
- `Dao / Entity` 负责 Room 持久化。
- `domain` 放业务模型，`mapper` 负责 Entity、DTO、Domain 之间转换。
- 页面或 Adapter 如果只需要领域模型的一部分字段，应优先创建 `ui_model`，不要直接消费过大的领域模型。

新增功能时优先沿用当前模块结构。例如 `model/goods`、`model/store`、`model/conversation` 已经具备 `data / domain / mapper / repository` 等结构，不要在页面层直接操作 Room。

## 数据层

Room 数据库入口：

- `app/src/main/java/com/example/grabthisforme/model/AppDataBase/AppDatabase.kt`
- 数据库名：`grab_this_for_me_core_db`
- 当前版本：`34`
- 当前迁移策略：`fallbackToDestructiveMigration()`

Hilt 数据库注入入口：

- `app/src/main/java/com/example/grabthisforme/di/DatabaseModule.kt`

已注册的主要 DAO：

- `SearchDao`
- `UserDao`
- `GoodsDao`
- `MessageDao`
- `ConversationDao`
- `ConversationUserStateDao`
- `ConversationRelationDao`
- `OrderDao`
- `PostDao`
- `PostStatsDao`
- `UserRelationDao`
- `StoreRelationDao`
- `StoreDao`
- `FriendAndGroupDao`

完整表说明维护在：

- `docs/skills/data/database-table-catalog.md`

建表、拆表、领域模型和 UI 模型规范维护在：

- `docs/skills/data/README.md`
- `docs/skills/data/table-and-domain-model-guidelines/SKILL.md`
- `docs/skills/data/page-ui-model-decomposition/SKILL.md`
- `docs/skills/data/room-normalize-relations/SKILL.md`

## 核心模型模块

`model/` 下的主要模块：

- `user`：用户账号、资料、统计、当前登录用户、用户偏好。
- `Post`：社区帖子、评论、回复、帖子统计。
- `Order`：跑腿订单。
- `goods`：商品基础信息、价格、UI 状态、库存状态。
- `store`：店铺信息。
- `relation`：用户点赞关系、店铺商品分类关系、会话参与者关系。
- `secondhandGoods`：二手商品和交易信息。
- `conversation`：会话基础信息和当前用户会话状态。
- `message`：聊天消息内容。
- `friendAndGroup`：好友关系、聊群、用户与聊群关系。

历史目录中存在大小写混用，例如 `Post`、`Order` 目录首字母大写。新增代码时优先保持当前模块已有 package 风格，并参考同模块现有写法。

## 会话、未读和隐藏规则

当前会话模型采用“会话共用，用户状态独立”的设计：

- `conversation` 表保存会话本身，例如 `conversationId`、会话类型、目标 id、最后消息和最后时间。
- `conversation_participant` 表保存会话与参与用户的关系，用于判断某个用户是否参与该会话。
- `conversation_user_state` 表保存每个用户自己的会话状态，主键是 `(conversationId, userId)`。
- `unreadCount` 是每个用户独立的未读数量。
- `isHidden` 是每个用户独立的隐藏状态。

当前规则：

- 发送消息后，更新 `conversation.lastMessageId` 和 `lastTime`。
- 发送消息后，只给非发送者的参与用户 `unreadCount + 1`。
- 如果用户主动在隐藏会话中发送消息，则该用户的 `isHidden` 会恢复为 `false`。
- 如果用户收到新未读消息，则该用户的 `isHidden` 会恢复为 `false`。
- 打开聊天页或长按会话选择“标记为已读”时，只把当前用户的 `unreadCount` 清零，不会自动重新隐藏会话。
- 会话列表只显示当前用户参与且 `isHidden = false` 的会话。

相关入口：

- `model/conversation/data/entity/ConversationEntity.kt`
- `model/conversation/data/dao/ConversationUserStateDao.kt`
- `model/conversation/data/repository/ConversationRepository.kt`
- `model/message/data/repository/MessageRepository.kt`
- `activity/informationFragment/view/FragmentConversation.kt`
- `ui/menu/BubbleArrowMenuView.kt`
- `ui/menu/BubbleArrowMenuPopup.kt`

## UI 与组件

通用 UI 组件放在 `app/src/main/java/com/example/grabthisforme/ui/`：

- `ui/liquidglass`：液态玻璃按钮、底部 Tab、弹窗和交互辅助。
- `ui/goods`：商品/二手商品页面可复用的分类和筛选组件。
- `ui/menu`：通用菜单类自定义 View，例如会话长按气泡菜单。

页面样式规范和布局技能文档维护在：

- `docs/skills/ui/README.md`
- `docs/skills/ui/layouts/nested-scroll-view-layout/SKILL.md`
- `docs/skills/ui/forms/create-information-form-pages-ui/SKILL.md`
- `docs/skills/ui/settings/settings-page-pattern/SKILL.md`

## 关键入口文件

- Application：`app/src/main/java/com/example/grabthisforme/activity/myApp/MyApp.kt`
- 主 Activity：`app/src/main/java/com/example/grabthisforme/activity/mainactivity/view/MainActivity.kt`
- 登录 Activity：`app/src/main/java/com/example/grabthisforme/activity/LoginActivity/view/LoginActivity.kt`
- Hilt 数据库模块：`app/src/main/java/com/example/grabthisforme/di/DatabaseModule.kt`
- Room 数据库：`app/src/main/java/com/example/grabthisforme/model/AppDataBase/AppDatabase.kt`
- Manifest：`app/src/main/AndroidManifest.xml`

## 权限

当前声明权限：

- `READ_MEDIA_IMAGES`：Android 13+ 图片读取。
- `CAMERA`：聊天、发布等场景拍照。
- `READ_EXTERNAL_STORAGE / WRITE_EXTERNAL_STORAGE`：兼容旧版本 Android。
- `FileProvider`：拍照图片 URI 安全共享。

## 给 AI 和新协作者的阅读建议

如果要理解或修改某个功能，建议按这个顺序阅读：

1. 先看对应 `Fragment / Activity`，确认 UI 事件和 ViewModel 绑定。
2. 再看对应 `ViewModel`，确认页面状态和业务入口。
3. 接着看 `Repository`，确认数据来自 Room、DataStore 还是 mock。
4. 最后看 `Dao / Entity / Mapper`，确认数据库结构和转换逻辑。

常见路径示例：

- 聊天会话：`FragmentConversation -> InformationViewModel -> ConversationRepository -> ConversationDao / ConversationUserStateDao`
- 聊天消息：`FragmentChat -> FragmentChatViewModel -> MessageRepository -> MessageDao`
- 商品数据：`GoodsRepository -> GoodsDao -> Goods*Entity`
- 店铺数据：`StoreFragment / StoreViewModel -> StoreRepository -> StoreDao`
- 社区帖子：`PostDetailFragment -> PostDetailViewModel -> PostRepository -> PostDao / PostStatsDao`
- 用户/好友/聊群：`UserRepository / FriendAndGroupRepository / ContactDirectoryRepository -> UserDao / FriendAndGroupDao`

## 当前注意事项

- 项目仍处于原型阶段，部分页面仍有 mock 数据或 UI 层临时数据。
- 数据库使用 `fallbackToDestructiveMigration()`，开发期升级 schema 会清库。
- Gradle 中 Navigation、Glide、MPAndroidChart 等依赖存在重复声明，当前不影响编译，但后续可以整理。
- 新增表、删除表、修改字段、调整表语义后，需要同步更新 `docs/skills/data/database-table-catalog.md`。
- 新增页面或 Adapter 时，如果只展示领域模型的一部分信息，优先创建 `ui_model`。
