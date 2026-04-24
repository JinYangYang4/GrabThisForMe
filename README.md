# GrabThisForMe

一个基于 Android（Kotlin）的代取/跑腿 + 社区 + 消息 + 商城 + 二手综合应用原型。

##  功能概览

- 首页双模式：`我来取` / `找人取`
- 订单能力：任务列表、订单详情、创建订单、历史订单
- 社区能力：帖子流、帖子详情、评论/回复、点赞、分享
- 消息能力：会话列表、好友/群组、聊天（文本/相册发图/拍照发图/图片预览）
- 商城能力：店铺页、分类商品、加购与已选商品面板、店铺搜索
- 二手能力：二手商品列表、分类浏览、发布入口
- 账户能力：登录、注册、找回密码、切换账号
- 个人中心：我的喜欢、我的话题、设置、个人信息与账户安全页面
- 搜索能力：商品/社区/好友群聊/店铺 多场景搜索历史（本地持久化）
- 活动能力：签到日历、优惠券列表、券商城

##  技术栈

- 语言：Kotlin（JVM 11）
- UI：XML + DataBinding（已开启 Compose 支持）
- 架构：MVVM（ViewModel + LiveData）
- 导航：Navigation Component + Safe Args + 自定义 `keep_state_fragment` Navigator
- 依赖注入：Hilt
- 本地存储：Room（搜索历史、用户）
- 组件：RecyclerView / ViewPager2 / Material Components
- 图片与图表：Glide、MPAndroidChart

##  项目结构

```text
app/
  src/main/
    java/com/example/grabthisforme/
      activity/                # 各业务页面（home/community/information/my/login/misc）
      model/                   # 数据模型与 mock 数据
      di/                      # Hilt + Room 注入模块
      extension/               # 扩展函数
    res/
      layout/                  # XML 布局
      navigation/              # nav_graph / nav_new / nav_graph_login / nav_graph_home
      drawable/                # 图标与背景资源
```

##  快速开始

### 1) 环境要求

- Android Studio（建议最新稳定版）
- JDK 11
- Android SDK（`compileSdk = 36`）

### 2) 拉取项目

```bash
git clone <your-repo-url>
cd GrabThisForMe
```

### 3) 构建与运行

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

然后在 Android Studio 中运行 `app` 模块到模拟器或真机。

##  权限说明

- 相册读取：用于聊天选图
- 相机：用于聊天拍照发送
- 文件提供器（FileProvider）：用于拍照后安全分享图片 URI

##  数据现状

当前项目以原型验证为主：

- 主要业务数据（订单、帖子、商品、会话等）大多为本地 mock 数据
- 已接入 Room 本地持久化：
  - `search` 表：多场景搜索历史
  - `user` 表：本地账号与当前账号状态

##  Roadmap（建议）

- 接入真实后端 API（登录、订单、帖子、聊天）
- WebSocket/IM 实时消息与未读同步
- 订单流转状态机与支付/结算
- 图片上传与媒体服务
- 完善埋点、监控、崩溃与性能优化

##  Contributing

欢迎提 Issue 和 PR。建议先开 Issue 讨论改动方向，再提交实现。

## 📄 License

当前仓库暂未声明 License。

