# UI Skills

## 作用

这一层用于归档项目中的界面类 skill，包括页面结构、视觉语言、输入交互、滚动行为、导航和主题规范。

## 分类

- `layouts/`：通用布局容器规范（NestedScrollView、ScrollView 等），适用于所有页面类型。
- `forms/`：创建页、发布页、数据填写页、编辑页。
- `settings/`：设置页、资料查看页、账号安全页、编辑资料页。
- `feed/`：列表流、卡片流、时间线预留目录。
- `detail/`：详情页、资料页预留目录。
- `navigation/`：侧边栏、顶部栏、底部导航预留目录。
- `theme/`：颜色、字体、间距、组件风格预留目录。

## 使用顺序

- 遇到 `NestedScrollView`、滚动容器相关的布局约束问题，进入 `layouts/`。
- 遇到创建、发布、填写信息、编辑内容类页面，优先进入 `forms/`。
- 遇到设置页、个人资料查看/编辑、账号安全、聊天背景等页面，优先进入 `settings/`。
- 遇到多个页面样式需要统一时，优先找可复用的通用 skill。
- 只有在需求明确限定某一个页面时，再使用页面专属 skill。

## 检索提示

可同时使用中英文关键词检索，例如：

- `NestedScrollView` / `scroll view` / `scroll container`
- `direct child` / `padding` / `margin` / `layout constraint`
- `form ui`
- `settings page` / `profile page`
- `create page`
- `publish page`
- `data entry`
- `keyboard insets`
- `hero cards`
- `staggered entrance`
- `card group`
- `滚动布局` / `嵌套滚动`
- `表单页面`
- `信息填写页`
- `设置页`
- `资料页`
