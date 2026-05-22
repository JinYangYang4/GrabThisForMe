# GrabThisForMe

GrabThisForMe 是一个面向校园生活场景的 Android Kotlin 原型项目，覆盖跑腿订单、社区帖子、校园商城、二手交易、聊天消息、搜索和个人中心等流程。项目当前重点是功能验证、数据层本地化和模块化重构，部分页面仍保留 mock 数据。

## 项目概览

主要功能模块：

- 首页：跑腿入口、接单/发单场景、最近用户和店铺展示。
- 订单：创建订单、订单列表、订单详情、历史订单。
- 社区：帖子流、发布帖子、帖子详情、评论、回复、点赞、分享入口。
- 商城：店铺页、商品列表、店铺搜索、店主后台、商品创建。
- 二手：二手商品列表、分类浏览、发布二手商品。
- 消息：会话列表、好友/群组、聊天、图片选择、拍照和图片预览。
- 搜索：商品、社区、好友/群聊、店铺等多场景搜索历史。
- 用户：登录、注册、找回密码、账号切换、个人信息、设置、收藏/喜欢。

## 技术栈

- 语言：Kotlin
- UI：XML Layout、DataBinding、RecyclerView、ConstraintLayout、Material Components
- 局部新 UI：Jetpack Compose
- 架构：MVVM，按 `view / viewModel / data / domain / mapper` 分层推进
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
- Android Gradle Plugin：`8.11.2`
- Kotlin：`2.3.10`
- Hilt：`2.57.2`
- Room：`2.8.4`
- Navigation：主要使用 `2.7.x`
- Backdrop：`1.0.6`

## 快速运行

Windows:

```powershell
.\gradlew.bat assembleDebug
.\gradlew.bat :app:compileDebugKotlin
.\gradlew.bat test
```

macOS / Linux:

```bash
./gradlew assembleDebug
./gradlew :app:compileDebugKotlin
./gradlew test
```

推荐 JDK 11。项目使用 Android Studio 打开最省事。

## 目录结构

主要源码路径：

```text
app/src/main/java/com/example/grabthisforme/
├── activity/      页面、Fragment、Activity、Adapter、ViewModel
├── di/            Hilt 依赖注入模块
├── extension/     扩展方法
├── model/         数据模型、Room、Repository、Mapper
└── ui/            通用 UI 和 Compose 组件
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

## 架构约定

项目整体采用渐进式 MVVM：

- `Fragment / Activity` 负责页面绑定、点击事件、RecyclerView 初始化和系统交互。
- `ViewModel` 负责页面状态、输入校验、UI 事件转业务请求。
- `Repository` 负责屏蔽数据来源，当前主要连接 Room，也会在部分模块提供 mock fallback。
- `Dao / Entity` 负责 Room 持久化。
- `domain` 放业务模型，`mapper` 负责 Entity、DTO、Domain 之间转换。

新增功能时优先沿用当前模块已有结构。例如 `model/Post` 中已经包含 `data / domain / mapper`，不要在页面层直接操作 Room。

## 导航结构

主界面有多个导航图：

- `nav_graph.xml`：主 Tab 级页面，如首页、社区、消息、我的。
- `nav_graph_home.xml`：首页内部子导航。
- `nav_graph_login.xml`：登录注册流程。
- `nav_new.xml`：零散业务页，例如订单详情、店铺、帖子详情、设置等。

`MainActivity` 中维护了主流程和零散业务流两个 `NavHostFragment`。跨业务页跳转优先使用 Safe Args 的 `Directions`，尤其是需要参数的详情页。

## 数据层

Room 数据库入口：

- `model/AppDataBase/AppDatabase.kt`
- 数据库名：`grab_this_for_me_core_db`
- 当前版本：`17`
- 当前迁移策略：`fallbackToDestructiveMigration()`

Hilt 注入入口：

- `di/DatabaseModule.kt`

已接入 Room 的主要 DAO：

- `SearchDao`
- `UserDao`
- `GoodsDao`
- `MessageDao`
- `ConversationDao`
- `OrderDao`
- `PostDao`
- `StoreDao`

当前用户偏好使用：

- `model/user/data/datastore/UserSettingsDataStore.kt`

## 核心模型模块

`model/` 下的主要模块：

- `user`：用户账号、资料、当前登录用户、喜欢列表。
- `Post`：社区帖子、帖子评论缓存、点赞状态同步。
- `Order`：跑腿订单。
- `goods`：商品基础信息、价格、UI 状态、库存状态。
- `store`：店铺信息、店铺商品绑定。
- `secondhandGoods`：二手商品和交易信息。
- `conversation`：会话列表。
- `messageContent`：聊天消息内容。
- `friendAndGroup`：好友和群组相关模型。

历史目录中存在大小写混用，例如 `Post`、`Order` 目录大写，但 Kotlin package 使用小写 `model.post`、`model.order`。新增代码时优先保持 package 小写，并参考同模块现有写法。

## 社区与点赞数据流

社区帖子详情当前数据流：

```text
PostDetailFragment
  -> PostDetailViewModel
  -> PostRepository
  -> PostDao / Room
```

详情页进入时：

- 根据 Safe Args 中的 `postId` 加载对应帖子。
- 评论只做一次初始读取，后续页面显示由 ViewModel 的本地 LiveData 更新。
- 新增评论/回复后，ViewModel 先更新本地列表，再异步写回 Room。
- 点赞状态由当前用户是否已经喜欢该 `postId` 决定。
- 点赞/取消点赞会同时更新 `UserLikeEntity.likedPostIdsJson` 和 `PostEntity.likeCount`。

相关入口：

- `activity/fragment_misc/postDetailFragment/view/PostDetailFragment.kt`
- `activity/fragment_misc/postDetailFragment/viewModel/PostDetailViewModel.kt`
- `model/Post/data/repository/PostRepository.kt`
- `model/Post/data/dao/PostDao.kt`
- `model/user/data/repository/UserRepository.kt`

## 用户喜欢模块

用户喜欢数据保存在 `user_like` 表中：

- `likedPostIdsJson`
- `likedStoreIdsJson`
- `likedGoodsIdsJson`

Domain 模型为 `UserLike`，挂在 `User.likes` 上。Repository 已提供：

- `setPostLiked(postId, liked)`
- `setStoreLiked(storeId, liked)`
- `setGoodsLiked(goodsId, liked)`

目前帖子详情已经接入 post 点赞。Store/Goods 的喜欢按钮有数据接口，但部分页面还没完全接上真实显示。

## 关键入口文件

- Application：`app/src/main/java/com/example/grabthisforme/activity/myApp/MyApp.kt`
- 主 Activity：`app/src/main/java/com/example/grabthisforme/activity/mainactivity/view/MainActivity.kt`
- 登录 Activity：`app/src/main/java/com/example/grabthisforme/activity/LoginActivity/view/LoginActivity.kt`
- Hilt 数据库模块：`app/src/main/java/com/example/grabthisforme/di/DatabaseModule.kt`
- Room 数据库：`app/src/main/java/com/example/grabthisforme/model/AppDataBase/AppDatabase.kt`
- Manifest：`app/src/main/AndroidManifest.xml`

## 权限

项目当前声明了：

- `READ_MEDIA_IMAGES`：Android 13+ 图片读取。
- `CAMERA`：聊天、发布等场景拍照。
- `READ_EXTERNAL_STORAGE / WRITE_EXTERNAL_STORAGE`：兼容旧版本 Android。
- `FileProvider`：拍照图片 URI 安全共享。

## 给 AI 和新协作者的阅读建议

如果要理解或修改某个功能，建议按这个顺序读：

1. 先看对应 Fragment，确认 UI 事件和 ViewModel 绑定。
2. 再看对应 ViewModel，确认页面状态和业务入口。
3. 接着看 Repository，确认数据来自 Room、DataStore 还是 mock。
4. 最后看 Dao、Entity、Mapper，确认数据库结构和转换逻辑。

常见路径示例：

- 帖子详情：`PostDetailFragment -> PostDetailViewModel -> PostRepository -> PostDao`
- 用户喜欢：`UserRepository -> UserDao -> UserLikeEntity -> UserLike`
- 店铺详情：`StoreFragment / StoreViewModel -> StoreRepository -> StoreDao`
- 商品数据：`GoodsRepository -> GoodsDao -> Goods*Entity`

## 当前注意事项

- 这是原型阶段项目，仍有部分 mock 数据和真实 Room 数据并存。
- 数据库使用 `fallbackToDestructiveMigration()`，开发期升级 schema 会清库。
- Gradle 中 Navigation 和 Glide 依赖存在重复声明，暂不影响编译，但后续可以整理。
- 多个模块仍在从页面内 mock 迁移到 Repository/Room，新增功能时建议直接走 Repository。
- README 只描述当前项目状态，不代表所有页面都已经完成生产级数据闭环。

