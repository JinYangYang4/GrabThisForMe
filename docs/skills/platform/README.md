# Platform Skills

## 作用

这一层存放与项目构建、测试、运行环境、工具链和工程验证有关的 skill。

## 适用场景

遇到以下任务时，优先来这里检索：

- 需要验证项目是否还能编译
- 需要执行单元测试或设备测试
- 需要确认 Gradle、JDK、缓存目录的约束
- 需要统一不同页面 skill 中的“编译验证”做法

## 当前 Skills

### `android-build-and-test/`

用于本项目 Android 构建与测试验证。

覆盖内容：

- `compileDebugKotlin` 编译验证
- `testDebugUnitTest` 单元测试
- `connectedDebugAndroidTest` 设备 / 模拟器测试
- `JAVA_HOME`、`GRADLE_USER_HOME`、Gradle Wrapper 的使用约束
- 当前环境下已知问题与可执行边界
