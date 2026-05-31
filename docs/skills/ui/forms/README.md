# Forms Skills

## 作用

这一层存放“信息填写页 / 创建页 / 发布页 / 编辑页”的界面规范和实现规则。

## 优先级

- 先使用通用 skill。
- 通用 skill 无法覆盖页面的独特需求时，再看页面专属 skill。

## 检索关键词

可用以下关键词快速定位：

- `create form`
- `publish form`
- `data entry form`
- `information entry page`
- `keyboard insets`
- `NestedScrollView`
- `hero + cards`
- `创建页面`
- `发布页面`
- `信息填写`
- `键盘遮挡`

## 当前 Skills

### `create-information-form-pages-ui/`

通用信息填写页 skill。

适用范围：

- `CreateGoodsFragment`
- `CreateOrderFragment`
- `PostTopicFragment`
- `CreateSecondHandGoods`

用途：

- 统一创建 / 发布 / 填写信息类页面的布局骨架
- 统一卡片圆角、输入区结构、滚动区域组织方式
- 统一键盘弹出后的避让与聚焦滚动逻辑
- 统一插图、banner、提交区的设计方式

结论：

只要任务是“美化或规范一类信息填写页”，优先使用这个 skill。

## 验证约定

表单类 skill 不再各自内嵌完整的构建 / 测试命令。

统一引用：

- `docs/skills/platform/android-build-and-test/SKILL.md`

普通 UI 改动至少执行：

- `:app:compileDebugKotlin`
