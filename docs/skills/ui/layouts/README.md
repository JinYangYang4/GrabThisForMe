# Layout Skills

## 作用

这一层用于归档项目中的布局容器类 skill，包括 `NestedScrollView`、`ScrollView`、`RecyclerView` 等通用容器在项目中的使用规范。

与 `forms/`、`settings/` 等业务页面 skill 不同，这里的 skill 关注的是**容器本身的布局约束**，适用于所有使用该容器的页面。

## 分类

- `nested-scroll-view-layout/`：NestedScrollView 布局规范（直接子 View 约束、padding/margin 转换规则）

## 使用顺序

- 在编写或修改任何包含 `NestedScrollView` 的页面布局时，先查阅 `nested-scroll-view-layout/` 中的约束规则
- 业务页面 skill（如 `forms/`、`settings/`）是"页面级规范"，layout skill 是"容器级规范"
- 如果页面级规范与容器级规范冲突，以容器级规范为准（除非有明确的注释说明例外）

## 检索提示

可同时使用中英文关键词检索，例如：

- `NestedScrollView`
- `scroll view`
- `scroll container`
- `direct child`
- `padding`
- `margin`
- `layout constraint`
- `滚动布局`
- `嵌套滚动`
- `布局规范`
