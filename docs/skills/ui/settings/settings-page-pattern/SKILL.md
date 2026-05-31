---
name: settings-page-pattern
description: 用于本项目设置页、资料查看页、资料编辑页、账号安全页、聊天背景页的统一 UI 规范与实现模式。覆盖 5 个页面：FragmentSet（设置主页）、FragmentPersonalInformation（个人信息查看）、EditPersonalInformationFragment（个人信息编辑）、FragmentAccountSecurity（账号安全）、FragmentChatBackground（聊天背景）。涵盖 ConstraintLayout + title_bar + NestedScrollView 骨架、卡片分组、交错入场动画、标题栏规范、ViewBinding 管理、底部栏处理与导航跳转。English retrieval keywords: settings page, profile page, personal information, account security, chat background, staggered entrance animation, card group, title bar, NestedScrollView, ViewAnimationUtils, bg_set_group_card.
---

# 设置页面通用模式 Skill

## 目标

统一本项目所有"设置 / 个人资料 / 账号安全"类页面的布局骨架、视觉风格、入场动画和交互习惯。这 5 个页面共用同一套结构，避免各自写法不一致导致的体验割裂。

## 适用范围

本 skill 覆盖以下 5 个页面：

| 页面 | 布局文件 | Fragment 文件 | 类型 |
|---|---|---|---|
| 设置主页 | `fragment_set.xml` | `FragmentSet.kt` | 列表导航型 |
| 个人信息查看 | `fragment_personal_information.xml` | `FragmentPersonalInformation.kt` | 资料展示型 |
| 个人信息编辑 | `fragment_edit_personal_information.xml` | `EditPersonalInformationFragment.kt` | 表单编辑型 |
| 账号安全 | `fragment_account_security.xml` | `FragmentAccountSecurity.kt` | 表单编辑型 |
| 聊天背景 | `fragment_chat_backgroud.xml` | `FragmentChatBackground.kt` | 选项列表型 |

## 何时使用

出现以下需求时，使用本 skill：

- 新建一个"设置"类页面
- 新建一个"个人资料"展示或编辑页
- 调整上述 5 个页面的布局结构、卡片分组或动画
- 为新页面添加交错入场动画
- 统一设置页面的标题栏、背景、滚动区域的写法
- 需要参考设置页面中的导航跳转和底部栏处理方式

## 先读文件

开始修改前，先看三类文件：

1. **参考布局**：上面列出的 5 个 XML 布局文件
2. **参考 Fragment**：上面列出的 5 个 Kotlin Fragment 文件
3. **共享工具**：
   - `com.example.grabthisforme.util.ViewAnimationUtils` — 入场动画工具
   - `com.example.grabthisforme.util.KeyboardScrollHelper` — 键盘避让工具（仅编辑型页面需要）

优先在现有实现基础上统一，不要忽略当前页面已有的 id、绑定关系和交互逻辑。

## 总体结构

所有设置类页面使用同一套框架：

```
ConstraintLayout (bg_create_goods_screen 背景)
├── title_bar (LinearLayout, 位于 NestedScrollView 之外)
│   ├── 返回按钮行：iv_back + 标题文字 + 可选的操作按钮/状态标签
│   └── 可选的摘要卡片（头像区、安全提醒、聊天预览等）
└── NestedScrollView (layout_height="0dp", constraintTop_toBottomOf title_bar)
    └── 内容 LinearLayout (paddingBottom="24dp")
        ├── 卡片组 1 (bg_set_group_card)
        ├── 卡片组 2 (bg_set_group_card)
        └── ...
```

关键约束：
- **title_bar 必须在 NestedScrollView 之外**，保证标题区域不随内容滚动
- **NestedScrollView 使用约束定位**：`height="0dp"` + `Top_toBottomOf="@+id/title_bar"` + `Bottom_toBottomOf="parent"`
- **不要使用 hero 背景**（如 `bg_security_hero`、`bg_chat_background_hero`），改用标准浅色背景 `bg_create_goods_screen`
- 根容器使用 `ConstraintLayout`，不要用 `FrameLayout` 或 `LinearLayout`

## 页面骨架规范

### 1. 根布局模板

所有设置页面统一使用以下根结构：

```xml
<androidx.constraintlayout.widget.ConstraintLayout
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@drawable/bg_create_goods_screen"
    tools:ignore="MissingConstraints">

    <LinearLayout
        android:id="@+id/title_bar"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:paddingTop="36dp">
        <!-- 返回按钮行 + 可选摘要卡片 -->
    </LinearLayout>

    <androidx.core.widget.NestedScrollView
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:clipToPadding="false"
        android:fillViewport="true"
        android:overScrollMode="never"
        app:layout_constraintTop_toBottomOf="@+id/title_bar"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintStart_toStartOf="parent">

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="vertical"
            android:paddingBottom="24dp">
            <!-- 卡片组 -->
        </LinearLayout>
    </androidx.core.widget.NestedScrollView>
</androidx.constraintlayout.widget.ConstraintLayout>
```

### 2. 标题栏

标题栏由两部分组成：**必须的返回按钮行** + **可选的摘要卡片**。

#### 返回按钮行（必须）

```xml
<LinearLayout
    android:layout_width="match_parent"
    android:layout_height="42dp"
    android:layout_marginHorizontal="16dp"
    android:gravity="center_vertical"
    android:orientation="horizontal">

    <ImageView
        android:id="@+id/iv_back"
        android:layout_width="34dp"
        android:layout_height="34dp"
        android:background="@drawable/bg_drawer_top_pill"
        android:padding="8dp"
        android:src="@drawable/ic_back" />

    <TextView
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:layout_marginStart="12dp"
        android:layout_weight="1"
        android:text="页面标题"
        android:textColor="@color/black"
        android:textSize="22sp"
        android:textStyle="bold" />

    <!-- 可选：操作按钮或状态标签，使用 bg_drawer_top_pill 背景 -->
</LinearLayout>
```

**标题文字规范：**
- 颜色统一使用 `@color/black` 或暗色（`#1D6F5E` 等绿色系可用于资料编辑页）
- 字号 22sp，加粗
- 左侧 margin 12dp（与返回按钮间距）

**操作按钮/状态标签（可选）：**
- 使用 `bg_drawer_top_pill` 作为背景，高度 32-34dp
- 文字 12sp，加粗
- paddingStart/End 各 12dp
- 示例：FragmentSet 的"已登录"标签、FragmentPersonalInformation 的"编辑"按钮、FragmentAccountSecurity 的"完成"按钮、FragmentChatBackground 的"预览中"标签

#### 摘要卡片（可选）

位于返回按钮行下方，与标题栏属于同一个 `LinearLayout`。参考以下页面的做法：

**FragmentSet 风格 — 大图标 + 标题 + 描述 + 状态胶囊：**
```xml
<LinearLayout
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_marginTop="18dp"
    android:layout_marginHorizontal="16dp"
    android:layout_marginBottom="5dp"
    android:background="@drawable/bg_set_group_card"
    android:elevation="2dp"
    android:gravity="center_vertical"
    android:orientation="horizontal"
    android:padding="16dp">

    <ImageView
        android:layout_width="58dp"
        android:layout_height="58dp"
        android:background="@drawable/bg_circle"
        android:backgroundTint="#FFFFFFFF"
        android:clipToOutline="true"
        android:padding="10dp"
        android:src="@drawable/ic_set" />

    <LinearLayout ...>
        <TextView ... 主标题 18sp bold />
        <TextView ... 描述 12sp />
        <TextView ... 状态胶囊芯片 bg_set_status_chip />
    </LinearLayout>
</LinearLayout>
```

**FragmentPersonalInformation 风格 — 头像 + 姓名/账号/签名：**
- 使用 `bg_info_avatar_ring` 包裹圆形头像
- 信息区垂直排列：姓名（20sp bold）、账号（12sp）、签名（12sp）

**FragmentAccountSecurity / FragmentChatBackground 风格 — 纯文本摘要：**
- 不需要大图标或头像
- 使用 `bg_set_group_card` 背景 + `elevation="2dp"`
- 16dp padding
- 标题 16sp bold (`#172A2B`) + 内容 12-13sp (`#6A7B7C`)

### 3. 卡片系统

所有内容分组放在 `NestedScrollView` 内，按业务拆分为多张卡片。

#### 卡片容器

每张卡片使用以下标准属性：

```xml
<LinearLayout
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_marginStart="14dp"
    android:layout_marginTop="5dp"
    android:layout_marginEnd="14dp"
    android:background="@drawable/bg_set_group_card"
    android:elevation="2dp"
    android:orientation="vertical"
    android:padding="16dp">
    <!-- 卡片内容 -->
</LinearLayout>
```

**卡片间距规则：**
- 第一张卡片 `marginTop="5dp"`（与 title_bar 的间距）
- 后续卡片 `marginTop="12dp"`（卡片之间）
- 左右 margin 统一 `14dp`

#### 卡片内列表项

每个可点击的列表项使用以下模板：

```xml
<androidx.constraintlayout.widget.ConstraintLayout
    android:id="@+id/item_xxx"
    android:layout_width="match_parent"
    android:layout_height="76dp"
    android:layout_marginTop="14dp"
    android:background="@drawable/bg_order_list_surface"
    android:foreground="?attr/selectableItemBackground"
    android:paddingStart="14dp"
    android:paddingEnd="14dp">

    <ImageView
        android:layout_width="42dp"
        android:layout_height="42dp"
        android:background="@drawable/bg_my_quick_icon_misty_mint"
        android:padding="10dp"
        android:src="@drawable/ic_xxx"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toTopOf="parent" />

    <TextView
        android:id="@+id/tv_xxx"
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:layout_marginStart="12dp"
        android:text="标题"
        android:textColor="@android:color/black"
        android:textSize="16sp"
        android:textStyle="bold"
        app:layout_constraintEnd_toStartOf="@+id/xxx_more"
        app:layout_constraintStart_toEndOf="@id/icon_xxx"
        app:layout_constraintTop_toTopOf="@id/icon_xxx" />

    <TextView
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:layout_marginTop="4dp"
        android:text="描述文字"
        android:textColor="#6A7B7C"
        android:textSize="12sp"
        app:layout_constraintEnd_toStartOf="@+id/xxx_more"
        app:layout_constraintStart_toStartOf="@id/tv_xxx"
        app:layout_constraintTop_toBottomOf="@id/tv_xxx" />

    <ImageView
        android:id="@+id/xxx_more"
        android:layout_width="18dp"
        android:layout_height="18dp"
        android:alpha="0.5"
        android:src="@drawable/ic_more"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintTop_toTopOf="parent" />
</androidx.constraintlayout.widget.ConstraintLayout>
```

**列表项间距**：
- 第一个列表项 `marginTop="14dp"`（与卡片标题的间距）
- 后续列表项 `marginTop="10dp"`

#### 图标背景色轮换

图标容器背景色按项轮换，避免单调：

| 顺序 | Drawable |
|---|---|
| 第 1 项 | `bg_my_quick_icon_misty_mint` |
| 第 2 项 | `bg_my_quick_icon_misty_blue` |
| 第 3 项 | `bg_my_quick_icon_misty_soft_pink` |
| 第 4 项 | `bg_my_quick_icon_soft_apricot` |

#### 特殊卡片

**退出登录 / 危险操作卡片：**
- 使用 `bg_set_danger_card` 替代 `bg_order_list_surface`
- 标题文字颜色 `#B13D30`（红色警告）
- 描述文字颜色 `#B16A5B`
- 搭配 `bg_drawer_logout_card` 图标容器

#### 纯文本行（无图标）

某些页面使用简单横向文字行展示信息（如 FragmentPersonalInformation 的基础资料卡）。模板：

```xml
<LinearLayout
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:layout_marginTop="10dp"
    android:background="@drawable/bg_order_list_surface"
    android:gravity="center_vertical"
    android:orientation="horizontal"
    android:padding="14dp">

    <TextView
        android:layout_width="0dp"
        android:layout_height="wrap_content"
        android:layout_weight="1"
        android:text="标签"
        android:textColor="#172A2B"
        android:textSize="14sp"
        android:textStyle="bold" />

    <TextView
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:text="值"
        android:textColor="#6A7B7C"
        android:textSize="14sp" />
</LinearLayout>
```

### 4. 页面类型

设置页面分为两种类型，区别在于 NestedScrollView 内的内容：

#### 列表导航型（FragmentSet、FragmentChatBackground）

内容卡片内是**可点击的列表项**，点击后跳转到子页面。每个列表项带有图标、标题、描述、右箭头。

#### 资料展示型（FragmentPersonalInformation）

内容卡片内是**只读的标签-值行**。不涉及输入，只做信息展示。

#### 表单编辑型（EditPersonalInformationFragment、FragmentAccountSecurity）

内容卡片内包含**输入框或输入区域**。需要额外的键盘避让处理。对于这类页面，参考 `docs/skills/ui/forms/create-information-form-pages-ui/SKILL.md` 中的输入区规范和键盘避让规范。

## 入场动画

### 动画工具方法

所有设置页面统一使用 `ViewAnimationUtils.animateStaggeredEntrance()`。

**文件位置：** `com.example.grabthisforme.util.ViewAnimationUtils`

**方法签名：**
```kotlin
@JvmStatic
fun animateStaggeredEntrance(
    vararg views: View,
    duration: Long = 260L,
    startDelayInterval: Long = 55L,
    translationY: Float = 24f
)
```

**动画效果：** 每个视图从透明（alpha=0）且向下偏移 24dp 开始，以默认 55ms 为间隔依次淡入上滑到原位，每个动画持续 260ms。

### 调用位置

**必须在 `onViewCreated` 末尾调用**，在 `initView()` / `initClickEvents()` / `initObserve()` 等初始化方法之后。

### 动画目标

只对 **NestedScrollView 内的内容卡片** 执行动画，不对 `title_bar` 及其内部的摘要卡片做动画。

各页面传入的视图：

| 页面 | 动画目标 |
|---|---|
| FragmentSet | `binding.accountSettings`, `binding.privacySettings`, `binding.accountOperations` |
| FragmentPersonalInformation | `binding.infoCard` |
| EditPersonalInformationFragment | `binding.accountInfoCard`, `binding.profileCard`, `binding.saveButtonCard` |
| FragmentAccountSecurity | `binding.llToast`, `binding.passwordFormCard` |
| FragmentChatBackground | `binding.backgroundSourceCard` |

### 调用模板

```kotlin
override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    initView()
    initObserve()
    // 放在 onViewCreated 末尾
    ViewAnimationUtils.animateStaggeredEntrance(binding.card1, binding.card2)
}
```

**注意：**
1. 确保传入的视图在布局中已经设置了 `android:id`（没有 id 的卡片需要先添加 id）
2. 只传入需要动画的卡片，不要传入 title_bar 内的视图
3. 如果只有 1 张卡片也要传入（动画只有 0ms 延迟，效果依然正常）

## Fragment Kotlin 模板

### 基本模板（无 ViewModel 的数据绑定）

适用于 FragmentSet、FragmentChatBackground 等不需要 ViewModel 的页面：

```kotlin
class FragmentXxx : Fragment() {
    private var _binding: FragmentXxxBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentXxxBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initClickEvents()
        ViewAnimationUtils.animateStaggeredEntrance(binding.card1, binding.card2)
    }

    private fun initClickEvents() {
        binding.ivBack.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
        // 其他点击事件...
    }

    override fun onResume() {
        super.onResume()
        (requireActivity() as MainActivity).innerBottomBar()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
```

### ViewModel + 数据绑定模板

适用于 FragmentAccountSecurity（有 ViewModel 和双向数据绑定）等页面：

```kotlin
@AndroidEntryPoint
class FragmentXxx : Fragment() {
    private var _binding: FragmentXxxBinding? = null
    private val binding get() = _binding!!
    private val viewModel: XxxViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentXxxBinding.inflate(inflater, container, false)
        binding.lifecycleOwner = viewLifecycleOwner
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewData()
        initClickListener()
        initObserve()
        ViewAnimationUtils.animateStaggeredEntrance(binding.card1, binding.card2)
    }

    // ...

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
```

### 关键约定

1. **binding 生命周期**：始终在 `onDestroyView` 中将 `_binding` 置为 `null`
2. **底部栏**：设置类页面在 `onResume` 中调用 `(requireActivity() as MainActivity).innerBottomBar()` 隐藏底部导航
3. **返回按钮**：统一 `parentFragmentManager.popBackStack()` 返回上一页
4. **DataBinding**：有 ViewModel 的页面须在 `onCreateView` 中设置 `binding.lifecycleOwner = viewLifecycleOwner` 和 `binding.viewModel = viewModel`

## 导航规范

设置页面之间的跳转全部通过 `MainActivity.intentToMiscFragment()` 方法，传入导航 action id。

```kotlin
(requireActivity() as MainActivity).intentToMiscFragment(R.id.action_xxx_to_yyy)
```

当前设置页面的导航关系：

```
FragmentSet（设置主页）
├── -> FragmentPersonalInformation（个人信息查看）
│       └── -> EditPersonalInformationFragment（编辑个人信息）
├── -> FragmentAccountSecurity（账号安全）
└── -> FragmentChatBackground（聊天背景设置）
```

**导航 action 命名规范：** `action_[源页面Fragment名]_to_[目标页面Fragment名]`

## 页面差异化原则

骨架统一，内容差异化：

| 页面 | 独特之处 |
|---|---|
| FragmentSet | 摘要卡片含大图标 + 状态胶囊；分为三个卡片组（账号信息、体验设置、账号操作）；危险操作卡片使用红色主题 |
| FragmentPersonalInformation | 摘要卡片含头像环 + 姓名/账号/签名；内容卡片使用纯文本标签-值行 |
| EditPersonalInformationFragment | 摘要卡片提示"完善个人资料"；使用 `TextInputLayout` 输入框 + `RadioGroup` 性别选择；有保存按钮卡片；需 `KeyboardScrollHelper` 键盘避让 |
| FragmentAccountSecurity | 摘要卡片为安全提醒（`ll_toast`）；表单卡片含密码输入（`textPassword` inputType）；有"完成"提交按钮 |
| FragmentChatBackground | 摘要卡片为聊天预览区（聊天气泡模拟）；内容卡片为三个选项（推荐背景、相册选择、拍照选择） |

## 资源与命名

优先复用已有资源，不要为相似效果重复创建。

**背景资源速查：**

| Drawable | 用途 |
|---|---|
| `bg_create_goods_screen` | 所有设置页面的根背景 |
| `bg_set_group_card` | 卡片组容器背景（圆角 + 阴影） |
| `bg_order_list_surface` | 卡片内列表项背景 |
| `bg_drawer_top_pill` | 标题栏返回按钮 + 操作按钮背景 |
| `bg_circle` | 圆形裁剪（头像、图标） |
| `bg_set_status_chip` | 绿色状态胶囊标签 |
| `bg_set_danger_card` | 危险操作项背景（浅红） |
| `bg_drawer_logout_card` | 退出登录图标背景 |
| `bg_primary_pill_green` | 主操作按钮背景（绿色圆角） |
| `bg_chip_mint` | 薄荷色选项 Chip |
| `bg_chip_warm` | 暖色选项 Chip |
| `bg_info_avatar_ring` | 头像装饰环（仅个人资料页） |

**图标资源速查：**

| Drawable | 用途 |
|---|---|
| `ic_back` | 返回按钮 |
| `ic_more` | 右箭头（列表项末尾） |
| `ic_set` | 设置图标 |
| `ic_my` | 个人资料图标 |
| `ic_show` | 账号安全图标 |
| `ic_information` | 聊天/信息图标 |
| `ic_logout` | 退出登录图标 |
| `ic_photo_album` | 相册图标 |
| `ic_camera` | 相机图标 |
| `ic_arrow_right` | 小右箭头 |

**新增资源时建议：**
- 文件名使用英文
- 名称体现用途前缀（如 `bg_`、`ic_`）
- 名称体现语义（如 `set_group_card`、`security_notice`）

## 修改流程

每次修改设置页面时，按以下顺序执行：

1. **确认页面类型**：列表导航型 / 资料展示型 / 表单编辑型
2. **对齐骨架**：确保 `ConstraintLayout` + `title_bar` 外置 + `NestedScrollView` 约束定位
3. **调整卡片分组**：检查卡片数量、间距、背景是否一致
4. **调整标题栏**：返回按钮行 + 摘要卡片的风格是否与同类页面一致
5. **添加/确认入场动画**：在 `onViewCreated` 末尾调用 `ViewAnimationUtils.animateStaggeredEntrance()`，确保目标视图均有 id
6. **处理键盘**（仅编辑型页面）：使用 `KeyboardScrollHelper` 处理键盘避让
7. **编译验证**：执行 `:app:compileDebugKotlin`

## 验证

按项目通用构建与测试 skill 执行验证：

- `docs/skills/platform/android-build-and-test/SKILL.md`

对于普通 UI / 布局改动，至少执行：

- `:app:compileDebugKotlin`

如需进一步验证 Kotlin 逻辑：

- `:app:testDebugUnitTest`

（注意当前环境可能存在 loopback 限制导致单元测试失败）

## 放置约定

这个 skill 放在项目内：

- `docs/skills/ui/settings/settings-page-pattern/`

原因：

- 设置页面是一类独立的 UI 模式（区别于表单创建页 `ui/forms/`）
- 5 个页面共享同一套骨架和动画方式，适合通用 skill 覆盖
- 方便不同智能体直接从仓库内检索
- 方便上层 `README.md` 建立检索树与优先级说明
