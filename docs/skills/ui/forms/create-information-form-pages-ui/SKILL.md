---
name: create-information-form-pages-ui
description: 用于本项目创建页、发布页、信息填写页的统一 UI 规范与改造流程。适用页面包括 `fragment_creat_goods.xml`、`fragment_create_order.xml`、`fragment_create_post.xml`、`fragment_create_secondhand_goods.xml` 及对应 Fragment。覆盖 hero 区、卡片式表单、键盘避让、焦点滚动、插图策略、输入区结构与提交区样式。English retrieval keywords: create form, publish form, data entry form, information entry page, keyboard insets, hero cards, NestedScrollView.
---

# 创建信息填写页面 Skill

## 目标

统一本项目“创建 / 发布 / 填写信息”类页面的布局骨架、视觉节奏、输入区结构和键盘交互，避免每个页面各写一套，导致体验割裂。

这个 skill 优先服务以下页面：

- `app/src/main/res/layout/fragment_creat_goods.xml`
- `app/src/main/res/layout/fragment_create_order.xml`
- `app/src/main/res/layout/fragment_create_post.xml`
- `app/src/main/res/layout/fragment_create_secondhand_goods.xml`

以及它们各自对应的 Fragment 逻辑文件。

## 何时使用

出现以下需求时，优先使用本 skill：

- 美化创建页
- 统一多个信息填写页的风格
- 重做表单结构
- 处理键盘弹出遮挡输入框
- 给创建页增加 banner、hero、插图
- 优化输入框、分组卡片、提交区

## 先读文件

开始修改前，先看两类文件：

1. 对应页面的 XML
2. 对应页面的 Fragment / Binding / Kotlin 逻辑

优先在现有实现基础上统一，不要忽略当前页面已有的 id、绑定关系和交互逻辑。

## 总体结构

所有这类页面优先使用同一套框架：

1. 顶部 hero 区
2. 下方 `NestedScrollView`
3. 滚动区内按业务分组的圆角卡片
4. 独立的提交区或操作区

不要退回成“白底长列表 + 密集输入框”的旧式录入页。

## 页面骨架规范

### 1. Hero 区

hero 区负责建立页面情绪和场景感，通常包含：

- 返回按钮
- 页面标题
- 一句主文案
- 一句辅助说明
- 1 到 2 个轻量标签或状态胶囊

下方主卡片可以轻微上叠到 hero 区，形成更现代的层次感。

### 2. 滚动区

滚动内容统一放在 `NestedScrollView` 内。

`ll_nested` 的职责只保留纵向内容组织，不负责横向留白。

统一规则：

- `ll_nested` 不设置横向 `padding`
- 只保留必要的顶部、底部 `padding`
- 每一张卡片自己设置横向 `margin`

建议：

- 主卡片统一 `layout_marginHorizontal="10dp"`

### 3. 卡片系统

所有信息组优先拆成多张卡片，而不是一张超长大卡片。

卡片应具备：

- 明确圆角
- 轻阴影或轻描边
- 舒适的内边距
- 清楚的标题与辅助文本
- 字段之间稳定的垂直节奏

常见顺序可参考：

1. banner / 氛围卡片
2. 基础信息卡片
3. 价格 / 时效 / 状态卡片
4. 图片 / 附件卡片
5. 提交操作卡片

## 输入区规范

### 1. 图标位置

输入图标放在输入框前面，不放在输入框内部。

推荐结构：

- 外层横向 `LinearLayout`
- 左侧为图标容器
- 右侧为 `TextInputLayout`

不要把位图图标直接放到 `TextInputLayout` 内部前缀位。

原因：

- 容易被 tint
- 容易变成发绿、扁平、失真效果
- 容易把输入框高度撑坏

### 2. 输入框样式

所有 `TextInputLayout` 统一使用以下 outline 圆角风格，以 `fragment_creat_goods.xml` 为基准。

**标准可编辑字段模板：**

```xml
<com.google.android.material.textfield.TextInputLayout
    android:layout_width=”0dp”
    android:layout_height=”wrap_content”
    android:layout_marginStart=”12dp”
    android:layout_weight=”1”
    android:hint=”商品名称”
    app:boxBackgroundColor=”#F6FBFA”
    app:boxBackgroundMode=”outline”
    app:boxCornerRadiusBottomEnd=”18dp”
    app:boxCornerRadiusBottomStart=”18dp”
    app:boxCornerRadiusTopEnd=”18dp”
    app:boxCornerRadiusTopStart=”18dp”
    app:boxStrokeColor=”#C9E4E0”
    app:boxStrokeWidth=”1dp”
    app:boxStrokeWidthFocused=”2dp”
    app:endIconMode=”clear_text”
    app:hintTextColor=”#7A8E8E”
    app:placeholderText=”请输入…”>

    <com.google.android.material.textfield.TextInputEditText
        android:id=”@+id/et_xxx”
        android:layout_width=”match_parent”
        android:layout_height=”wrap_content”
        android:minHeight=”52dp”
        android:paddingVertical=”8dp”
        android:textColor=”#172A2B”
        android:textSize=”15sp” />
</com.google.android.material.textfield.TextInputLayout>
```

**各属性说明：**

| 属性 | 值 | 说明 |
|------|------|------|
| `boxBackgroundColor` | `#F6FBFA` | 浅薄荷底色 |
| `boxBackgroundMode` | `outline` | 轮廓模式 |
| `boxCornerRadius*` | `18dp` | 四角统一圆角 |
| `boxStrokeColor` | `#C9E4E0` | 默认描边色 |
| `boxStrokeWidth` | `1dp` | 默认描边宽度 |
| `boxStrokeWidthFocused` | `2dp` | 聚焦时描边加粗 |
| `endIconMode` | `clear_text` | 一键清空按钮 |
| `hintTextColor` | `#7A8E8E` | 提示文字颜色 |
| EditText `minHeight` | `52dp` | 最小高度，保证触摸区域 |
| EditText `textColor` | `#172A2B` | 输入文字颜色 |
| EditText `textSize` | `15sp` | 字号 |

**只读 / 禁用字段变体：**

```xml
app:boxBackgroundColor=”#F3F6F5”
app:boxStrokeColor=”#DDE5E3”
android:textColor=”#8A9A9A”
<!-- 不设置 endIconMode，不设置 boxStrokeWidthFocused -->
```

**多行文本字段变体（签名、备注、描述等）：**

```xml
<com.google.android.material.textfield.TextInputEditText
    android:gravity=”top”
    android:inputType=”textMultiLine”
    android:maxLines=”5”
    android:minHeight=”100dp”
    android:paddingVertical=”12dp” />
<!-- 不设置 endIconMode -->
```

**图标 + 输入框的行结构：**

图标放在输入框外面左侧，使用 44×44dp 的 `FrameLayout` 容器承载 20×20dp 的图标：

```xml
<LinearLayout
    android:layout_width=”match_parent”
    android:layout_height=”wrap_content”
    android:layout_marginTop=”14dp”
    android:gravity=”center_vertical”
    android:orientation=”horizontal”>

    <FrameLayout
        android:layout_width=”44dp”
        android:layout_height=”44dp”
        android:background=”@drawable/bg_create_goods_field_icon”>

        <ImageView
            android:layout_width=”20dp”
            android:layout_height=”20dp”
            android:layout_gravity=”center”
            android:contentDescription=”@null”
            android:src=”@drawable/market_icon_goods” />
    </FrameLayout>

    <com.google.android.material.textfield.TextInputLayout ...>
        ...
    </com.google.android.material.textfield.TextInputLayout>
</LinearLayout>
```

**字段间距**：同行内图标与输入框间距 `12dp`；上下字段行间距 `10dp`；每组第一个字段距标题 `14dp`。

**重要提醒：** 不要为了省事把 `TextInputLayout` 退化成旧的 `boxStrokeWidth=”0dp”` 下划线样式，也不要用 `style=”@style/Widget.MaterialComponents.TextInputLayout.OutlinedBox”`（会覆盖上述定制属性）。

### 3. 字段排布

只要两个字段并排会影响阅读或输入，就改成单列。

适合单列的典型字段：

- 价格
- 折扣
- 标签
- 库存
- 位置
- 说明
- 备注

结论是：优先可读性，不强求双列整齐。

## 插图与 banner 策略

### 1. 商品、订单、二手类页面

这些页面可以使用顶部插图或业务 banner，但必须服务于业务语义。

要求：

- 风格统一
- 画面与业务相关
- 不喧宾夺主
- 与 hero 和卡片过渡自然

插图可以偏写实，也可以是现代轻插画，但不要使用廉价感过强的旧式扁平插画。

### 2. 文本发布页例外

`fragment_create_post.xml` 以文本编辑为主体，不强调插图。

这一页应优先强化：

- 标题输入
- 正文编辑区
- 草稿感与创作氛围
- 字数反馈
- 辅助操作

不要强行套用商品发布页那种“图片先行”的结构。

## 键盘避让与焦点滚动

这是所有信息填写页的强制规范。

### 1. 目标

目标不是整页简单上移，而是：

- 当前焦点输入区不被键盘遮挡
- 焦点移动时滚动位置自然
- 收起键盘后状态干净

### 2. 推荐实现

**优先使用共享帮助类 `KeyboardScrollHelper`**，位于 `com.example.grabthisforme.util.KeyboardScrollHelper`。
不要再在每个 Fragment 中内联重复键盘避让和焦点滚动逻辑。

```kotlin
private var keyboardScrollHelper: KeyboardScrollHelper? = null

// onViewCreated
keyboardScrollHelper = KeyboardScrollHelper(
    rootView = requireView(),
    scrollView = binding.nestedScrollView,
    density = resources.displayMetrics.density,
    onImeHidden = { if (_binding != null) clearInputFocus() }
).also { it.setup() }

// onDestroyView
keyboardScrollHelper?.teardown()
keyboardScrollHelper = null
```

对于大段文本编辑页（如 `PostTopicFragment`），若需要光标级滚动，可传入 `focusRectProvider`：

```kotlin
focusRectProvider = { view ->
    if (view === binding.itPostContent) {
        buildCursorRect(binding.itPostContent)
    } else {
        android.graphics.Rect().also { view.getDrawingRect(it) }
    }
}
```

若 Fragment 需要在非键盘弹起时机手动滚动焦点（如文本变更、焦点监听），可调用 `keyboardScrollHelper?.scrollToFocused()`。

### 3. 实现要点

`KeyboardScrollHelper` 内部封装了：

1. 记录 `NestedScrollView` 初始 `paddingBottom`
2. 监听 window insets
3. 分别读取 IME inset 与 system bar inset
4. 计算纯键盘高度
5. 将这部分高度加到滚动容器底部 `padding`
6. 键盘显示后延迟 120ms 滚动焦点
7. 键盘收起后通过 `onImeHidden` 回调清理焦点

内部常量：

- `KEYBOARD_SCROLL_DELAY_MS = 120L`
- `KEYBOARD_FOCUS_SPACING_DP = 24`

### 4. 焦点滚动要求

滚动逻辑应做到：

- 获取当前 `focus view`
- 确认焦点属于当前滚动容器
- 把焦点可视区域换算到 scrollView 坐标系
- 当焦点底部被键盘压住时向下滚动
- 当焦点顶部跑到可见区域外时向上滚动
- 留出安全间距，不要紧贴键盘

对于文本编辑页，如果是大文本输入区，滚动目标应优先保证”光标附近内容可见”，而不是简单把整个输入框滚到中间。此时应通过 `focusRectProvider` 返回光标所在行区域。

## 页面差异化原则

统一的是结构，不是语气。

不同页面可以在同一套框架下表达不同重点：

- `CreateGoodsFragment`：商品上架、展示、售卖
- `CreateOrderFragment`：需求发布、代买、代取、委托
- `CreateSecondHandGoods`：闲置转让、成色说明、交易透明
- `PostTopicFragment`：文本表达、社区发布、观点组织

## 资源与命名

优先复用已有资源体系，不要为相似效果重复创建大量资源。

新增资源时建议：

- 文件名使用英文
- 名称体现页面或业务前缀
- 名称体现用途，例如 `hero`、`card`、`field_icon`、`banner`

这样更利于不同工具检索和后续维护。

## 文案与编码

- 页面展示文案保持正常中文。
- 不要把乱码再次写回 XML、Kotlin、Markdown。
- 项目文档建议保持 UTF-8。

## 修改流程

每次改这类页面，建议按顺序执行：

1. 确认业务类型和页面重点
2. 调整 hero、banner、卡片分组
3. 调整输入区结构和字段排布
4. 调整插图或资源风格
5. 完成键盘避让、焦点滚动、空白收键盘
6. 编译验证

## 验证

按项目通用构建与测试 skill 执行验证：

- `docs/skills/platform/android-build-and-test/SKILL.md`

对于普通 UI / 布局改动，至少执行：

- `:app:compileDebugKotlin`

如需进一步验证 Kotlin 逻辑，可再尝试：

- `:app:testDebugUnitTest`

但要注意当前环境可能存在 loopback 限制。

## 放置约定

这个 skill 放在项目内：

- `docs/skills/ui/forms/create-information-form-pages-ui/`

原因：

- 方便和项目代码一起维护
- 方便不同智能体直接从仓库内检索
- 方便上层 `README.md` 建立检索树与优先级说明
