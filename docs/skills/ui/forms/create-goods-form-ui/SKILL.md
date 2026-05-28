---
name: create-goods-form-ui
description: 用于本项目 `CreateGoodsFragment` 及同类数据填写页的界面设计与改造规范。适用于编辑 `fragment_creat_goods.xml`、`CreateGoodsFragment.kt`、该页相关 drawable 或把同样的卡片式表单和键盘避让能力复用到其他表单页面时。
---

# CreateGoodsFragment 页面 Skill

## 目标

统一本项目商品创建页的视觉结构、输入区样式和键盘弹出后的避让逻辑，后续只要改这类“表单填写页”，都优先沿用这套规则，不要每次重新设计一套。

## 适用范围

适用于以下场景：

- 优化 `CreateGoodsFragment` 的 UI
- 新建一个和商品创建页风格一致的表单页
- 修复输入框被键盘挡住的问题
- 调整卡片式输入区、顶部插图、上传图片区的结构
- 给同类页面复用 `NestedScrollView + IME inset + 聚焦字段滚动` 的交互

## 先读文件

先看这两个文件：

- `app/src/main/res/layout/fragment_creat_goods.xml`
- `app/src/main/java/com/example/grabthisforme/activity/fragment_misc/create/view/CreateGoodsFragment.kt`

## 布局规则

### 1. 页面骨架

页面保持这套结构：

- 顶部 `hero` 区
- 下方 `NestedScrollView`
- 滚动区内用多张卡片堆叠承载内容

不要改成传统纯白表单页，也不要回退成旧式简单线性布局。

### 2. 顶部区域

顶部区域包含：

- 返回按钮
- 页面标题
- 一句主文案
- 一句辅助文案
- 两个小胶囊标签

视觉上要让下方卡片“顶”进 hero 区，保留当前这种叠层感。

### 3. 滚动内容区

`ll_nested` 不负责横向留白。

规则是：

- `ll_nested` 只保留必要的 `paddingTop`
- 横向间距交给每个子卡片自己处理
- banner 卡片、表单卡片、底部按钮统一使用 `layout_marginHorizontal="10dp"`

不要再给 `ll_nested` 加 `paddingStart/paddingEnd`。

### 4. 卡片规范

内容卡片保持以下特征：

- 圆角
- 轻阴影
- 内边距充足
- 信息分组明确

当前顺序建议保持：

1. 顶部横幅图卡片
2. 商品基础信息卡片
3. 价格与库存卡片
4. 封面图片区卡片
5. 提交按钮

### 5. 插图与图片

顶部插图和封面上传区遵守这些原则：

- 顶部 banner 走整图展示
- 图和卡片视觉统一，不要额外叠奇怪的圆角层
- 上传区采用“左侧预览 + 右侧说明”的结构
- 图片区域要明显可点击

## 输入区规则

### 1. 图标位置

图标必须放在输入框前面，不要放进 `TextInputLayout` 内部。

原因：

- 放进输入框内部后，生成的位图图标会被 tint
- 会变成扁平、发绿、失真
- 视觉效果比放在输入框前面差很多

字段结构统一为：

- 一层横向 `LinearLayout`
- 左边是图标容器
- 右边是 `TextInputLayout`

### 2. 输入框风格

沿用当前样式：

- `TextInputLayout` 的 `outline` 模式
- 柔和背景色
- 细描边
- 比较大的圆角
- 合理的最小高度

不要退回到老式裸 `EditText` 风格。

### 3. 字段排布

如果两个输入框并排后宽度明显不够，就拆成单列。

当前这页中，以下字段应该一行一个：

- 售价
- 折扣价
- 标签
- 库存

不要再改回双列并排，除非用户明确要求。

## 键盘避让规则

这是这份 skill 的重点，后续同类表单页也优先复用。

### 1. 基本策略

使用：

- `NestedScrollView`
- `WindowInsetsCompat.Type.ime()`
- 动态增加滚动区域底部 padding
- 键盘出现后把当前聚焦输入框滚动到可见区域

目标不是“整个页面上移”，而是“当前输入区域不会被键盘挡住”。

### 2. 当前实现要点

`CreateGoodsFragment.kt` 里已经有一套可复用实现，核心点如下：

1. 记录 `nestedScrollView` 原始 `paddingBottom`
2. 对根 view 设置 `ViewCompat.setOnApplyWindowInsetsListener`
3. 读取 IME inset 和 system bar inset
4. 计算纯键盘占用高度
5. 把这个高度加到 `nestedScrollView.paddingBottom`
6. 键盘显示后稍微延迟，再滚动聚焦输入框
7. 键盘隐藏后清理焦点
8. 点击空白处时也隐藏键盘并清理焦点

### 3. 聚焦字段滚动逻辑

滚动逻辑必须做到：

- 先拿到当前 `focus view`
- 确认它属于当前 `NestedScrollView`
- 把 focus 的可视区域转换到 scrollView 坐标系
- 计算当前可见顶部和底部
- 如果 focus 底部被键盘压住，就向下滚
- 如果 focus 顶部跑到可见区外，也要向上滚
- 留一小段额外安全距离，不要紧贴键盘

当前实现里的两个常量可以直接复用：

- `KEYBOARD_SCROLL_DELAY_MS = 120L`
- `KEYBOARD_FOCUS_SPACING_DP = 24`

## 点击与收起键盘规则

当前页和后续同类页都建议保留：

- 点根布局空白区域时收起键盘
- 点滚动容器空白区域时收起键盘
- 提交前先清理焦点

这能避免用户提交后键盘还悬在屏幕上。

## 相关资源

这页常用资源包括：

- `bg_create_goods_screen`
- `bg_create_goods_hero`
- `bg_create_goods_card`
- `bg_create_goods_chip`
- `bg_create_goods_button`
- `bg_create_goods_icon_button`
- `bg_create_goods_field_icon`
- `bg_create_goods_photo_slot`
- `bg_create_goods_banner_badge`
- `bg_create_goods_top_glass`
- `create_goods_market_banner`
- `market_icon_goods`
- `market_icon_store`
- `market_icon_info`
- `market_icon_price`
- `market_icon_voucher`
- `market_icon_stock`
- `market_icon_photo`

## 文案规则

这页文案必须保持正常中文。

注意：

- 不要把乱码重新写回 XML
- 不要让 shell 工具把文件写成带 BOM 的 UTF-8
- 这个文件之前已经因为 BOM 和乱码导致过资源合并失败

## 修改流程

每次改这类页面，按这个顺序做：

1. 先改布局 XML
2. 再看是否需要改 Fragment 里的焦点、键盘、滚动逻辑
3. 检查图标是否仍然在输入框前面
4. 检查价格区是否仍是一行一个字段
5. 编译验证

## 验证

建议在项目根目录运行：

```powershell
$env:JAVA_HOME='C:\Users\YaoShi16Pro\AppData\Local\JetBrains\IdeaIC2025.1\tmp\patch-update\jre'
$env:PATH="$env:JAVA_HOME\bin;$env:PATH"
& 'C:\Users\YaoShi16Pro\.gradle\wrapper\dists\gradle-8.13-bin\5xuhj0ry160q40clulazy9h7d\gradle-8.13\bin\gradle.bat' --no-daemon :app:compileDebugKotlin
```

已知无关告警：

- Glide 仍提示把 `annotationProcessor` 改成 `kapt`

这个告警和当前页面 UI 修改无关。
