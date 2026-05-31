# 涂鸦板 (Doodle Board)

一个简洁的 Android 涂鸦应用，采用 Material Design 3 风格。

## 功能特性

- **马克笔绘图** - 半透明效果，二阶贝塞尔曲线平滑处理
- **颜色选择** - 8 个预设颜色 + 自定义颜色选择器（RGB 滑块）
- **笔刷粗细** - 滑块调节（1-50px）+ 4 个预设档位（2px/8px/20px/40px）
- **橡皮擦** - 白色画笔模拟橡皮擦效果
- **撤销/重做** - 支持最多 50 笔撤销
- **清空画布** - 带确认对话框
- **保存图片** - 保存为 PNG 格式到设备相册 `Pictures/涂鸦板/`

## 技术栈

- **语言:** Java
- **最低 SDK:** Android 10 (API 29)
- **目标 SDK:** Android 14 (API 34)
- **UI:** Material Design Components
- **架构:** MV 模式（Model-View）

## 项目结构

```
com.chenjinxiang.doodleboard
├── MainActivity.java          # 主 Activity
├── view/
│   └── DrawingView.java       # 自定义绘图 View
├── model/
│   ├── Stroke.java            # 笔画数据类（不可变）
│   ├── BrushManager.java      # 笔刷参数管理
│   └── HistoryManager.java    # 撤销/重做逻辑
├── ui/
│   ├── ColorPickerDialog.java # 颜色选择对话框
│   └── BrushSizeDialog.java   # 粗细调节对话框
└── utils/
    └── FileSaver.java         # 保存图片工具
```

## 构建

```bash
# 构建 Debug APK
./gradlew assembleDebug

# 构建 Release APK
./gradlew assembleRelease

# 安装到连接的设备
./gradlew installDebug

# 运行测试
./gradlew test
```

## 架构设计

### 纯笔画渲染
- 所有笔画在每次 `onDraw()` 时重新绘制
- 无离屏 Bitmap 缓存
- 低内存占用，支持无损编辑

### 笔画不可变性
- `Stroke` 构造函数执行 `new Path(path)` 深拷贝
- 确保 Path 对象在 Stroke 创建后不可修改
- 撤销/重做正确性的关键

### HistoryManager 三层结构
```java
List<Stroke> strokes;              // 所有笔画（完整画布状态）
Deque<Stroke> undoableStrokes;      // 最近 50 笔（可撤销范围）
Deque<Stroke> redoStack;            // 已撤销的笔画
```

### 橡皮擦实现
- MVP 方案：白色画笔模拟 (`Color.WHITE`, alpha 255)
- 不使用 `PorterDuff.Mode.CLEAR`（对白色背景而言过度设计）

## 截图

<!-- TODO: 添加应用截图 -->

## 许可证

MIT License
