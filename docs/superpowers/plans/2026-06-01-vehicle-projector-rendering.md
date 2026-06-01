# 车辆投影实时渲染实施计划

> **给执行代理的要求:** 实施本计划时必须使用 `superpowers:subagent-driven-development`（推荐）或 `superpowers:executing-plans`，并按任务逐项执行。所有步骤使用复选框（`- [ ]`）追踪进度。

**目标:** 让涂鸦板运行在车辆中控屏上，同时把同一幅画布实时渲染到车辆投影仪。

**架构:** 第一阶段优先走 Android 第二屏方案：使用 `DisplayManager` 检测投影显示设备，使用 `Presentation` 承载投影窗口，并用只读的投影视图渲染与中控屏 `DrawingView` 相同的笔迹状态。中控屏仍然是唯一输入端，投影端只负责输出，并按自身分辨率等比缩放画布。

**技术栈:** Java、Android SDK、`DisplayManager`、`Presentation`、Canvas/Path/Paint 渲染、现有 `DrawingView` / `HistoryManager` / `Stroke` 模型。

---

## 文件结构

- 新建 `app/src/main/java/com/chenjinxiang/doodleboard/render/DoodleRenderer.java`
  - 集中处理 Canvas 绘制逻辑，保证中控屏和投影屏使用完全一致的渲染代码。
- 修改 `app/src/main/java/com/chenjinxiang/doodleboard/view/DrawingView.java`
  - 将绘制逻辑委托给 `DoodleRenderer`。
  - 对外暴露当前正在绘制笔迹的只读快照。
  - 在绘制、移动、撤销、重做、清空时发出画面变化通知。
- 新建 `app/src/main/java/com/chenjinxiang/doodleboard/projection/ProjectorDrawingView.java`
  - 显示在投影仪上的只读 View。
  - 读取中控屏 `DrawingView` 的状态，并按投影尺寸缩放。
- 新建 `app/src/main/java/com/chenjinxiang/doodleboard/projection/DrawingPresentation.java`
  - Android `Presentation` 封装类，负责投影显示窗口。
- 新建 `app/src/main/java/com/chenjinxiang/doodleboard/projection/ProjectorController.java`
  - 负责显示设备发现、显示生命周期、`Presentation` 生命周期和投影刷新。
- 修改 `app/src/main/java/com/chenjinxiang/doodleboard/MainActivity.java`
  - 启停 `ProjectorController`。
  - 将画面变化转发到投影端。

---

## 任务 1：抽取共享渲染器

**文件:**
- 新建: `app/src/main/java/com/chenjinxiang/doodleboard/render/DoodleRenderer.java`
- 修改: `app/src/main/java/com/chenjinxiang/doodleboard/view/DrawingView.java`

- [ ] **步骤 1：创建渲染器类**

创建 `DoodleRenderer.java`：

```java
package com.chenjinxiang.doodleboard.render;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;

import com.chenjinxiang.doodleboard.model.Stroke;

import java.util.List;

public class DoodleRenderer {
    private final Paint strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public DoodleRenderer() {
        strokePaint.setStrokeCap(Paint.Cap.ROUND);
        strokePaint.setStrokeJoin(Paint.Join.ROUND);
        strokePaint.setStyle(Paint.Style.STROKE);
    }

    public void render(
        Canvas canvas,
        List<Stroke> strokes,
        Path currentPath,
        int currentColor,
        int currentAlpha,
        float currentWidth,
        boolean currentEraser
    ) {
        canvas.drawColor(Color.WHITE);
        for (Stroke stroke : strokes) {
            drawStroke(canvas, stroke);
        }
        if (currentPath != null) {
            strokePaint.setColor(currentEraser ? Color.WHITE : currentColor);
            strokePaint.setAlpha(currentEraser ? 255 : currentAlpha);
            strokePaint.setStrokeWidth(currentWidth);
            canvas.drawPath(currentPath, strokePaint);
        }
    }

    private void drawStroke(Canvas canvas, Stroke stroke) {
        strokePaint.setColor(stroke.isEraser() ? Color.WHITE : stroke.getColor());
        strokePaint.setAlpha(stroke.isEraser() ? 255 : stroke.getAlpha());
        strokePaint.setStrokeWidth(stroke.getWidth());
        canvas.drawPath(stroke.getPath(), strokePaint);
    }
}
```

- [ ] **步骤 2：替换 `DrawingView` 中重复的渲染逻辑**

在 `DrawingView` 中添加字段：

```java
private DoodleRenderer renderer;
```

在 `init()` 中初始化：

```java
renderer = new DoodleRenderer();
```

在 `onDraw(Canvas canvas)` 中，用下面代码替换白色背景、历史笔迹和当前笔迹的绘制逻辑：

```java
renderer.render(
    canvas,
    historyManager.getStrokes(),
    currentPath,
    brushManager.getColor(),
    brushManager.getAlpha(),
    brushManager.getWidth(),
    brushManager.isEraser()
);
```

橡皮擦光标绘制逻辑保留在这段代码之后。

- [ ] **步骤 3：更新 `getCanvasBitmap()`**

将 `getCanvasBitmap()` 中的手动绘制替换为相同的渲染器调用：

```java
renderer.render(
    canvas,
    historyManager.getStrokes(),
    null,
    brushManager.getColor(),
    brushManager.getAlpha(),
    brushManager.getWidth(),
    brushManager.isEraser()
);
```

- [ ] **步骤 4：构建验证**

运行：

```bash
./gradlew :app:assembleDebug
```

预期结果：`BUILD SUCCESSFUL`。

---

## 任务 2：暴露安全的绘图快照

**文件:**
- 修改: `app/src/main/java/com/chenjinxiang/doodleboard/view/DrawingView.java`

- [ ] **步骤 1：添加画面变化监听器**

在 `DrawingView` 内部添加接口：

```java
public interface OnDrawingChangeListener {
    void onDrawingChanged();
}
```

添加字段：

```java
private OnDrawingChangeListener drawingChangeListener;
```

添加 setter：

```java
public void setOnDrawingChangeListener(OnDrawingChangeListener listener) {
    this.drawingChangeListener = listener;
}
```

添加通知方法：

```java
private void notifyDrawingChanged() {
    if (drawingChangeListener != null) {
        drawingChangeListener.onDrawingChanged();
    }
}
```

- [ ] **步骤 2：实时绘制时发出通知**

在以下方法中，每次 `invalidate()` 后调用 `notifyDrawingChanged()`：

- `handleActionDown`
- `handleActionMove`
- `handleActionUp`
- `clear`

撤销和重做由 `MainActivity` 调用后续新增的公共通知方法触发刷新。

- [ ] **步骤 3：添加投影端只读访问方法**

在 `DrawingView` 中添加：

```java
public Path getCurrentPathSnapshot() {
    return currentPath == null ? null : new Path(currentPath);
}

public int getCurrentRenderColor() {
    return brushManager.getColor();
}

public int getCurrentRenderAlpha() {
    return brushManager.getAlpha();
}

public float getCurrentRenderWidth() {
    return brushManager.getWidth();
}

public boolean isCurrentRenderEraser() {
    return brushManager.isEraser();
}

public void notifyExternalDrawingChanged() {
    notifyDrawingChanged();
}
```

- [ ] **步骤 4：构建验证**

运行：

```bash
./gradlew :app:assembleDebug
```

预期结果：`BUILD SUCCESSFUL`。

---

## 任务 3：新增投影端只读画布 View

**文件:**
- 新建: `app/src/main/java/com/chenjinxiang/doodleboard/projection/ProjectorDrawingView.java`

- [ ] **步骤 1：创建投影视图**

创建：

```java
package com.chenjinxiang.doodleboard.projection;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

import com.chenjinxiang.doodleboard.render.DoodleRenderer;
import com.chenjinxiang.doodleboard.view.DrawingView;

public class ProjectorDrawingView extends View {
    private final DoodleRenderer renderer = new DoodleRenderer();
    private DrawingView sourceView;

    public ProjectorDrawingView(Context context) {
        super(context);
    }

    public ProjectorDrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void bindSource(DrawingView sourceView) {
        this.sourceView = sourceView;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (sourceView == null) {
            canvas.drawColor(android.graphics.Color.WHITE);
            return;
        }

        float sourceWidth = Math.max(1f, sourceView.getWidth());
        float sourceHeight = Math.max(1f, sourceView.getHeight());
        float scale = Math.min(getWidth() / sourceWidth, getHeight() / sourceHeight);
        float dx = (getWidth() - sourceWidth * scale) / 2f;
        float dy = (getHeight() - sourceHeight * scale) / 2f;

        canvas.save();
        canvas.translate(dx, dy);
        canvas.scale(scale, scale);
        renderer.render(
            canvas,
            sourceView.getHistoryManager().getStrokes(),
            sourceView.getCurrentPathSnapshot(),
            sourceView.getCurrentRenderColor(),
            sourceView.getCurrentRenderAlpha(),
            sourceView.getCurrentRenderWidth(),
            sourceView.isCurrentRenderEraser()
        );
        canvas.restore();
    }
}
```

- [ ] **步骤 2：构建验证**

运行：

```bash
./gradlew :app:assembleDebug
```

预期结果：`BUILD SUCCESSFUL`。

---

## 任务 4：新增 Android Presentation 层

**文件:**
- 新建: `app/src/main/java/com/chenjinxiang/doodleboard/projection/DrawingPresentation.java`

- [ ] **步骤 1：创建 Presentation 类**

创建：

```java
package com.chenjinxiang.doodleboard.projection;

import android.app.Presentation;
import android.content.Context;
import android.os.Bundle;
import android.view.Display;

import com.chenjinxiang.doodleboard.view.DrawingView;

public class DrawingPresentation extends Presentation {
    private final DrawingView sourceView;
    private ProjectorDrawingView projectorView;

    public DrawingPresentation(Context context, Display display, DrawingView sourceView) {
        super(context, display);
        this.sourceView = sourceView;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        projectorView = new ProjectorDrawingView(getContext());
        projectorView.bindSource(sourceView);
        setContentView(projectorView);
    }

    public void refresh() {
        if (projectorView != null) {
            projectorView.invalidate();
        }
    }
}
```

- [ ] **步骤 2：构建验证**

运行：

```bash
./gradlew :app:assembleDebug
```

预期结果：`BUILD SUCCESSFUL`。

---

## 任务 5：新增投影控制器

**文件:**
- 新建: `app/src/main/java/com/chenjinxiang/doodleboard/projection/ProjectorController.java`

- [ ] **步骤 1：创建控制器**

创建：

```java
package com.chenjinxiang.doodleboard.projection;

import android.content.Context;
import android.hardware.display.DisplayManager;
import android.view.Display;

import com.chenjinxiang.doodleboard.view.DrawingView;

public class ProjectorController {
    private final Context context;
    private final DrawingView sourceView;
    private final DisplayManager displayManager;
    private DrawingPresentation presentation;

    private final DisplayManager.DisplayListener displayListener =
        new DisplayManager.DisplayListener() {
            @Override
            public void onDisplayAdded(int displayId) {
                startIfAvailable();
            }

            @Override
            public void onDisplayRemoved(int displayId) {
                if (presentation != null && presentation.getDisplay().getDisplayId() == displayId) {
                    stop();
                    startIfAvailable();
                }
            }

            @Override
            public void onDisplayChanged(int displayId) {
                invalidate();
            }
        };

    public ProjectorController(Context context, DrawingView sourceView) {
        this.context = context;
        this.sourceView = sourceView;
        this.displayManager = (DisplayManager) context.getSystemService(Context.DISPLAY_SERVICE);
    }

    public void start() {
        displayManager.registerDisplayListener(displayListener, null);
        startIfAvailable();
    }

    public void stop() {
        if (presentation != null) {
            presentation.dismiss();
            presentation = null;
        }
    }

    public void destroy() {
        stop();
        displayManager.unregisterDisplayListener(displayListener);
    }

    public void invalidate() {
        if (presentation != null) {
            presentation.refresh();
        }
    }

    private void startIfAvailable() {
        if (presentation != null) {
            return;
        }
        Display[] displays = displayManager.getDisplays(DisplayManager.DISPLAY_CATEGORY_PRESENTATION);
        if (displays.length == 0) {
            return;
        }
        presentation = new DrawingPresentation(context, displays[0], sourceView);
        presentation.show();
    }
}
```

- [ ] **步骤 2：构建验证**

运行：

```bash
./gradlew :app:assembleDebug
```

预期结果：`BUILD SUCCESSFUL`。

---

## 任务 6：接入 MainActivity 生命周期和绘图事件

**文件:**
- 修改: `app/src/main/java/com/chenjinxiang/doodleboard/MainActivity.java`

- [ ] **步骤 1：添加控制器字段**

添加 import：

```java
import com.chenjinxiang.doodleboard.projection.ProjectorController;
```

添加字段：

```java
private ProjectorController projectorController;
```

- [ ] **步骤 2：初始化控制器**

在 `initViews()` 末尾添加：

```java
projectorController = new ProjectorController(this, drawingView);
```

在 `drawingView.setOnHistoryChangeListener(this::updateButtonStates);` 后添加：

```java
drawingView.setOnDrawingChangeListener(() -> {
    if (projectorController != null) {
        projectorController.invalidate();
    }
});
```

- [ ] **步骤 3：随 Activity 生命周期启停投影**

添加：

```java
@Override
protected void onStart() {
    super.onStart();
    if (projectorController != null) {
        projectorController.start();
    }
}

@Override
protected void onStop() {
    if (projectorController != null) {
        projectorController.stop();
    }
    super.onStop();
}

@Override
protected void onDestroy() {
    if (projectorController != null) {
        projectorController.destroy();
    }
    super.onDestroy();
}
```

- [ ] **步骤 4：撤销、重做、清空时刷新投影**

在 `undoDrawing()` 和 `redoDrawing()` 中，`drawingView.invalidate()` 后添加：

```java
drawingView.notifyExternalDrawingChanged();
```

在清空确认框的 positive action 中，`drawingView.clear()` 后添加：

```java
drawingView.notifyExternalDrawingChanged();
```

- [ ] **步骤 5：构建验证**

运行：

```bash
./gradlew :app:assembleDebug
```

预期结果：`BUILD SUCCESSFUL`。

---

## 任务 7：硬件手动验证

**文件:**
- 不改代码。

- [ ] **步骤 1：无投影设备验证**

在普通 Android 设备或模拟器上运行应用。

预期结果：
- 应用正常打开。
- 绘画、撤销、重做、清空、保存仍然可用。
- 没有第二屏时应用不崩溃。

- [ ] **步骤 2：Android 第二屏投影验证**

使用投影仪会以 presentation display 暴露的设备或车机系统。

预期结果：
- 打开应用后，投影仪自动显示白色画布。
- 在中控屏绘画时，手指移动过程中投影端实时显示，而不是只在抬手后显示。
- 撤销、重做、清空会立即同步到投影端。
- 投影内容居中显示，并保持画布比例。

- [ ] **步骤 3：热插拔验证**

应用打开时连接和断开投影仪。

预期结果：
- 连接：投影视图出现。
- 断开：中控屏应用继续运行。
- 重新连接：投影视图再次出现。

---

## 投影仪不是 Android 第二屏时的后备方案

如果真实车机硬件上 `DisplayManager.getDisplays(DisplayManager.DISPLAY_CATEGORY_PRESENTATION)` 返回空，不要重写绘图逻辑。保留 `DoodleRenderer`，新增一种传输实现：

- 渲染到固定尺寸的离屏 `Bitmap` 或 `Surface`。
- 仅在笔迹变化时使用 `MediaCodec` 编码帧。
- 通过 OEM 投影 SDK、RTSP/WebRTC 或本地 HDMI 编码接口发送画面。

该后备方案应在确认真实投影硬件 API 后，单独制定实施计划。

---

## 验证清单

- `./gradlew :app:assembleDebug` 通过。
- 没有外接显示设备时应用不崩溃。
- 外接显示设备能在 `ACTION_MOVE` 期间收到当前正在绘制的笔迹。
- 撤销、重做、清空能立即同步到投影端。
- 投影渲染保持画布比例。
- 应用能处理投影设备热插拔。
