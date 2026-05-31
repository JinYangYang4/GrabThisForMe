---
name: android-build-and-test
description: 用于本项目 Android 构建验证、单元测试、设备测试和 Gradle 运行环境检查。适用于需要确认修改后是否还能编译、是否能运行 JUnit 单元测试、是否能运行 androidTest、如何设置 JAVA_HOME 和 GRADLE_USER_HOME、以及如何处理当前项目已知 Gradle 环境限制的场景。English retrieval keywords: Android build, Gradle test, compileDebugKotlin, testDebugUnitTest, connectedDebugAndroidTest, JAVA_HOME, GRADLE_USER_HOME.
---

# Android 构建与测试 Skill

## 作用

统一本项目的编译验证与测试执行方式，避免每个页面 skill 都各自写一套命令。

这个 skill 面向整个项目，不限定某一个页面。

## 适用范围

适用于以下任务：

- 改完 XML、Kotlin、资源后验证项目是否还能编译
- 运行单元测试
- 运行 `androidTest`
- 检查构建失败到底是代码问题还是环境问题
- 给其他 skill 提供统一的验证出口

## 基本结论

当前项目里需要区分三类动作：

1. 编译验证
2. 本地 JVM 单元测试
3. 设备 / 模拟器测试

不要把 `compileDebugKotlin` 误写成“测试命令”。

## 环境约定

优先使用项目已有的 Gradle Wrapper。

推荐在 PowerShell 中先设置：

```powershell
$env:JAVA_HOME='C:\Users\YaoShi16Pro\AppData\Local\JetBrains\IdeaIC2025.1\tmp\patch-update\jre'
$env:PATH="$env:JAVA_HOME\bin;$env:PATH"
$env:GRADLE_USER_HOME='D:\projects\GrabThisForMe\xin\GrabThisForMe\.gradle-user-home-local'
```

说明：

- `JAVA_HOME`：指定当前终端使用的 JDK
- `PATH`：确保当前终端优先使用该 JDK
- `GRADLE_USER_HOME`：把 Gradle 缓存固定到项目附近，减少全局缓存和锁文件干扰

如果本机 JDK 路径变化，只替换 `JAVA_HOME` 即可，命令结构保持不变。

## 默认验证顺序

处理普通页面或资源修改时，按以下顺序：

1. 先跑编译验证
2. 如果改动涉及纯 Kotlin 逻辑，再尝试单元测试
3. 如果改动涉及需要真机 / 模拟器的交互，再考虑 `androidTest`

## 编译验证

这是当前项目最稳定、最通用的验证方式。

执行命令：

```powershell
$env:JAVA_HOME='C:\Users\YaoShi16Pro\AppData\Local\JetBrains\IdeaIC2025.1\tmp\patch-update\jre'
$env:PATH="$env:JAVA_HOME\bin;$env:PATH"
$env:GRADLE_USER_HOME='D:\projects\GrabThisForMe\xin\GrabThisForMe\.gradle-user-home-local'
.\gradlew.bat --no-daemon --console=plain :app:compileDebugKotlin
```

作用：

- 检查 Kotlin 代码是否可编译
- 检查大部分资源引用和 DataBinding / ViewBinding 生成是否正常
- 适合作为大多数 UI 改动后的最低验证标准

当前状态：

- 已在本项目环境中实际跑通

## 单元测试

本项目当前存在：

- `app/src/test/java/com/example/grabthisforme/ExampleUnitTest.kt`

执行命令：

```powershell
$env:JAVA_HOME='C:\Users\YaoShi16Pro\AppData\Local\JetBrains\IdeaIC2025.1\tmp\patch-update\jre'
$env:PATH="$env:JAVA_HOME\bin;$env:PATH"
$env:GRADLE_USER_HOME='D:\projects\GrabThisForMe\xin\GrabThisForMe\.gradle-user-home-local'
.\gradlew.bat --no-daemon --console=plain :app:testDebugUnitTest
```

当前状态：

- 在当前环境下执行失败
- 已知错误：`java.io.IOException: Unable to establish loopback connection`

结论：

- 这个失败更像当前执行环境限制，不直接说明测试代码本身有错
- 因此当前项目级通用 skill 应把它标记为“尝试执行的标准单元测试命令”，但不能承诺在所有受限环境中必定跑通

## 设备 / 模拟器测试

本项目当前存在：

- `app/src/androidTest/java/com/example/grabthisforme/ExampleInstrumentedTest.kt`

标准命令：

```powershell
$env:JAVA_HOME='C:\Users\YaoShi16Pro\AppData\Local\JetBrains\IdeaIC2025.1\tmp\patch-update\jre'
$env:PATH="$env:JAVA_HOME\bin;$env:PATH"
$env:GRADLE_USER_HOME='D:\projects\GrabThisForMe\xin\GrabThisForMe\.gradle-user-home-local'
.\gradlew.bat --no-daemon --console=plain :app:connectedDebugAndroidTest
```

前提：

- 已连接可用设备，或已启动可用模拟器

注意：

- 没有设备 / 模拟器时，不要把这条命令当成默认验证步骤

## 当前环境已知问题

### 1. `:app:tasks --all` 失败

当前环境里直接执行 Gradle 任务列表时，出现：

- `java.io.IOException: Unable to establish loopback connection`

### 2. `:app:testDebugUnitTest` 失败

当前环境里执行单元测试也出现同样错误：

- `java.io.IOException: Unable to establish loopback connection`

### 3. 解释边界

这类错误优先视为环境限制，通常和以下因素有关：

- 受限沙箱
- 单次 daemon 启动过程受限
- 回环网络建立失败

不要在没有更多证据时，直接把它归因到业务代码。

## 给其他 Skills 的引用规则

其他页面级或模块级 skill 不要再各自内嵌完整测试说明。

统一做法：

- 页面 skill 只写“修改后按项目通用构建 / 测试 skill 验证”
- 具体命令和环境约束统一引用本 skill
