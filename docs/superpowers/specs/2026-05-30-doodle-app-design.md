# 涂鸦板 Android 应用设计文档

**日期**: 2026-05-30
**版本**: 1.0

## 项目概述

一个简洁的 Material Design 风格手写涂鸦应用，支持马克笔绘图、撤销/重做、颜色和粗细调节。

## 技术栈

| 项目 | 选择 |
|------|------|
| 语言 | Java |
| 最低 SDK | Android 10 (API 29) |
| UI 框架 | Material Design Components |
| 目标 SDK | Android 14 (API 34) |

## 核心功能

| 功能 | 描述 |
|------|------|
| 绘图画布 | 全屏画布，背景纯白 |
| 马克笔 | 半透明笔刷效果，使用 RoundCap |
| 颜色选择 | 预设色板 + 自定义颜色选择器 |
| 笔刷粗细 | 滑块调节，范围 1-50px |
| 橡皮擦 | 可调节粗细的橡皮擦 |
| 撤销/重做 | 最多 50 步历史记录 |
| 清空画布 | 确认后清空所有内容 |
| 保存图片 | 导出为 PNG 格式到设备相册 |

## 界面布局

```
┌─────────────────────────────────────┐
│  [≡] 涂鸦板              [⋮]          │  顶部栏
├─────────────────────────────────────┤
│                                     │
│                                     │
│            画布区域（纯白背景）        │
│                                     │
│                                     │
├─────────────────────────────────────┤
│  [↶] [↷]  │  ● ● ● │ [⚪] [⚫] [💾]  │  底部工具栏
│  撤销/重做    颜色      橡皮/保存/清空│
└─────────────────────────────────────┘
```

- **顶部栏**: 应用名称、更多菜单
- **画布**: 占据主要屏幕空间
- **底部栏**: 撤销/重做、颜色、橡皮擦、保存、清空
- **侧边面板**: 点击颜色按钮弹出完整调色板和笔刷粗细滑块

## 架构设计

```
com.example.doodleapp
├── MainActivity.java
├── view
│   └── DrawingView.java           # 自定义绘图 View
├── model
│   ├── Stroke.java                # 笔画数据类（path + 属性）
│   ├── BrushManager.java          # 马克笔参数管理
│   └── HistoryManager.java        # 撤销/重做逻辑
├── ui
│   ├── ColorPickerDialog.java     # 颜色选择对话框
│   └── BrushSizeDialog.java       # 笔刷粗细对话框
└── utils
    └── FileSaver.java             # 保存图片到相册
```

### DrawingView 实现要点

- 继承 `View`
- 重写 `onTouchEvent()` 处理触摸事件
- 重写 `onDraw()` 绘制所有 Stroke
- `Paint` 设置：
  - `setStrokeCap(Paint.Cap.ROUND)`
  - `setAlpha(stroke.alpha)` 每笔独立透明度
  - `setStyle(Paint.Style.STROKE)`

### 橡皮擦实现

**MVP 方案**（第一版）：使用白色画笔模拟橡皮擦，适合纯白背景画布。

```java
if (stroke.eraser) {
    paint.setColor(Color.WHITE);
    paint.setAlpha(255);
} else {
    paint.setColor(stroke.color);
    paint.setAlpha(stroke.alpha);
}
```

**后续扩展**：若需支持透明背景或多种背景色，使用离屏 Bitmap + `PorterDuff.Mode.CLEAR` 真正擦除像素。

### 历史记录设计

```java
// Stroke 数据结构
class Stroke {
    Path path;       // 路径轨迹
    int color;       // 颜色 (RGB)
    int alpha;       // 透明度 (0-255)
    float width;     // 粗细
    boolean eraser;  // 是否是橡皮擦
}

// 历史管理
public class HistoryManager {
    private static final int MAX_UNDO = 50;

    private final List<Stroke> strokes = new ArrayList<>();           // 全部笔画
    private final Deque<Stroke> undoableStrokes = new ArrayDeque<>(); // 可撤销的笔画
    private final Deque<Stroke> redoStack = new ArrayDeque<>();       // 被撤销的笔画

    public void addStroke(Stroke stroke) {
        strokes.add(stroke);

        undoableStrokes.addLast(stroke);
        if (undoableStrokes.size() > MAX_UNDO) {
            undoableStrokes.removeFirst();
        }

        redoStack.clear();
    }

    public void undo() {
        if (!undoableStrokes.isEmpty()) {
            Stroke stroke = undoableStrokes.removeLast();
            strokes.remove(stroke);
            redoStack.addLast(stroke);
        }
    }

    public void redo() {
        if (!redoStack.isEmpty()) {
            Stroke stroke = redoStack.removeLast();
            strokes.add(stroke);

            undoableStrokes.addLast(stroke);
            if (undoableStrokes.size() > MAX_UNDO) {
                undoableStrokes.removeFirst();
            }
        }
    }

    public List<Stroke> getStrokes() {
        return strokes;
    }

    public boolean canUndo() {
        return !undoableStrokes.isEmpty();
    }

    public boolean canRedo() {
        return !redoStack.isEmpty();
    }
}
```

**行为示例**：
```
用户画了 100 笔：
- 画布上保留 100 笔
- 只能撤销最近 50 笔
- 前 50 笔已"固化"，不能撤销，但不会消失
```

### 马克笔效果

- Alpha 值: 180 (约 70% 不透明度)
- 多次叠加产生渐变效果
- 支持颜色混合模式

## 保存流程

1. 点击保存按钮
2. 检查权限 `WRITE_EXTERNAL_STORAGE` (Android 12+ 不需要)
3. 创建 Bitmap 并绘制画布内容
4. 使用 `MediaStore` API 保存到相册
5. 显示保存成功提示

## 权限需求

| 权限 | 用途 | 版本 |
|------|------|------|
| 无 | Android 10+ 使用 scoped storage，无需额外权限 |

## 后续扩展可能

- 更多笔刷类型（钢笔、荧光笔）
- 图层支持
- 自定义背景模板
- 压感支持
