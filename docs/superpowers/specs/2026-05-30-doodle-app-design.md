# 涂鸦板 Android 应用设计文档

**日期**: 2026-05-30
**版本**: 1.1

## 项目概述

一个简洁的 Material Design 风格手写涂鸦应用，支持马克笔绘图、撤销/重做、颜色和粗细调节。

## 技术栈

| 项目 | 选择 |
|------|------|
| 语言 | Java |
| 包名 | `com.chenjinxiang.doodleboard` |
| 应用名 | 涂鸦板 |
| 构建 | Gradle (Android Studio 创建) |
| 最低 SDK | Android 10 (API 29) |
| 目标 SDK | Android 14 (API 34) |
| UI 框架 | Material Design Components |
| 屏幕方向 | 固定竖屏 (`portrait`) |
| 深色模式 | 不支持，固定浅色主题 |

## 核心功能

| 功能 | 描述 |
|------|------|
| 绘图画布 | 全屏画布，背景纯白，尺寸跟随 View |
| 马克笔 | 半透明笔刷，RoundCap，二阶贝塞尔平滑 |
| 颜色选择 | 8 个预设颜色 + 自定义颜色选择器 |
| 笔刷粗细 | 滑块 (1-50px) + 预设档位 (2/8/20/40px) |
| 橡皮擦 | PorterDuff.Mode.CLEAR 真正擦除 |
| 撤销/重做 | 最多撤销 50 笔，按钮触发 |
| 清空画布 | 确认对话框后清空所有笔画和历史 |
| 保存图片 | PNG 格式，实际画布分辨率，保存到系统 Pictures 目录 |

## 界面布局

```
┌───────────────────────────────────────────────┐
│  [≡] 涂鸦板                        [⋮]         │  顶部栏
├───────────────────────────────────────────────┤
│                                               │
│                                               │
│              画布区域（纯白背景）                │
│                                               │
│                                               │
├───────────────────────────────────────────────┤
│  [↶][↷] │ ●●●●●●●●[+] │ [size] │ [⏺] │ [🗑] │ [💾] │
│  撤销/重做  预设颜色 更多  粗细   橡皮   清空   保存│
└───────────────────────────────────────────────┘
```

- **顶部栏**: 应用名称、更多菜单
- **画布**: 占据主要屏幕空间
- **底部栏**: 撤销/重做、8个预设颜色+更多按钮、粗细、橡皮擦、清空、保存
- **选中状态**: 颜色下加点或边框高亮，粗细显示当前数值

## 预设颜色

| 颜色 | RGB |
|------|-----|
| 黑色 | #000000 |
| 红色 | #FF0000 |
| 蓝色 | #0000FF |
| 绿色 | #00FF00 |
| 黄色 | #FFFF00 |
| 橙色 | #FFA500 |
| 紫色 | #800080 |
| 棕色 | #8B4513 |

## 架构设计

```
com.chenjinxiang.doodleboard
├── MainActivity.java
├── view
│   └── DrawingView.java           # 自定义绘图 View
├── model
│   ├── Stroke.java                # 笔画数据类
│   ├── BrushManager.java          # 马克笔参数管理
│   └── HistoryManager.java        # 撤销/重做逻辑
├── ui
│   ├── ColorPickerDialog.java     # 自定义颜色选择对话框
│   └── BrushSizeDialog.java       # 笔刷粗细对话框
└── utils
    └── FileSaver.java             # 保存图片到相册
```

## 数据模型

### Stroke（不可变）

```java
public class Stroke {
    private final Path path;           // 路径轨迹（深拷贝）
    @ColorInt private final int color;  // 颜色 (RGB)
    private final int alpha;            // 透明度 (0-255)
    private final float width;          // 粗细
    private final boolean eraser;       // 是否是橡皮擦

    public Stroke(Path path, @ColorInt int color, int alpha, float width, boolean eraser) {
        this.path = new Path(path);     // 深拷贝 Path，确保不可变性
        this.color = color;
        this.alpha = alpha;
        this.width = width;
        this.eraser = eraser;
    }

    // Getters...
}
```

## DrawingView 实现

### 初始化

```java
public class DrawingView extends View {
    private Paint paint;
    private Path currentPath;
    private HistoryManager historyManager;
    private BrushManager brushManager;

    public DrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        historyManager = new HistoryManager();
        brushManager = new BrushManager();

        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStyle(Paint.Style.STROKE);
    }
}
```

### 触摸处理

```java
@Override
public boolean onTouchEvent(MotionEvent event) {
    switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN:
            currentPath = new Path();
            currentPath.moveTo(event.getX(), event.getY());
            break;

        case MotionEvent.ACTION_MOVE:
            // 二阶贝塞尔平滑处理
            // ...
            invalidate();  // 实时刷新
            break;

        case MotionEvent.ACTION_UP:
            // 创建 Stroke 并保存
            Stroke stroke = new Stroke(
                currentPath,
                brushManager.getColor(),
                brushManager.getAlpha(),
                brushManager.getWidth(),
                brushManager.isEraser()
            );
            historyManager.addStroke(stroke);
            invalidate();
            break;
    }
    return true;
}
```

### 绘制

```java
@Override
protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);

    // 1. 绘制白色背景
    canvas.drawColor(Color.WHITE);

    // 2. 绘制所有已保存的笔画
    for (Stroke stroke : historyManager.getStrokes()) {
        drawStroke(canvas, stroke);
    }

    // 3. 绘制当前正在画的笔画
    if (currentPath != null) {
        paint.setColor(brushManager.isEraser() ? Color.WHITE : brushManager.getColor());
        paint.setAlpha(brushManager.isEraser() ? 255 : brushManager.getAlpha());
        paint.setStrokeWidth(brushManager.getWidth());
        canvas.drawPath(currentPath, paint);
    }
}

private void drawStroke(Canvas canvas, Stroke stroke) {
    if (stroke.isEraser()) {
        // MVP: 橡皮擦使用白色画笔模拟
        paint.setColor(Color.WHITE);
        paint.setAlpha(255);
    } else {
        paint.setColor(stroke.getColor());
        paint.setAlpha(stroke.getAlpha());
    }
    paint.setStrokeWidth(stroke.getWidth());
    canvas.drawPath(stroke.getPath(), paint);
}
```

### 二阶贝塞尔平滑

```java
private void smoothPath(MotionEvent event) {
    float x = event.getX();
    float y = event.getY();

    // 使用上一点和当前点的中点作为控制点
    if (event.getAction() == MotionEvent.ACTION_MOVE) {
        float midX = (lastX + x) / 2;
        float midY = (lastY + y) / 2;
        currentPath.quadTo(lastX, lastY, midX, midY);
    }

    lastX = x;
    lastY = y;
}
```

## HistoryManager 实现

```java
public class HistoryManager {
    private static final int MAX_UNDO = 50;

    private final List<Stroke> strokes = new ArrayList<>();
    private final Deque<Stroke> undoableStrokes = new ArrayDeque<>();
    private final Deque<Stroke> redoStack = new ArrayDeque<>();

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

    public void clear() {
        strokes.clear();
        undoableStrokes.clear();
        redoStack.clear();
    }
}
```

## BrushManager 实现

```java
public class BrushManager {
    // 预设颜色（黑、红、蓝、绿、黄、橙、紫、棕）
    private static final int[] PRESET_COLORS = {
        Color.BLACK,       // 0
        Color.RED,         // 1
        Color.BLUE,        // 2
        Color.GREEN,       // 3
        Color.YELLOW,      // 4
        0xFFFFA500,        // 5 - 橙色
        0xFF800080,        // 6 - 紫色
        0xFF8B4513         // 7 - 棕色
    };

    // 预设粗细
    private static final float[] PRESET_SIZES = {2f, 8f, 20f, 40f};

    private int color = Color.BLACK;
    private int alpha = 180;  // 马克笔半透明
    private float width = 8f;
    private boolean eraser = false;

    // Getters and Setters...
    public void setPresetColor(int index) {
        if (index >= 0 && index < PRESET_COLORS.length) {
            this.color = PRESET_COLORS[index];
        }
    }

    public void setPresetSize(int index) {
        if (index >= 0 && index < PRESET_SIZES.length) {
            this.width = PRESET_SIZES[index];
        }
    }
}
```

## 保存流程

### 文件命名

格式：`涂鸦_YYYY-MM-DD_HHMMSS.png`

示例：`涂鸦_2026-05-30_143025.png`

### 保存步骤

1. 点击保存按钮
2. 从 DrawingView 获取 Bitmap
3. 使用 MediaStore API 保存到系统 Pictures 目录
4. 显示 Snackbar："已保存到相册" + "查看"按钮

### FileSaver 实现

```java
public class FileSaver {
    public static void saveToGallery(Context context, Bitmap bitmap) {
        String filename = "涂鸦_" + getTimestamp() + ".png";

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, filename);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
        values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);

        Uri uri = context.getContentResolver().insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        try (OutputStream out = context.getContentResolver().openOutputStream(uri)) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String getTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HHmmss", Locale.getDefault());
        return sdf.format(new Date());
    }
}
```

## 权限需求

Android 10+ (API 29+) 使用 Scoped Storage，使用 MediaStore API 无需声明任何权限。

## 清空画布确认

```java
private void showClearDialog() {
    new AlertDialog.Builder(this)
        .setTitle("清空画布")
        .setMessage("确定要清空所有内容吗？此操作不可撤销。")
        .setPositiveButton("清空", (dialog, which) -> {
            historyManager.clear();
            drawingView.invalidate();
        })
        .setNegativeButton("取消", null)
        .show();
}
```

## 橡皮擦视觉反馈

1. 底部工具栏橡皮擦按钮高亮
2. 显示 Toast："橡皮擦模式"
3. （后续）光标变为圆形指示当前粗细

## 粗细调节 UI

### 优先实现：对话框数值预览

```
┌─────────────────────┐
│   笔刷粗细          │
├─────────────────────┤
│  [●]━━━━━━━━━━○     │  滑块
│  当前粗细: 8px      │  数值显示
│                     │
│  [2px] [8px] [20px] │  预设档位
│  [40px]             │
├─────────────────────┤
│  [取消]     [确定]  │
└─────────────────────┘
```

### 后续实现：画布圆圈预览

在画布中央显示一个圆圈，实时反映当前粗细。

## 后续扩展可能

- 笔刷粗细画布圆圈预览
- 更多笔刷类型（钢笔、荧光笔）
- 自定义背景模板
- 压感支持
- 深色模式
- 横竖屏旋转支持
