# Android UI Skill: Forms/Auth-Login-Register-Recover

## 1. Skill定位
- Skill ID：`ui/forms/auth-login-register-recover`
- Skill 类型：认证流程表单 UI 规范（登录/注册/密码找回）
- 参考页面：
  - `app/src/main/res/layout/fragment_login.xml`
  - `app/src/main/res/layout/fragment_register.xml`
  - `app/src/main/res/layout/fragment_find_passage.xml`
- 目标：批量生成同风格认证页面 XML，并保证与现有 Fragment/ViewBinding 兼容。

## 2. 总体视觉风格
- 页面基底：统一使用 `@drawable/img_register_back`。
- 整体布局：根容器固定为纵向 `LinearLayout`，`match_parent` 宽高。
- 视觉结构：上方大标题 + 中部卡片表单 + 底部切换账号文案。
- 设计语义：轻拟态浅色表单卡片，弱分隔，主操作位于卡片底部。

## 3. 页面骨架（强制）
- 根布局：
  - `LinearLayout`
  - `orientation="vertical"`
  - `layout_width/height="match_parent"`
  - `background="@drawable/img_register_back"`
- 标题区：
  - `TextView`，`id=@id/tv_title`
  - `textSize="28sp"`，`textStyle="bold"`
  - 顶部与左右留白通过 `layout_margin="24dp"` 控制
  - 标题下间距 `layout_marginBottom="20dp"`
- 表单卡片：
  - `LinearLayout`，纵向
  - `layout_marginHorizontal="24dp"`
  - `layout_marginTop="120dp"`（登录页可为 `140dp`）
  - `background="@drawable/bg_round_stripe"`
  - `padding="16dp"`
  - `elevation="4dp"`，`clipToOutline="true"`，`outlineProvider="background"`
- 底部占位与切换：
  - `Space`：`height=0dp` + `layout_weight=1`
  - `TextView`：`id=@id/tv_switch_accounts`，居中，`layout_marginBottom="20dp"`，文案“切换账号”

## 4. 表单输入组件规范
- 输入容器：全部使用 `TextInputLayout`。
- 输入框：全部使用 `TextInputEditText`。
- 通用输入样式：
  - `layout_width="match_parent"`
  - `layout_height="wrap_content"`
  - `background="@drawable/bg_rounded_white"`
- 垂直节奏：
  - 第一项无额外顶距
  - 后续每项 `layout_marginTop="16dp"`
- 密码类输入：
  - `TextInputLayout` 增加 `app:endIconMode="password_toggle"`
  - `TextInputEditText` 用 `inputType="textPassword"`
- 文本类输入：
  - 账号/用户名 `inputType="textNoSuggestions"`
  - 手机号 `inputType="phone"`
  - 验证码 `inputType="number"`

## 5. 按钮与操作区规范
- 操作条容器：
  - `id=@id/layout_action`
  - `layout_width="match_parent"`，`layout_height="40dp"`
  - `layout_marginTop="24dp"`
  - `background="@drawable/bg_round_stripe"`
  - `clipToOutline="true"`，`outlineProvider="background"`
  - `orientation="horizontal"`，`gravity="center"`
- 按钮项基准：
  - 子容器 `layout_margin="2dp"`，`layout_height="match_parent"`
  - `clickable="true"`，`focusable="true"`，`gravity="center"`
  - `elevation="4dp"`
- 登录页双按钮：
  - 左按钮 `id=@id/layout_login`，右按钮 `id=@id/layout_register`
  - 二者 `layout_width="0dp"` + `layout_weight="1"`
  - 中间固定 `View` 间隔 `16dp`
- 注册/找回页单按钮：
  - 使用 `id=@id/layout_register`
  - `layout_width="0dp"` + `layout_weight="1"`
- 按钮文本：
  - `textSize="16sp"`
  - `textColor="@color/blue_light"`

## 6. 三个页面的差异化规则

### 6.1 登录页 `fragment_login.xml`
- 标题文案：`登录`
- 必备输入：
  - `til_phone` + `et_user_id`（账号）
  - `til_password` + `et_password`（密码）
- 补充动作：
  - `tv_forget`（忘记密码）右对齐，`textSize="14sp"`，`textColor="#666666"`，`layout_marginTop="8dp"`
- 操作区：双按钮（登录/注册）

### 6.2 注册页 `fragment_register.xml`
- 标题文案：`注册`
- 必备输入：
  - `til_name` + `et_user_name`（用户名）
  - `til_password` + `et_password`（密码）
  - `til_password_make_sure` + `et_password_make_sure`（确认密码）
- 操作区：单按钮（确认）

### 6.3 找回页 `fragment_find_passage.xml`
- 标题文案：`密码找回`
- 必备输入：
  - `til_user_id` + `et_user_id`（账号）
  - `til_phone` + `et_phone`（手机号）
  - `til_verification_code` + `et_verification_code`（验证码）
  - `til_new_passage` + `et_new_password`（新密码）
- 操作区：单按钮（确认）

## 7. ViewBinding兼容规则（强制）
- 不允许随意改动以下 `id`，否则会破坏现有 Fragment 逻辑绑定：
  - 登录页：`tv_title`、`til_phone`、`et_user_id`、`til_password`、`et_password`、`tv_forget`、`layout_action`、`layout_login`、`layout_register`、`tv_switch_accounts`
  - 注册页：`tv_title`、`til_name`、`et_user_name`、`til_password`、`et_password`、`til_password_make_sure`、`et_password_make_sure`、`layout_action`、`layout_register`、`tv_switch_accounts`
  - 找回页：`tv_title`、`til_user_id`、`et_user_id`、`til_phone`、`et_phone`、`til_verification_code`、`et_verification_code`、`til_new_passage`、`et_new_password`、`layout_action`、`layout_register`、`tv_switch_accounts`
- 允许新增辅助视图，但不得删除上述核心控件。

## 8. 生成约束（强制）
- 必须保持认证三页同一视觉语言：相同背景、相同卡片材质、相同按钮系统。
- 必须保持垂直间距节奏：输入项 `16dp`，操作区 `24dp`，底部文案 `20dp`。
- 禁止引入与本规范冲突的强边框输入样式、深色主题按钮或全新配色体系。
- 禁止把根布局改为 `ConstraintLayout`（认证页统一使用纵向 `LinearLayout`）。
- 生成后必须自检：
  - 无 XML 冲突标记（`<<<<<<<`、`=======`、`>>>>>>>`）
  - id 与绑定一致
  - 页面结构完整（标题、表单卡片、操作区、底部切换文案）
