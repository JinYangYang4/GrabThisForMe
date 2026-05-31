# 项目 Skills 目录

## 作用

`docs/skills/` 用来存放当前项目内可复用的技能文档，约束 UI、交互、结构、构建和验证方式，方便不同智能体在同一个仓库内快速检索并复用已有规则。

这里的 skill 属于项目文档，不依赖 `.codex`，也不绑定某一个特定工具。

## 目录约定

- 一个 skill 对应一个独立文件夹。
- 每个 skill 文件夹内必须包含一个 `SKILL.md`。
- 上层 `README.md` 负责做分类说明、检索引导和优先级说明。
- 文件夹名优先使用英文，便于 `Codex`、`Claude`、`Gemini`、`Cursor` 等工具检索。

## 当前分层

- `ui/`：界面、交互、视觉规范（含 `layouts/` 通用布局容器规范、`forms/` 表单页、`settings/` 设置页等）。
- `domain/`：业务规则、领域建模预留目录。
- `data/`：数据结构、存储、接口映射预留目录。
- `platform/`：构建、测试、工具链、工程规范。

## 检索原则

- 先看上层分类说明，再进入具体 skill。
- 多个页面共用一套模式时，优先使用通用 skill，不要先写页面专属 skill。
- 只有当某个页面存在明显独有的结构、资源或交互时，才单独拆出页面专属 skill。
- 构建、编译、测试、Gradle 环境问题优先到 `platform/` 下检索。

## 跨智能体约定

- 目录名使用英文检索词。
- `SKILL.md` 正文可以使用中文，但建议在标题、描述或正文中保留少量英文关键词。
- 父级 `README.md` 需要明确说明“先用哪个 skill、什么情况下再用更窄的 skill”。

## 当前重点

当前已建立的布局容器 skill 位于：

- `ui/layouts/nested-scroll-view-layout/`：NestedScrollView 通用布局约束规范（直接子 View 约束、padding/margin 转换规则）。

当前已建立的信息填写页相关 skill 位于：

- `ui/forms/create-information-form-pages-ui/`：通用创建 / 发布 / 信息填写页规范。

当前已建立的平台通用 skill 位于：

- `platform/android-build-and-test/`：Android 项目的编译验证、单元测试、设备测试与环境约束说明。
