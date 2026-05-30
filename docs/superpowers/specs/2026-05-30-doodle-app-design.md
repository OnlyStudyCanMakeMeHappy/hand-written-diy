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
│   ├── PathManager.java           # 管理绘制的路径
│   ├── BrushManager.java          # 马克笔参数管理
│   └── HistoryManager.java        # 撤销/重做栈
├── ui
│   ├── ColorPickerDialog.java     # 颜色选择对话框
│   └── BrushSizeDialog.java       # 笔刷粗细对话框
└── utils
    └── FileSaver.java             # 保存图片到相册
```

### DrawingView 实现要点

- 继承 `View`（轻量级）或 `SurfaceView`（高性能）
- 重写 `onTouchEvent()` 处理触摸事件
- 使用 `Canvas` 和 `Paint` 绘制

### 触摸事件处理详解

```java
@Override
public boolean onTouchEvent(MotionEvent event) {
    float x = event.getX();
    float y = event.getY();

    switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN:
            // 手指按下，开始新路径
            currentPath = new Path();
            currentPath.moveTo(x, y);
            break;

        case MotionEvent.ACTION_MOVE:
            // 手指移动，添加线段
            currentPath.lineTo(x, y);
            invalidate();  // 触发重绘
            break;

        case MotionEvent.ACTION_UP:
            // 手指抬起，完成路径
            currentPath.lineTo(x, y);
            saveToHistory();  // 保存到历史记录
            break;
    }
    return true;
}
```

### MotionEvent 动作类型

| 动作 | 含义 | 使用场景 |
|------|------|---------|
| `ACTION_DOWN` | 手指首次触摸屏幕 | 开始一条新路径 (moveTo) |
| `ACTION_MOVE` | 手指在屏幕上移动 | 继续绘制路径 (lineTo) |
| `ACTION_UP` | 手指离开屏幕 | 完成路径，保存历史 |
| `ACTION_CANCEL` | 事件被取消（如来电） | 清理当前路径 |

### onDraw() 绘制流程

```java
@Override
protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);

    // 1. 绘制白色背景
    canvas.drawColor(Color.WHITE);

    // 2. 绘制所有历史路径
    for (DrawingPath dp : historyPaths) {
        paint.setColor(dp.isEraser ? Color.WHITE : dp.color);
        paint.setStrokeWidth(dp.width);
        paint.setAlpha(dp.isEraser ? 255 : 180);
        canvas.drawPath(dp.path, paint);
    }

    // 3. 绘制当前正在画的路径
    if (currentPath != null) {
        canvas.drawPath(currentPath, paint);
    }
}
```

**为什么调用 invalidate()？**
- `invalidate()` 请求系统重新调用 `onDraw()`
- 每次 `ACTION_MOVE` 都调用，实现实时绘制效果

### Paint 详细配置

```java
Paint paint = new Paint();
paint.setColor(currentColor);        // 设置画笔颜色
paint.setStrokeWidth(currentSize);   // 设置线条粗细（像素）
paint.setStyle(Paint.Style.STROKE);   // 只画轮廓，不填充
paint.setStrokeCap(Paint.Cap.ROUND); // 线条端点为圆形
paint.setAlpha(180);                  // 半透明效果（0-255，180约70%不透明）
```

**各参数说明：**

| 参数 | 作用 | 可选值说明 |
|------|------|-----------|
| `setStyle()` | 绘制模式 | `STROKE`（只画轮廓线）<br>`FILL`（填充形状）<br>`FILL_AND_STROKE`（两者） |
| `setStrokeCap()` | 线条端点样式 | `ROUND`（圆形端点，圆润自然）<br>`BUTT`（平端点，默认）<br>`SQUARE`（方形突出） |
| `setAlpha()` | 透明度 | 0=完全透明，255=完全不透明 |

**为什么用 ROUND：**
```
BUTT vs ROUND 对比：

BUTT:        ─────                (线条突然结束，有缺口)
ROUND:       ─────●               (线条圆润，手写更自然)
```
对于手写涂鸦，ROUND 让笔画连接更流畅，没有生硬的缺口。

### 撤销/重做详细设计

#### 数据结构

```java
// 历史记录管理器
class HistoryManager {
    private Stack<CanvasState> undoStack = new Stack<>();
    private Stack<CanvasState> redoStack = new Stack<>();
    private static final int MAX_HISTORY = 50;

    // 画布状态：包含所有已绘制的路径
    static class CanvasState {
        List<DrawingPath> paths = new ArrayList<>();

        // 深拷贝，防止引用被修改
        CanvasState copy() {
            CanvasState copy = new CanvasState();
            for (DrawingPath p : paths) {
                copy.paths.add(p.copy());
            }
            return copy;
        }
    }

    // 单条绘图路径
    static class DrawingPath {
        Path path;           // Android Path 对象，存储坐标点
        int color;           // 颜色值（ARGB）
        float width;         // 笔刷粗细
        boolean isEraser;    // 是否为橡皮擦模式
    }
}
```

#### 时序图

```
用户绘制中 → 用户抬起手指 (ACTION_UP)
    ↓
保存当前状态到 undoStack
    ↓
清空 redoStack（新操作使重做失效）
    ↓
检查栈容量，超过50则移除最旧记录
    ↓
更新撤销按钮状态（栈非空时可用）
```

#### 撤销操作流程

```java
void undo() {
    // 1. 检查是否可撤销
    if (undoStack.isEmpty()) return;

    // 2. 保存当前状态到 redoStack（以便重做）
    redoStack.push(getCurrentState());

    // 3. 从 undoStack 弹出上一个状态
    CanvasState prevState = undoStack.pop();

    // 4. 重绘画布
    redrawFromState(prevState);

    // 5. 更新按钮状态
    updateUndoRedoButtons();
}
```

#### 重做操作流程

```java
void redo() {
    // 1. 检查是否可重做
    if (redoStack.isEmpty()) return;

    // 2. 保存当前状态到 undoStack
    undoStack.push(getCurrentState());

    // 3. 从 redoStack 弹出状态
    CanvasState nextState = redoStack.pop();

    // 4. 重绘画布
    redrawFromState(nextState);

    // 5. 更新按钮状态
    updateUndoRedoButtons();
}
```

## 保存图片流程

### 详细步骤

```java
void saveToGallery() {
    // 1. 创建与画布等大的 Bitmap
    Bitmap bitmap = Bitmap.createBitmap(
        canvasView.getWidth(),
        canvasView.getHeight(),
        Bitmap.Config.ARGB_8888
    );

    // 2. 将画布内容绘制到 Bitmap
    Canvas canvas = new Canvas(bitmap);
    canvas.drawColor(Color.WHITE);  // 先填充白色背景
    canvasView.draw(canvas);        // 绘制所有路径

    // 3. 生成文件名（时间戳）
    String fileName = "Doodle_" + System.currentTimeMillis() + ".png";

    // 4. 通过 MediaStore 保存到相册
    ContentValues values = new ContentValues();
    values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
    values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
    values.put(MediaStore.Images.Media.RELATIVE_PATH,
                Environment.DIRECTORY_PICTURES + "/Doodle");

    Uri uri = contentResolver.insert(
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

    try (OutputStream out = contentResolver.openOutputStream(uri)) {
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
    }

    // 5. 通知媒体库更新
    Toast.makeText(this, "保存成功", Toast.LENGTH_SHORT).show();
}
```

### 为什么不用 WRITE_EXTERNAL_STORAGE 权限？

- Android 10 (API 29) 引入**分区存储 (Scoped Storage)**
- 应用只能访问自己的私有目录和通过 MediaStore 访问公共媒体
- 保存图片到 `Pictures` 目录通过 MediaStore API，无需额外权限
- 这比旧版方案（直接写文件路径）更安全、更规范

## 权限需求

| 权限 | 用途 | 版本 |
|------|------|------|
| 无 | Android 10+ 使用 scoped storage，无需额外权限 |

## 后续扩展可能

- 更多笔刷类型（钢笔、荧光笔）
- 图层支持
- 自定义背景模板
- 压感支持
