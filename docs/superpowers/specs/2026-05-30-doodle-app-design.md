# 涂鸦板 Android 应用设计文档

**日期**: 2026-05-30
**版本**: 1.4
**变更**:
- v1.4 (2026-05-31): 移除橡皮擦模式 Toast 提示；修复粗细标签文本换行问题（>=10px）
- v1.3 (2026-05-31): Material 3 UI 修订；PaletteWheelView 完整实现（触摸交互、HSV 颜色计算、双向同步）
- v1.2 (2026-05-31): 现代简约 UI 设计，更新配色方案和间距系统
- v1.1 (2026-05-30): 初始设计

## v1.3 Material 3 UI 修订

本次 UI 修订以最新原型为准，覆盖旧版顶部栏和底部工具栏设计：

- **顶部栏**: 左侧为返回键，仅负责退出当前 Activity 回到桌面；中间不显示“未命名画板”或其他标题；右侧为清空按钮，图标使用垃圾桶样式，点击后弹出清空确认框。
- **底部工具栏**: 保留撤销、重做、画笔、橡皮、颜色、粗细、保存；移除“更多”按钮。
- **颜色入口**: 点击底部“颜色”直接打开调色盘，不再先展开简单颜色面板，也不通过“更多”进入调色盘。
- **调色盘 UI**: 调色盘需要包含色轮视觉、当前颜色预览、HEX 值、RGB 值、常用颜色快捷选择，以及可实际调节颜色的控件。第一阶段可用 RGB 滑杆承载实际颜色调整，但调色盘界面结构必须与原型一致。
  - **实现状态 (2026-05-31)**: ✅ 已完成 - `PaletteWheelView` 支持触摸交互、HSV 颜色计算、与 RGB 滑块双向同步
- **清空语义**: 清空画布只能由右上垃圾桶按钮触发；返回键不得清空画布。

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
│  [←]                              [🗑]        │  顶部栏
├───────────────────────────────────────────────┤
│                                               │
│                                               │
│              画布区域（纯白背景）                │
│                                               │
│                                               │
├───────────────────────────────────────────────┤
│  [↶][↷][笔][橡][色][8px][存]                 │
│  撤销/重做  画笔 橡皮 颜色 粗细 保存           │
└───────────────────────────────────────────────┘
```

- **顶部栏**: 左侧返回，右侧垃圾桶清空，中间不显示标题
- **画布**: 占据主要屏幕空间
- **底部栏**: 撤销/重做、画笔、橡皮擦、颜色、粗细、保存
- **选中状态**: 当前工具高亮，颜色显示当前色点，粗细显示当前数值

## 预设颜色

| 颜色 | 旧 RGB | 新 RGB（现代简约） |
|------|-------|-------------------|
| 黑色 | #000000 | #1A1A1A |
| 红色 | #FF0000 | #E63946 |
| 蓝色 | #0000FF | #457B9D |
| 绿色 | #00FF00 | #2A9D8F |
| 黄色 | #FFFF00 | #E9C46A |
| 橙色 | #FFA500 | #F4A261 |
| 紫色 | #800080 | #9B5DE5 |
| 棕色 | #8B4513 | #8D6E63 |

### 主题配色（现代简约风格）

| 用途 | 颜色值 | 说明 |
|------|--------|------|
| 主色 | #5B6BC7 | 柔和蓝紫 |
| 强调色 | #FF6B6B | 珊瑚红 |
| 背景 | #FAFAFA | 暖白色 |
| 画布 | #FFFFFF | 纯白 |
| 分割线 | #E8E4EC | 淡灰 |

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
│   ├── PaletteWheelView.java      # 颜色轮盘选择器 (v1.3)
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
    // 预设颜色（黑、红、蓝、绿、黄、橙、紫、棕）- 现代简约配色
    private static final int[] PRESET_COLORS = {
        0xFF1A1A1A,       // 0 - 黑色
        0xFFE63946,       // 1 - 红色
        0xFF457B9D,       // 2 - 蓝色
        0xFF2A9D8F,       // 3 - 绿色
        0xFFE9C46A,       // 4 - 黄色
        0xFFF4A261,       // 5 - 橙色
        0xFF9B5DE5,       // 6 - 紫色
        0xFF8D6E63        // 7 - 棕色
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
2. ~~显示 Toast："橡皮擦模式"~~ (v1.4 已移除)
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

- **Activity 状态持久化**：防止返回后画板清空（需实现 onSaveInstanceState/onRestoreInstanceState）
- 笔刷粗细画布圆圈预览
- 更多笔刷类型（钢笔、荧光笔）
- 自定义背景模板
- 压感支持
- 深色模式
- 横竖屏旋转支持
