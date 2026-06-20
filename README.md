# GrabThisForMe 前端项目

## 项目简介

GrabThisForMe 是一个面向校园生活场景的 Android Kotlin 项目，覆盖跑腿订单、校园社区、商品商城、二手交易、聊天消息、好友与群组、搜索和个人中心等功能。

当前项目重点在于：

- 逐步从原型页面迁移到更稳定的数据链路
- 统一 `view / viewModel / data / domain / mapper` 分层
- 用 Room、Repository、DataStore 承接本地数据
- 为后续前后端联调逐步接入 Retrofit 与远程仓库

## 项目位置

前端项目目录：

```text
D:\projects\GrabThisForMe\xin\GrabThisForMe
```

后端项目目录：

```text
D:\projects\GrabThisForMe\GrabThisForMe-Backend\GrabThisForMe
```

如果任务涉及后端代码、联调或接口修改，除了当前前端仓库外，还需要同时查看后端项目。

## 主要功能模块

- 首页：跑腿入口、接单/发单场景、常用入口、店铺和用户展示
- 订单：创建订单、订单列表、订单详情、历史订单
- 社区：帖子流、发帖、帖子详情、评论、回复、点赞、分享
- 商城：商品列表、商品详情、店铺页、店铺搜索、店主管理
- 二手：二手商品列表、分类浏览、商品详情、交易信息
- 消息：会话列表、聊天页面、图片选择、拍照、好友、群组
- 搜索：商品、社区、联系人、群组、店铺等多场景搜索
- 用户：登录、注册、找回密码、账号切换、个人信息、设置

## 技术栈

- 语言：Kotlin
- UI：XML Layout、DataBinding、RecyclerView、ConstraintLayout、Material Components
- 局部新 UI：Jetpack Compose
- 架构：渐进式 MVVM
- 导航：Jetpack Navigation、Safe Args、多 NavHostFragment
- 依赖注入：Hilt
- 本地数据库：Room
- 本地偏好：DataStore Preferences
- 网络：Retrofit、OkHttp
- 图片加载：Glide
- 图片查看：PhotoView

当前关键版本：

- `compileSdk = 36`
- `targetSdk = 36`
- `minSdk = 24`
- Gradle Wrapper `8.13`
- Java / Kotlin JVM Target `11`

## 当前架构约定

项目整体按模块组织在 `app/src/main/java/com/example/grabthisforme/model/` 下。

每个业务模块优先采用以下结构：

```text
model/<module>/
├─ data/
│  ├─ local/
│  │  ├─ dao/
│  │  └─ entity/
│  ├─ network/
│  │  ├─ api/
│  │  └─ dto/
│  └─ repository/
├─ domain/
└─ mapper/
```

Repository 层约定：

- `LocalRepository`：只负责本地数据源
- `RemoteRepository`：只负责网络数据源
- `Repository`：统一对上提供业务入口，优先远程，失败再回退本地

页面层约定：

- `Fragment / Activity` 负责页面绑定、事件分发、列表初始化
- `ViewModel` 负责页面状态、输入校验、业务调用
- `ui_model` 只承接页面真正需要展示的字段

## 当前数据链路重点

### 用户与认证

- 登录、注册等请求已经按 `network/auth` 方向接入
- 登录后端会返回 token
- 客户端后续请求需要通过请求头携带 `Authorization: Bearer <token>`

### 评论与回复

近期帖子详情评论/回复链路已经朝以下方向调整：

- 显示时优先使用网络返回 DTO 转成 domain 直接展示
- 本地数据库只作为缓存和兜底，不作为首要显示来源
- 评论和回复分页都使用 `beforeTime` 游标方式
- 回复展开策略：
  - 第一次展开显示 3 条
  - 第二次显示到 5 条
  - 之后每次再增加 7 条
- 评论列表在帖子详情页按游标分页继续加载
- 分页停止优先依据后端 `hasMore`，前端同时保留去重兜底

如果继续修改帖子评论、回复、缓存或分页逻辑，建议先查看：

- `docs/skills/data/dto-backend-remote-repository-flow/SKILL.md`
- `docs/skills/data/repository-data-chain/SKILL.md`
- `docs/skills/platform/frontend-backend-handoff/SKILL.md`

## 目录结构

主要源码目录：

```text
app/src/main/java/com/example/grabthisforme/
├─ activity/     Activity、Fragment、页面 Adapter、页面 ViewModel
├─ di/           Hilt 模块
├─ extension/    Kotlin 扩展
├─ model/        数据层、领域层、Mapper
├─ ui/           通用 UI 组件、自定义 View、Compose 组件
└─ util/         工具类
```

主要资源目录：

```text
app/src/main/res/
├─ layout/
├─ navigation/
├─ drawable/
├─ values/
└─ xml/
```

## 快速编译

推荐在 PowerShell 中执行：

```powershell
$env:JAVA_HOME='D:\Application\java\jdk-21.0.11'
$env:PATH="$env:JAVA_HOME\bin;$env:PATH"
$env:GRADLE_USER_HOME='D:\projects\GrabThisForMe\xin\GrabThisForMe\.gradle-user-home-local'
.\gradlew.bat --no-daemon --console=plain :app:compileDebugKotlin
```

其他常用命令：

```powershell
.\gradlew.bat --no-daemon --console=plain :app:kaptDebugKotlin
.\gradlew.bat --no-daemon --console=plain assembleDebug
```

## 数据库说明

Room 数据库入口：

- `app/src/main/java/com/example/grabthisforme/model/AppDataBase/AppDatabase.kt`

Hilt 数据库注入入口：

- `app/src/main/java/com/example/grabthisforme/di/DatabaseModule.kt`

当前数据库特征：

- 数据库名：`grab_this_for_me_core_db`
- 当前版本请以代码中的 `AppDatabase` 为准
- 目前仍使用 `fallbackToDestructiveMigration()`，开发期升级 schema 可能清库

完整表说明维护在：

- `docs/skills/data/database-table-catalog.md`

## 与后端联调

后端默认端口是：

```text
http://localhost:8080
```

常见接口路径示例：

- 登录/注册：`/api/auth/...`
- 帖子：`/api/posts/...`
- 店铺：`/api/stores/...`
- 用户：`/api/users/...`

如果接口返回：

```json
{
  "code": 40101,
  "message": "Missing bearer token"
}
```

说明当前请求没有携带 token，需要在请求头中加入：

```http
Authorization: Bearer <token>
```

## 当前文档入口

建议先读：

1. `docs/skills/README.md`
2. 对应分类下的 `README` 或具体 `SKILL.md`

重点文档：

- `docs/skills/data/database-table-catalog.md`
- `docs/skills/data/dto-backend-remote-repository-flow/SKILL.md`
- `docs/skills/data/repository-data-chain/SKILL.md`
- `docs/skills/platform/android-build-and-test/SKILL.md`
- `docs/skills/platform/frontend-backend-handoff/SKILL.md`

## 新协作者阅读建议

如果要理解或修改某个功能，推荐按这个顺序阅读：

1. 先看对应 `Fragment / Activity`
2. 再看对应 `ViewModel`
3. 再看对应 `Repository`
4. 最后看 `Dao / Entity / DTO / Mapper / Domain`

例如：

- 帖子详情：`PostDetailFragment -> PostDetailViewModel -> PostRepository`
- 聊天会话：`FragmentConversation -> InformationViewModel -> ConversationRepository`
- 用户认证：登录页面 -> 对应 ViewModel -> `auth` / `user` 相关 Repository

## 当前注意事项

- 项目仍处于持续重构阶段，部分页面仍混有 mock 或过渡数据链路
- 旧模块目录中存在大小写混用，例如 `Post`、`Order`，新增代码时优先保持当前模块既有风格
- 如果新增或调整表结构，需要同步更新 `docs/skills/data/database-table-catalog.md`
- 如果修改前后端联调流程、编译环境、交接规则，需要同步更新 `docs/skills/platform/` 下文档
