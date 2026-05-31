# UI - 设置与资料页 (Settings / Profile)

## 何时使用此分类

当需求涉及以下任意场景时，优先进入 `settings-page-pattern/` 技能：

- 新建或修改"设置"类页面（账号安全、隐私、聊天背景等）
- 新建或修改"个人资料"类页面（查看资料、编辑资料）
- 需要在设置页面中应用交错入场动画
- 需要遵循设置页面的标题栏、卡片分组、滚动区域的统一布局骨架

-不适合在非设置页面中使用，除非指定使用这个skills
## 子技能

| 技能 | 路径 | 覆盖页面 |
|---|---|---|
| 设置页面通用模式 | `settings-page-pattern/SKILL.md` | FragmentSet、FragmentPersonalInformation、EditPersonalInformationFragment、FragmentAccountSecurity、FragmentChatBackground |

## 搜索关键词

`settings ui`, `profile page`, `personal information`, `account security`, `chat background`, `设置页`, `资料页`, `编辑资料`, `交错动画`, `staggered entrance`, `title bar`, `card group`, `NestedScrollView`
