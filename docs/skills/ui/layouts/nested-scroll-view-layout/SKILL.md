---
name: nested-scroll-view-layout
description: NestedScrollView 布局规范：只能包含一个直接子布局，且禁止在该直接子布局上使用 padding。如需内边距效果，将 padding 转换为直接子布局的下一层子 view 的 margin。English retrieval keywords: NestedScrollView, scroll view, layout constraint, padding, margin, direct child, single child, scroll container.
---

# NestedScrollView 布局规范 Skill

## 目标

统一本项目所有使用 `NestedScrollView` 的页面的布局结构，防止因多子 View、直接子布局使用 padding、以及不当的嵌套方式导致的滚动异常、测量错误和渲染问题。

## 适用范围

本项目所有 XML 布局中使用了 `androidx.core.widget.NestedScrollView` 的页面。

当前涉及约 23 个布局文件，涵盖：
- 创建/发布/填写信息页（`fragment_creat_goods`、`fragment_create_order`、`fragment_create_post`、`fragment_create_secondhand_goods` 等）
- 设置/资料/安全页（`fragment_set`、`fragment_personal_information`、`fragment_edit_personal_information`、`fragment_account_security`、`fragment_chat_backgroud` 等）
- 登录/注册页（`fragment_login`、`fragment_register`、`fragment_sign_in`、`fragment_find_passage` 等）
- 商店/商品页（`fragment_store`、`fragment_store_owner`、`fragment_goods_detail`、`fragment_search_goods` 等）

## 何时使用

当满足以下任一条件时，使用本 skill：

- 新建一个包含 `NestedScrollView` 的页面布局
- 修改现有 `NestedScrollView` 页面的布局结构
- 排查 `NestedScrollView` 相关的滚动异常、内容截断、测量错误
- 统一多个页面的滚动区写法
- 检查或修复 `NestedScrollView` 的直接子布局 padding 使用

## 核心规范

### 规则 1：NestedScrollView 只能包含一个直接子布局

`NestedScrollView` 继承自 `FrameLayout`，但其滚动逻辑依赖于**唯一一个直接子 View** 的测量。放入多个直接子 View 会导致滚动内容不可预测，部分子 View 无法正常滚动或被遮挡。

**正确做法：**
- `NestedScrollView` 的直接子 View 必须是一个 ViewGroup（如 `LinearLayout`、`ConstraintLayout`、`Column`）
- 所有需要滚动的内容都放在这个唯一的 ViewGroup 内部

**错误做法：**
- 在 `NestedScrollView` 内并列放置多个直接子 View
- 在 `NestedScrollView` 内使用 `<include>` 标签平铺（必须先包一层 ViewGroup）
- 用 `FrameLayout`/`LinearLayout` 包裹内容后，又在其同级添加其他 View

### 规则 2：禁止在直接子布局上使用 padding（除非明确要求）

`NestedScrollView` 的直接子布局**禁止使用任何 `padding` 属性**。需要在内容周围留白时，应将 padding 转换为**直接子布局的下一层子 View 的 `margin`**。

**原因：**

1. `NestedScrollView` 通过 `clipToPadding="false"` 控制内容区域，直接子布局的 padding 会与 ScrollView 的 padding 产生不可预期的叠加效果
2. 子布局的 padding 影响测量结果，可能导致 `fillViewport="true"` 时内容区域计算错误
3. 在嵌套滚动链中（如 CoordinatorLayout → NestedScrollView → RecyclerView），padding 会逐层传递，调试困难
4. 将留白放在卡片级 View 的 margin 上，职责更清晰、可视区域更可控

**例外情况：** 只有在该页面的设计规范中**明确指定**直接子布局需要使用 padding 时，才允许保留。此时必须在 XML 中添加注释说明原因。

### 规则 3：横向留白统一由子 View 的 margin 负责

直接子布局（如 `ll_nested`、内容 LinearLayout）不设置 `paddingStart`、`paddingEnd`、`paddingHorizontal`。每张卡片或内容区域通过自己的 `layout_marginHorizontal` 或 `layout_marginStart` + `layout_marginEnd` 来控制横向留白。

这与项目已有规范一致：
- `docs/skills/ui/forms/create-information-form-pages-ui/SKILL.md` 第 73-75 行：`ll_nested` 不设置横向 `padding`，只保留必要的顶部、底部 padding
- `docs/skills/ui/settings/settings-page-pattern/SKILL.md`：卡片使用 `layout_marginStart="14dp"` + `layout_marginEnd="14dp"`

## 正确示例（Good）

```xml
<androidx.core.widget.NestedScrollView
    android:layout_width="match_parent"
    android:layout_height="0dp"
    android:clipToPadding="false"
    android:fillViewport="true"
    android:overScrollMode="never">

    <!-- 唯一的直接子布局，不设置任何 padding -->
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical">

        <!-- 每张卡片自己控制横向 margin -->
        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginStart="14dp"
            android:layout_marginTop="5dp"
            android:layout_marginEnd="14dp"
            android:background="@drawable/bg_set_group_card"
            android:orientation="vertical"
            android:padding="16dp">

            <!-- 卡片内容 ... -->
        </LinearLayout>

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginStart="14dp"
            android:layout_marginTop="12dp"
            android:layout_marginEnd="14dp"
            android:layout_marginBottom="24dp"
            android:background="@drawable/bg_set_group_card"
            android:orientation="vertical"
            android:padding="16dp">

            <!-- 卡片内容 ... -->
        </LinearLayout>
    </LinearLayout>
</androidx.core.widget.NestedScrollView>
```

## 错误示例（Bad）—— 直接子布局使用了 padding

```xml
<!-- ❌ 错误：直接子布局使用了 padding -->
<androidx.core.widget.NestedScrollView
    android:layout_width="match_parent"
    android:layout_height="0dp"
    android:clipToPadding="false"
    android:fillViewport="true">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:paddingStart="14dp"
        android:paddingEnd="14dp"
        android:paddingBottom="24dp">

        <!-- 子 View 们 ... -->
    </LinearLayout>
</androidx.core.widget.NestedScrollView>
```

**修正方式：** 去掉直接子布局的 padding，给每个子 View 添加对应的 margin：

```xml
<!-- ✅ 修正后：padding 转换为子 View 的 margin -->
<androidx.core.widget.NestedScrollView
    android:layout_width="match_parent"
    android:layout_height="0dp"
    android:clipToPadding="false"
    android:fillViewport="true">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical">

        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:layout_marginStart="14dp"
            android:layout_marginEnd="14dp"
            ...>

            <!-- 子 View 们 ... -->
        </LinearLayout>
    </LinearLayout>
</androidx.core.widget.NestedScrollView>
```

## 错误示例（Bad）—— 多个直接子 View

```xml
<!-- ❌ 错误：NestedScrollView 内有两个直接子 View -->
<androidx.core.widget.NestedScrollView
    android:layout_width="match_parent"
    android:layout_height="0dp">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical">
        <!-- 上半部分内容 -->
    </LinearLayout>

    <TextView
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="底部提示" />
</androidx.core.widget.NestedScrollView>
```

**修正方式：** 将多个直接子 View 包裹在一个 ViewGroup 内：

```xml
<!-- ✅ 修正后：唯一直接子布局 -->
<androidx.core.widget.NestedScrollView
    android:layout_width="match_parent"
    android:layout_height="0dp">

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="vertical">
        <!-- 上半部分内容 -->

        <TextView
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:text="底部提示" />
    </LinearLayout>
</androidx.core.widget.NestedScrollView>
```

## 错误示例（Bad）—— NestedScrollView 自身设置了不必要的 padding

```xml
<!-- ❌ 避免：在 NestedScrollView 上设置 padding -->
<androidx.core.widget.NestedScrollView
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:paddingStart="16dp"
    android:paddingEnd="16dp"
    android:paddingBottom="20dp">

    <LinearLayout ...>
        ...
    </LinearLayout>
</androidx.core.widget.NestedScrollView>
```

**说明：** NestedScrollView 的 padding 会压缩可滚动视口，配合 `clipToPadding="false"` 虽然能让内容绘制到 padding 区域，但滚动边界计算会变复杂。建议仅在 NestedScrollView 上使用 `clipToPadding="false"`，让内部的子 View 通过 margin 控制间距。如需底部安全间距，在最后一个子 View 上设置 `layout_marginBottom`。

## 检查清单

在编写或修改含 `NestedScrollView` 的布局时，逐项确认：

1. [ ] `NestedScrollView` 有且仅有一个直接子 View
2. [ ] 该直接子 View 是一个 ViewGroup（LinearLayout、ConstraintLayout 等）
3. [ ] 直接子 View **没有** `padding`、`paddingStart`、`paddingEnd`、`paddingTop`、`paddingBottom`、`paddingHorizontal`、`paddingVertical` 属性
4. [ ] 如需横向留白，由直接子 View 的下一层子 View 通过 `layout_marginHorizontal` / `layout_marginStart` + `layout_marginEnd` 控制
5. [ ] 如需底部留白（防止内容被截断），在最后一个子 View 上设置 `layout_marginBottom`
6. [ ] 如需顶部留白，在第一个子 View 上设置 `layout_marginTop`
7. [ ] 如果某个 padding 确实必须保留，XML 中有注释明确说明原因

## 相关 Skill

本 skill 是基础布局规范，以下 skill 依赖并引用了本规范：

- `docs/skills/ui/forms/create-information-form-pages-ui/SKILL.md` — 创建页 `ll_nested` 规则
- `docs/skills/ui/settings/settings-page-pattern/SKILL.md` — 设置页骨架中的 NestedScrollView 用法

## 放置约定

这个 skill 放在项目内：

- `docs/skills/ui/layouts/nested-scroll-view-layout/`

原因：

- NestedScrollView 是一种通用布局容器，跨越表单页、设置页、详情页等多种页面类型
- 不适合归入 `forms/`（仅创建/发布页）或 `settings/`（仅设置页）子分类
- 独立为 `layouts/` 分类，与其他业务页面 skill 平级，只在需要时加载
