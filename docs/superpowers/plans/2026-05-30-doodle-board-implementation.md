# 涂鸦板 Android 应用实施计划

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 创建一个简洁的 Material Design 风格手写涂鸦 Android 应用，支持马克笔绘图、撤销/重做、颜色和粗细调节、橡皮擦、清空和保存功能。

**Architecture:** 采用 MV 模式，MainActivity 负责 UI 协调，DrawingView 处理绘图逻辑，HistoryManager 管理撤销/重做状态，BrushManager 管理笔刷参数。所有笔画存储为不可变的 Stroke 对象。

**Tech Stack:** Java, Android SDK 29+, Material Design Components, Canvas/Path/Paint API

---

## 文件结构

```
com.chenjinxiang.doodleboard
├── MainActivity.java                    # 主 Activity
├── view
│   └── DrawingView.java                 # 自定义绘图 View
├── model
│   ├── Stroke.java                      # 笔画数据类（不可变）
│   ├── BrushManager.java                # 笔刷参数管理
│   └── HistoryManager.java               # 撤销/重做逻辑
├── ui
│   ├── ColorPickerDialog.java           # 自定义颜色选择对话框
│   └── BrushSizeDialog.java             # 笔刷粗细对话框
└── utils
    └── FileSaver.java                   # 保存图片到相册

res/
├── layout/
│   ├── activity_main.xml                # 主界面布局
│   ├── dialog_color_picker.xml         # 颜色选择对话框布局
│   └── dialog_brush_size.xml           # 粗细调节对话框布局
├── values/
│   ├── colors.xml                       # 颜色定义
│   ├── strings.xml                      # 字符串资源
│   └── dimens.xml                       # 尺寸定义
└── drawable/
    ├── ic_undo.xml                      # 撤销图标
    ├── ic_redo.xml                      # 重做图标
    ├── ic_eraser.xml                    # 橡皮擦图标
    ├── ic_clear.xml                     # 清空图标
    └── ic_save.xml                      # 保存图标

AndroidManifest.xml                      # 应用清单文件
build.gradle (app level)                 # 应用级构建配置
```

---

### Task 1: 项目初始化和配置

**Files:**
- Create: `app/src/main/AndroidManifest.xml`
- Create: `app/build.gradle`
- Create: `build.gradle` (project level)
- Create: `settings.gradle`
- Create: `gradle.properties`

- [ ] **Step 1: 创建 project level build.gradle**

```groovy
// build.gradle (project level)
buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath 'com.android.tools.build:gradle:8.1.0'
    }
}

allprojects {
    repositories {
        google()
        mavenCentral()
    }
}

task clean(type: Delete) {
    delete rootProject.buildDir
}
```

- [ ] **Step 2: 创建 settings.gradle**

```groovy
// settings.gradle
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "DoodleBoard"
include ':app'
```

- [ ] **Step 3: 创建 app 级 build.gradle**

```groovy
// app/build.gradle
plugins {
    id 'com.android.application'
}

android {
    namespace 'com.chenjinxiang.doodleboard'
    compileSdk 34

    defaultConfig {
        applicationId "com.chenjinxiang.doodleboard"
        minSdk 29
        targetSdk 34
        versionCode 1
        versionName "1.0"

        testInstrumentationRunner "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            minifyEnabled false
            proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
        }
    }

    compileOptions {
        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation 'androidx.appcompat:appcompat:1.6.1'
    implementation 'com.google.android.material:material:1.9.0'
    implementation 'androidx.constraintlayout:constraintlayout:2.1.4'
    testImplementation 'junit:junit:4.13.2'
    androidTestImplementation 'androidx.test.ext:junit:1.1.5'
    androidTestImplementation 'androidx.test.espresso:espresso-core:3.5.1'
}
```

- [ ] **Step 4: 创建 AndroidManifest.xml**

```xml
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <application
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:roundIcon="@mipmap/ic_launcher_round"
        android:supportsRtl="true"
        android:theme="@style/Theme.Material3.Light.NoActionBar">

        <activity
            android:name=".MainActivity"
            android:exported="true"
            android:screenOrientation="portrait">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />
                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>

</manifest>
```

- [ ] **Step 5: 创建 gradle.properties**

```properties
# gradle.properties
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
android.useAndroidX=true
android.enableJetifier=true
```

- [ ] **Step 6: 提交项目初始化**

```bash
git add build.gradle settings.gradle app/build.gradle gradle.properties
git commit -m "feat: initialize project structure and Gradle configuration"
```

---

### Task 2: 创建资源文件

**Files:**
- Create: `app/src/main/res/values/colors.xml`
- Create: `app/src/main/res/values/strings.xml`
- Create: `app/src/main/res/values/dimens.xml`
- Create: `app/src/main/res/values/themes.xml`

- [ ] **Step 1: 创建 colors.xml（预设颜色和主题颜色）**

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <!-- 预设画笔颜色 -->
    <color name="brush_black">#000000</color>
    <color name="brush_red">#FF0000</color>
    <color name="brush_blue">#0000FF</color>
    <color name="brush_green">#00FF00</color>
    <color name="brush_yellow">#FFFF00</color>
    <color name="brush_orange">#FFA500</color>
    <color name="brush_purple">#800080</color>
    <color name="brush_white">#FFFFFF</color>

    <!-- 主题颜色 -->
    <color name="primary">#6200EE</color>
    <color name="primary_dark">#3700B3</color>
    <color name="accent">#03DAC5</color>
    <color name="canvas_white">#FFFFFF</color>
    <color name="toolbar_bg">#FFFFFF</color>
    <color name="toolbar_stroke">#E0E0E0</color>
    <color name="selected_indicator">#6200EE</color>
</resources>
```

- [ ] **Step 2: 创建 strings.xml**

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <string name="app_name">涂鸦板</string>

    <!-- 工具栏 -->
    <string name="undo">撤销</string>
    <string name="redo">重做</string>
    <string name="eraser">橡皮擦</string>
    <string name="clear">清空</string>
    <string name="save">保存</string>
    <string name="more_colors">更多颜色</string>
    <string name="brush_size">笔刷粗细</string>

    <!-- 对话框 -->
    <string name="clear_dialog_title">清空画布</string>
    <string name="clear_dialog_message">确定要清空所有内容吗？此操作不可撤销。</string>
    <string name="clear_confirm">清空</string>
    <string name="cancel">取消</string>
    <string name="confirm">确定</string>

    <!-- 提示信息 -->
    <string name="eraser_mode">橡皮擦模式</string>
    <string name="draw_mode">绘图模式</string>
    <string name="saved_to_gallery">已保存到相册</string>
    <string name="view">查看</string>
    <string name="current_size">当前粗细: %dpx</string>

    <!-- 颜色选择 -->
    <string name="select_color">选择颜色</string>
</resources>
```

- [ ] **Step 3: 创建 dimens.xml**

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <!-- 工具栏 -->
    <dimen name="toolbar_height">56dp</dimen>
    <dimen name="toolbar_padding">8dp</dimen>
    <dimen name="button_size">48dp</dimen>

    <!-- 颜色选择器 -->
    <dimen name="color_circle_size">32dp</dimen>
    <dimen name="color_circle_selected_stroke">3dp</dimen>
    <dimen name="color_spacing">8dp</dimen>

    <!-- 对话框 -->
    <dimen name="dialog_padding">24dp</dimen>
    <dimen name="dialog_button_height">48dp</dimen>

    <!-- 笔刷 -->
    <dimen name="brush_size_indicator">80dp</dimen>
</resources>
```

- [ ] **Step 4: 创建 themes.xml**

```xml
<?xml version="1.0" encoding="utf-8"?>
<resources>
    <style name="Theme.DoodleBoard" parent="Theme.Material3.Light.NoActionBar">
        <item name="colorPrimary">@color/primary</item>
        <item name="colorPrimaryDark">@color/primary_dark</item>
        <item name="colorAccent">@color/accent</item>
        <item name="android:statusBarColor">@color/primary</item>
        <item name="android:windowLightStatusBar">true</item>
    </style>
</resources>
```

- [ ] **Step 5: 提交资源文件**

```bash
git add app/src/main/res/
git commit -m "feat: add colors, strings, dimens and theme resources"
```

---

### Task 3: 创建图标资源

**Files:**
- Create: `app/src/main/res/drawable/ic_undo.xml`
- Create: `app/src/main/res/drawable/ic_redo.xml`
- Create: `app/src/main/res/drawable/ic_eraser.xml`
- Create: `app/src/main/res/drawable/ic_clear.xml`
- Create: `app/src/main/res/drawable/ic_save.xml`
- Create: `app/src/main/res/drawable/ic_add.xml`
- Create: `app/src/main/res/drawable/ic_color_indicator.xml`

- [ ] **Step 1: 创建撤销图标 ic_undo.xml**

```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24">
    <path
        android:fillColor="#000000"
        android:pathData="M12.5,8c-2.65,0 -5.05,0.99 -6.9,2.6L2,7v9h9l-3.62,-3.62c1.39,-1.16 3.16,-1.88 5.12,-1.88 3.54,0 6.55,2.31 7.6,5.5l2.37,-0.78C21.08,11.03 17.15,8 12.5,8z"/>
</vector>
```

- [ ] **Step 2: 创建重做图标 ic_redo.xml**

```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24">
    <path
        android:fillColor="#000000"
        android:pathData="M18.4,10.6C16.55,9 14.15,8 11.5,8c-4.65,0 -8.58,3.03 -9.96,7.22L3.9,16c1.05,-3.19 4.05,-5.5 7.6,-5.5 1.95,0 3.73,0.72 5.12,1.88L13,16h9V7l-3.6,3.6z"/>
</vector>
```

- [ ] **Step 3: 创建橡皮擦图标 ic_eraser.xml**

```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24">
    <path
        android:fillColor="#000000"
        android:pathData="M16.24,3.56l4.95,4.94c0.78,0.79 0.78,2.05 0,2.84L12,20.53a4.008,4.008 0,0 1 -5.66,0L2.81,17c-0.78,-0.79 -0.78,-2.05 0,-2.84l10.6,-10.6c0.79,-0.78 2.05,-0.78 2.83,0zM4.22,15.58l3.54,3.53c0.78,0.79 2.04,0.79 2.83,0l8.48,-8.48 -3.54,-3.54 -11.31,11.31zM17,2v2h-3v3h-2V4h-3V2h5z"/>
</vector>
```

- [ ] **Step 4: 创建清空图标 ic_clear.xml**

```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24">
    <path
        android:fillColor="#000000"
        android:pathData="M6,19c0,1.1 0.9,2 2,2h8c1.1,0 2,-0.9 2,-2V7H6v12zM19,4h-3.5l-1,-1h-5l-1,1H5v2h14V4z"/>
</vector>
```

- [ ] **Step 5: 创建保存图标 ic_save.xml**

```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24">
    <path
        android:fillColor="#000000"
        android:pathData="M17,3L5,3c-1.11,0 -2,0.9 -2,2v14c0,1.1 0.89,2 2,2h14c1.1,0 2,-0.9 2,-2L21,7l-4,-4zM12,19c-1.66,0 -3,-1.34 -3,-3s1.34,-3 3,-3 3,1.34 3,3 -1.34,3 -3,3zM15,9L5,9L5,5h10v4z"/>
</vector>
```

- [ ] **Step 6: 创建添加按钮图标 ic_add.xml**

```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24">
    <path
        android:fillColor="#000000"
        android:pathData="M19,13h-6v6h-2v-6H5v-2h6V5h2v6h6v2z"/>
</vector>
```

- [ ] **Step 7: 创建颜色选择指示器 ic_color_indicator.xml**

```xml
<shape xmlns:android="http://schemas.android.com/apk/res/android"
    android:shape="oval">
    <solid android:color="@color/selected_indicator"/>
    <stroke
        android:width="2dp"
        android:color="@color/canvas_white"/>
</shape>
```

- [ ] **Step 8: 提交图标资源**

```bash
git add app/src/main/res/drawable/
git commit -m "feat: add toolbar icons"
```

---

### Task 4: 创建 Stroke 数据模型

**Files:**
- Create: `app/src/main/java/com/chenjinxiang/doodleboard/model/Stroke.java`

- [ ] **Step 1: 创建 Stroke.java（不可变数据类）**

```java
package com.chenjinxiang.doodleboard.model;

import android.graphics.Path;
import androidx.annotation.ColorInt;

/**
 * 笔画数据类（不可变）
 * 包含路径、颜色、透明度、粗细和橡皮擦状态
 */
public class Stroke {
    private final Path path;
    @ColorInt
    private final int color;
    private final int alpha;
    private final float width;
    private final boolean eraser;

    public Stroke(Path path, @ColorInt int color, int alpha, float width, boolean eraser) {
        this.path = path;
        this.color = color;
        this.alpha = alpha;
        this.width = width;
        this.eraser = eraser;
    }

    public Path getPath() {
        return path;
    }

    @ColorInt
    public int getColor() {
        return color;
    }

    public int getAlpha() {
        return alpha;
    }

    public float getWidth() {
        return width;
    }

    public boolean isEraser() {
        return eraser;
    }
}
```

- [ ] **Step 2: 提交 Stroke 类**

```bash
git add app/src/main/java/com/chenjinxiang/doodleboard/model/Stroke.java
git commit -m "feat: add immutable Stroke data class"
```

---

### Task 5: 创建 HistoryManager

**Files:**
- Create: `app/src/main/java/com/chenjinxiang/doodleboard/model/HistoryManager.java`

- [ ] **Step 1: 创建 HistoryManager.java**

```java
package com.chenjinxiang.doodleboard.model;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

/**
 * 历史记录管理器
 * 负责撤销/重做逻辑，最多可撤销 50 笔
 */
public class HistoryManager {
    private static final int MAX_UNDO = 50;

    private final List<Stroke> strokes = new ArrayList<>();
    private final Deque<Stroke> undoableStrokes = new ArrayDeque<>();
    private final Deque<Stroke> redoStack = new ArrayDeque<>();

    /**
     * 添加新笔画
     */
    public void addStroke(Stroke stroke) {
        strokes.add(stroke);
        undoableStrokes.addLast(stroke);
        if (undoableStrokes.size() > MAX_UNDO) {
            undoableStrokes.removeFirst();
        }
        redoStack.clear();
    }

    /**
     * 撤销
     */
    public void undo() {
        if (!undoableStrokes.isEmpty()) {
            Stroke stroke = undoableStrokes.removeLast();
            strokes.remove(stroke);
            redoStack.addLast(stroke);
        }
    }

    /**
     * 重做
     */
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

    /**
     * 获取所有笔画
     */
    public List<Stroke> getStrokes() {
        return strokes;
    }

    /**
     * 是否可以撤销
     */
    public boolean canUndo() {
        return !undoableStrokes.isEmpty();
    }

    /**
     * 是否可以重做
     */
    public boolean canRedo() {
        return !redoStack.isEmpty();
    }

    /**
     * 清空所有笔画和历史
     */
    public void clear() {
        strokes.clear();
        undoableStrokes.clear();
        redoStack.clear();
    }
}
```

- [ ] **Step 2: 提交 HistoryManager**

```bash
git add app/src/main/java/com/chenjinxiang/doodleboard/model/HistoryManager.java
git commit -m "feat: add HistoryManager with undo/redo logic"
```

---

### Task 6: 创建 BrushManager

**Files:**
- Create: `app/src/main/java/com/chenjinxiang/doodleboard/model/BrushManager.java`

- [ ] **Step 1: 创建 BrushManager.java**

```java
package com.chenjinxiang.doodleboard.model;

import android.graphics.Color;

/**
 * 笔刷管理器
 * 管理笔刷颜色、粗细、透明度和橡皮擦状态
 */
public class BrushManager {
    // 预设颜色（黑、红、蓝、绿、黄、橙、紫、白）
    public static final int[] PRESET_COLORS = {
        Color.BLACK,      // 0
        Color.RED,        // 1
        Color.BLUE,       // 2
        Color.GREEN,      // 3
        Color.YELLOW,     // 4
        0xFFFFA500,       // 5 - 橙色
        0xFF800080,       // 6 - 紫色
        Color.WHITE       // 7
    };

    // 预设粗细档位
    public static final float[] PRESET_SIZES = {2f, 8f, 20f, 40f};

    // 马克笔默认透明度（约70%不透明度）
    private static final int DEFAULT_ALPHA = 180;

    private int color = Color.BLACK;
    private int alpha = DEFAULT_ALPHA;
    private float width = 8f;
    private boolean eraser = false;

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    public void setPresetColor(int index) {
        if (index >= 0 && index < PRESET_COLORS.length) {
            this.color = PRESET_COLORS[index];
        }
    }

    public int getAlpha() {
        return alpha;
    }

    public void setAlpha(int alpha) {
        this.alpha = Math.max(0, Math.min(255, alpha));
    }

    public float getWidth() {
        return width;
    }

    public void setWidth(float width) {
        this.width = Math.max(1f, Math.min(50f, width));
    }

    public void setPresetSize(int index) {
        if (index >= 0 && index < PRESET_SIZES.length) {
            this.width = PRESET_SIZES[index];
        }
    }

    public boolean isEraser() {
        return eraser;
    }

    public void setEraser(boolean eraser) {
        this.eraser = eraser;
    }

    public void toggleEraser() {
        this.eraser = !this.eraser;
    }

    /**
     * 获取当前颜色在预设数组中的索引
     * 如果不是预设颜色，返回 -1
     */
    public int getPresetColorIndex() {
        for (int i = 0; i < PRESET_COLORS.length; i++) {
            if (color == PRESET_COLORS[i]) {
                return i;
            }
        }
        return -1;
    }
}
```

- [ ] **Step 2: 提交 BrushManager**

```bash
git add app/src/main/java/com/chenjinxiang/doodleboard/model/BrushManager.java
git commit -m "feat: add BrushManager with presets"
```

---

### Task 7: 创建 DrawingView（第一部分 - 基础结构）

**Files:**
- Create: `app/src/main/java/com/chenjinxiang/doodleboard/view/DrawingView.java`

- [ ] **Step 1: 创建 DrawingView.java 基础结构**

```java
package com.chenjinxiang.doodleboard.view;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.util.AttributeSet;
import android.view.View;

import com.chenjinxiang.doodleboard.model.BrushManager;
import com.chenjinxiang.doodleboard.model.HistoryManager;
import com.chenjinxiang.doodleboard.model.Stroke;

/**
 * 绘图 View
 * 处理触摸事件和绘制逻辑
 */
public class DrawingView extends View {
    private Bitmap canvasBitmap;
    private Canvas bitmapCanvas;
    private Paint paint;

    private Path currentPath;
    private float lastX, lastY;

    private HistoryManager historyManager;
    private BrushManager brushManager;

    public DrawingView(Context context) {
        super(context);
        init();
    }

    public DrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        historyManager = new HistoryManager();
        brushManager = new BrushManager();

        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStyle(Paint.Style.STROKE);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        // 创建离屏 Bitmap
        if (w > 0 && h > 0) {
            canvasBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            bitmapCanvas = new Canvas(canvasBitmap);
            bitmapCanvas.drawColor(Color.WHITE);
        }
    }

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
            paint.setXfermode(null);
            paint.setColor(brushManager.getColor());
            paint.setAlpha(brushManager.getAlpha());
            paint.setStrokeWidth(brushManager.getWidth());
            canvas.drawPath(currentPath, paint);
        }
    }

    private void drawStroke(Canvas canvas, Stroke stroke) {
        if (stroke.isEraser()) {
            paint.setColor(Color.TRANSPARENT);
            paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
            paint.setAlpha(255);
        } else {
            paint.setXfermode(null);
            paint.setColor(stroke.getColor());
            paint.setAlpha(stroke.getAlpha());
        }
        paint.setStrokeWidth(stroke.getWidth());
        canvas.drawPath(stroke.getPath(), paint);
    }

    // Getter 方法
    public HistoryManager getHistoryManager() {
        return historyManager;
    }

    public BrushManager getBrushManager() {
        return brushManager;
    }

    public void clear() {
        historyManager.clear();
        if (bitmapCanvas != null) {
            bitmapCanvas.drawColor(Color.WHITE);
        }
        invalidate();
    }
}
```

- [ ] **Step 2: 提交 DrawingView 基础结构**

```bash
git add app/src/main/java/com/chenjinxiang/doodleboard/view/DrawingView.java
git commit -m "feat: add DrawingView base structure"
```

---

### Task 8: 创建 DrawingView（第二部分 - 触摸处理）

**Files:**
- Modify: `app/src/main/java/com/chenjinxiang/doodleboard/view/DrawingView.java`

- [ ] **Step 1: 添加触摸事件处理**

在 DrawingView.java 的 `init()` 方法后添加以下代码：

```java
@Override
public boolean onTouchEvent(MotionEvent event) {
    float x = event.getX();
    float y = event.getY();

    switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN:
            handleActionDown(x, y);
            break;

        case MotionEvent.ACTION_MOVE:
            handleActionMove(x, y);
            break;

        case MotionEvent.ACTION_UP:
            handleActionUp();
            break;
    }

    return true;
}

private void handleActionDown(float x, float y) {
    currentPath = new Path();
    currentPath.moveTo(x, y);
    lastX = x;
    lastY = y;
}

private void handleActionMove(float x, float y) {
    if (currentPath != null) {
        // 二阶贝塞尔平滑处理
        float midX = (lastX + x) / 2;
        float midY = (lastY + y) / 2;
        currentPath.quadTo(lastX, lastY, midX, midY);

        lastX = x;
        lastY = y;

        invalidate(); // 实时刷新
    }
}

private void handleActionUp() {
    if (currentPath != null) {
        // 保存笔画
        Stroke stroke = new Stroke(
            currentPath,
            brushManager.getColor(),
            brushManager.getAlpha(),
            brushManager.getWidth(),
            brushManager.isEraser()
        );
        historyManager.addStroke(stroke);

        currentPath = null;
        invalidate();
    }
}
```

在文件顶部添加 import：

```java
import android.view.MotionEvent;
```

- [ ] **Step 2: 提交触摸事件处理**

```bash
git add app/src/main/java/com/chenjinxiang/doodleboard/view/DrawingView.java
git commit -m "feat: add touch event handling with Bezier smoothing"
```

---

### Task 9: 创建主界面布局

**Files:**
- Create: `app/src/main/res/layout/activity_main.xml`

- [ ] **Step 1: 创建 activity_main.xml**

```xml
<?xml version="1.0" encoding="utf-8"?>
<RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@color/canvas_white">

    <!-- 顶部栏 -->
    <com.google.android.material.appbar.MaterialToolbar
        android:id="@+id/toolbar"
        android:layout_width="match_parent"
        android:layout_height="?attr/actionBarSize"
        android:background="@color/toolbar_bg"
        android:elevation="4dp"
        app:title="@string/app_name"
        app:titleTextColor="@color/primary" />

    <!-- 画布 -->
    <com.chenjinxiang.doodleboard.view.DrawingView
        android:id="@+id/drawingView"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:layout_below="@id/toolbar"
        android:layout_above="@id/bottomToolbar" />

    <!-- 底部工具栏 -->
    <LinearLayout
        android:id="@+id/bottomToolbar"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_alignParentBottom="true"
        android:orientation="vertical"
        android:background="@color/toolbar_bg"
        android:elevation="8dp">

        <!-- 第一行：撤销/重做 + 颜色选择 -->
        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="horizontal"
            android:padding="@dimen/toolbar_padding"
            android:gravity="center_vertical">

            <!-- 撤销/重做按钮组 -->
            <LinearLayout
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:orientation="horizontal">

                <ImageButton
                    android:id="@+id/btnUndo"
                    android:layout_width="@dimen/button_size"
                    android:layout_height="@dimen/button_size"
                    android:background="?attr/selectableItemBackgroundBorderless"
                    android:src="@drawable/ic_undo"
                    android:contentDescription="@string/undo" />

                <ImageButton
                    android:id="@+id/btnRedo"
                    android:layout_width="@dimen/button_size"
                    android:layout_height="@dimen/button_size"
                    android:layout_marginStart="8dp"
                    android:background="?attr/selectableItemBackgroundBorderless"
                    android:src="@drawable/ic_redo"
                    android:contentDescription="@string/redo" />
            </LinearLayout>

            <!-- 分隔线 -->
            <View
                android:layout_width="1dp"
                android:layout_height="24dp"
                android:layout_marginStart="16dp"
                android:layout_marginEnd="16dp"
                android:background="@color/toolbar_stroke" />

            <!-- 颜色选择器 -->
            <HorizontalScrollView
                android:layout_width="0dp"
                android:layout_height="wrap_content"
                android:layout_weight="1"
                android:scrollbars="none">

                <LinearLayout
                    android:id="@+id/colorContainer"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:orientation="horizontal"
                    android:gravity="center_vertical" />

            </HorizontalScrollView>

            <!-- 更多颜色按钮 -->
            <ImageButton
                android:id="@+id/btnMoreColors"
                android:layout_width="@dimen/button_size"
                android:layout_height="@dimen/button_size"
                android:layout_marginStart="8dp"
                android:background="?attr/selectableItemBackgroundBorderless"
                android:src="@drawable/ic_add"
                android:contentDescription="@string/more_colors" />
        </LinearLayout>

        <!-- 第二行：粗细 + 橡皮擦 + 清空 + 保存 -->
        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:orientation="horizontal"
            android:padding="@dimen/toolbar_padding"
            android:gravity="center">

            <!-- 粗细按钮 -->
            <Button
                android:id="@+id/btnBrushSize"
                style="@style/Widget.Material3.Button.TextButton"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:text="@string/brush_size"
                android:minWidth="88dp" />

            <!-- 橡皮擦按钮 -->
            <ImageButton
                android:id="@+id/btnEraser"
                android:layout_width="@dimen/button_size"
                android:layout_height="@dimen/button_size"
                android:layout_marginStart="8dp"
                android:background="?attr/selectableItemBackgroundBorderless"
                android:src="@drawable/ic_eraser"
                android:contentDescription="@string/eraser" />

            <!-- 清空按钮 -->
            <ImageButton
                android:id="@+id/btnClear"
                android:layout_width="@dimen/button_size"
                android:layout_height="@dimen/button_size"
                android:layout_marginStart="8dp"
                android:background="?attr/selectableItemBackgroundBorderless"
                android:src="@drawable/ic_clear"
                android:contentDescription="@string/clear" />

            <!-- 保存按钮 -->
            <Button
                android:id="@+id/btnSave"
                style="@style/Widget.Material3.Button"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                android:layout_marginStart="16dp"
                android:text="@string/save"
                android:drawableStart="@drawable/ic_save"
                android:drawablePadding="8dp" />
        </LinearLayout>

    </LinearLayout>

</RelativeLayout>
```

- [ ] **Step 2: 提交主布局**

```bash
git add app/src/main/res/layout/activity_main.xml
git commit -m "feat: add main activity layout"
```

---

### Task 10: 创建颜色选择器对话框

**Files:**
- Create: `app/src/main/res/layout/dialog_color_picker.xml`
- Create: `app/src/main/java/com/chenjinxiang/doodleboard/ui/ColorPickerDialog.java`

- [ ] **Step 1: 创建 dialog_color_picker.xml**

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:padding="@dimen/dialog_padding">

    <TextView
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="@string/select_color"
        android:textSize="18sp"
        android:textStyle="bold"
        android:layout_marginBottom="16dp" />

    <com.google.android.material.slider.Slider
        android:id="@+id/redSlider"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:valueFrom="0"
        android:valueTo="255"
        android:title="R" />

    <com.google.android.material.slider.Slider
        android:id="@+id/greenSlider"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginTop="8dp"
        android:valueFrom="0"
        android:valueTo="255"
        android:title="G" />

    <com.google.android.material.slider.Slider
        android:id="@+id/blueSlider"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginTop="8dp"
        android:valueFrom="0"
        android:valueTo="255"
        android:title="B" />

    <!-- 颜色预览 -->
    <View
        android:id="@+id/colorPreview"
        android:layout_width="match_parent"
        android:layout_height="48dp"
        android:layout_marginTop="16dp"
        android:background="@color/brush_black" />

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginTop="24dp"
        android:orientation="horizontal"
        android:gravity="end">

        <Button
            android:id="@+id/btnCancel"
            style="@style/Widget.Material3.Button.TextButton"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="@string/cancel" />

        <Button
            android:id="@+id/btnConfirm"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginStart="8dp"
            android:text="@string/confirm" />
    </LinearLayout>

</LinearLayout>
```

- [ ] **Step 2: 创建 ColorPickerDialog.java**

```java
package com.chenjinxiang.doodleboard.ui;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.Toast;

import com.chenjinxiang.doodleboard.R;
import com.google.android.material.slider.Slider;

/**
 * 自定义颜色选择对话框
 */
public class ColorPickerDialog extends Dialog {

    private Slider redSlider, greenSlider, blueSlider;
    private View colorPreview;
    private Button btnConfirm, btnCancel;

    private int selectedColor = Color.BLACK;
    private OnColorSelectedListener listener;

    public interface OnColorSelectedListener {
        void onColorSelected(int color);
    }

    public ColorPickerDialog(Context context, int initialColor, OnColorSelectedListener listener) {
        super(context);
        this.selectedColor = initialColor;
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_color_picker);

        initViews();
        setupListeners();
        updatePreview();
    }

    private void initViews() {
        redSlider = findViewById(R.id.redSlider);
        greenSlider = findViewById(R.id.greenSlider);
        blueSlider = findViewById(R.id.blueSlider);
        colorPreview = findViewById(R.id.colorPreview);
        btnConfirm = findViewById(R.id.btnConfirm);
        btnCancel = findViewById(R.id.btnCancel);

        // 设置初始值
        redSlider.setValue(Color.red(selectedColor));
        greenSlider.setValue(Color.green(selectedColor));
        blueSlider.setValue(Color.blue(selectedColor));
    }

    private void setupListeners() {
        Slider.OnChangeListener listener = new Slider.OnChangeListener() {
            @Override
            public void onValueChange(Slider slider, float value, boolean fromUser) {
                updatePreview();
            }
        };

        redSlider.addOnChangeListener(listener);
        greenSlider.addOnChangeListener(listener);
        blueSlider.addOnChangeListener(listener);

        btnConfirm.setOnClickListener(v -> {
            selectedColor = Color.rgb(
                (int) redSlider.getValue(),
                (int) greenSlider.getValue(),
                (int) blueSlider.getValue()
            );
            if (this.listener != null) {
                this.listener.onColorSelected(selectedColor);
            }
            dismiss();
        });

        btnCancel.setOnClickListener(v -> dismiss());
    }

    private void updatePreview() {
        int color = Color.rgb(
            (int) redSlider.getValue(),
            (int) greenSlider.getValue(),
            (int) blueSlider.getValue()
        );
        colorPreview.setBackgroundColor(color);
    }
}
```

- [ ] **Step 3: 提交颜色选择器**

```bash
git add app/src/main/res/layout/dialog_color_picker.xml app/src/main/java/com/chenjinxiang/doodleboard/ui/ColorPickerDialog.java
git commit -m "feat: add color picker dialog"
```

---

### Task 11: 创建粗细调节对话框

**Files:**
- Create: `app/src/main/res/layout/dialog_brush_size.xml`
- Create: `app/src/main/java/com/chenjinxiang/doodleboard/ui/BrushSizeDialog.java`

- [ ] **Step 1: 创建 dialog_brush_size.xml**

```xml
<?xml version="1.0" encoding="utf-8"?>
<LinearLayout xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:orientation="vertical"
    android:padding="@dimen/dialog_padding">

    <TextView
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:text="@string/brush_size"
        android:textSize="18sp"
        android:textStyle="bold"
        android:layout_marginBottom="16dp" />

    <!-- 粗细滑块 -->
    <com.google.android.material.slider.Slider
        android:id="@+id/sizeSlider"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:valueFrom="1"
        android:valueTo="50"
        android:stepSize="1"
        android:value="8" />

    <!-- 当前粗细显示 -->
    <TextView
        android:id="@+id/tvSizeValue"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginTop="8dp"
        android:textAlignment="center"
        android:textSize="16sp"
        android:text="当前粗细: 8px" />

    <!-- 预设档位 -->
    <TextView
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginTop="16dp"
        android:text="预设档位"
        android:textSize="14sp"
        android:textColor="@color/primary" />

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginTop="8dp"
        android:orientation="horizontal">

        <Button
            android:id="@+id/btnSizeThin"
            style="@style/Widget.Material3.Button.OutlinedButton"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:layout_marginEnd="4dp"
            android:text="2px" />

        <Button
            android:id="@+id/btnSizeMedium"
            style="@style/Widget.Material3.Button.OutlinedButton"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:layout_marginEnd="4dp"
            android:text="8px" />

        <Button
            android:id="@+id/btnSizeThick"
            style="@style/Widget.Material3.Button.OutlinedButton"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:layout_marginEnd="4dp"
            android:text="20px" />

        <Button
            android:id="@+id/btnSizeExtra"
            style="@style/Widget.Material3.Button.OutlinedButton"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:text="40px" />
    </LinearLayout>

    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginTop="24dp"
        android:orientation="horizontal"
        android:gravity="end">

        <Button
            android:id="@+id/btnCancel"
            style="@style/Widget.Material3.Button.TextButton"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:text="@string/cancel" />

        <Button
            android:id="@+id/btnConfirm"
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginStart="8dp"
            android:text="@string/confirm" />
    </LinearLayout>

</LinearLayout>
```

- [ ] **Step 2: 创建 BrushSizeDialog.java**

```java
package com.chenjinxiang.doodleboard.ui;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.chenjinxiang.doodleboard.R;
import com.chenjinxiang.doodleboard.model.BrushManager;
import com.google.android.material.slider.Slider;

/**
 * 笔刷粗细调节对话框
 */
public class BrushSizeDialog extends Dialog {

    private Slider sizeSlider;
    private TextView tvSizeValue;
    private Button btnThin, btnMedium, btnThick, btnExtra;
    private Button btnConfirm, btnCancel;

    private float initialSize;
    private float selectedSize;
    private OnSizeSelectedListener listener;

    public interface OnSizeSelectedListener {
        void onSizeSelected(float size);
    }

    public BrushSizeDialog(Context context, float initialSize, OnSizeSelectedListener listener) {
        super(context);
        this.initialSize = initialSize;
        this.selectedSize = initialSize;
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_brush_size);

        initViews();
        setupListeners();
    }

    private void initViews() {
        sizeSlider = findViewById(R.id.sizeSlider);
        tvSizeValue = findViewById(R.id.tvSizeValue);
        btnThin = findViewById(R.id.btnSizeThin);
        btnMedium = findViewById(R.id.btnSizeMedium);
        btnThick = findViewById(R.id.btnSizeThick);
        btnExtra = findViewById(R.id.btnSizeExtra);
        btnConfirm = findViewById(R.id.btnConfirm);
        btnCancel = findViewById(R.id.btnCancel);

        // 设置初始值
        sizeSlider.setValue(initialSize);
        updateSizeValue(initialSize);
    }

    private void setupListeners() {
        sizeSlider.addOnChangeListener((slider, value, fromUser) -> {
            updateSizeValue(value);
        });

        // 预设档位按钮
        btnThin.setOnClickListener(v -> {
            sizeSlider.setValue(2);
        });

        btnMedium.setOnClickListener(v -> {
            sizeSlider.setValue(8);
        });

        btnThick.setOnClickListener(v -> {
            sizeSlider.setValue(20);
        });

        btnExtra.setOnClickListener(v -> {
            sizeSlider.setValue(40);
        });

        btnConfirm.setOnClickListener(v -> {
            selectedSize = sizeSlider.getValue();
            if (listener != null) {
                listener.onSizeSelected(selectedSize);
            }
            dismiss();
        });

        btnCancel.setOnClickListener(v -> dismiss());
    }

    private void updateSizeValue(float size) {
        tvSizeValue.setText(String.format(getContext().getString(R.string.current_size), (int) size));
    }
}
```

- [ ] **Step 3: 提交粗细对话框**

```bash
git add app/src/main/res/layout/dialog_brush_size.xml app/src/main/java/com/chenjinxiang/doodleboard/ui/BrushSizeDialog.java
git commit -m "feat: add brush size dialog with presets"
```

---

### Task 12: 创建 FileSaver 工具类

**Files:**
- Create: `app/src/main/java/com/chenjinxiang/doodleboard/utils/FileSaver.java`

- [ ] **Step 1: 创建 FileSaver.java**

```java
package com.chenjinxiang.doodleboard.utils;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;

import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 文件保存工具类
 * 负责将图片保存到系统相册
 */
public class FileSaver {

    /**
     * 保存 Bitmap 到系统相册
     */
    public static boolean saveToGallery(Context context, Bitmap bitmap) {
        String filename = "涂鸦_" + getTimestamp() + ".png";

        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DISPLAY_NAME, filename);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
        values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/涂鸦板");

        Uri uri = context.getContentResolver().insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            values
        );

        if (uri != null) {
            try (OutputStream out = context.getContentResolver().openOutputStream(uri)) {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                return true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return false;
    }

    /**
     * 获取当前时间戳字符串
     */
    private static String getTimestamp() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HHmmss", Locale.getDefault());
        return sdf.format(new Date());
    }
}
```

- [ ] **Step 2: 提交 FileSaver**

```bash
git add app/src/main/java/com/chenjinxiang/doodleboard/utils/FileSaver.java
git commit -m "feat: add FileSaver utility"
```

---

### Task 13: 创建 MainActivity（第一部分 - 基础设置）

**Files:**
- Create: `app/src/main/java/com/chenjinxiang/doodleboard/MainActivity.java`

- [ ] **Step 1: 创建 MainActivity.java 基础结构**

```java
package com.chenjinxiang.doodleboard;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.chenjinxiang.doodleboard.model.BrushManager;
import com.chenjinxiang.doodleboard.ui.BrushSizeDialog;
import com.chenjinxiang.doodleboard.ui.ColorPickerDialog;
import com.chenjinxiang.doodleboard.utils.FileSaver;
import com.chenjinxiang.doodleboard.view.DrawingView;
import com.google.android.material.snackbar.Snackbar;

public class MainActivity extends AppCompatActivity {

    private DrawingView drawingView;
    private BrushManager brushManager;

    // 底部工具栏控件
    private ImageButton btnUndo, btnRedo;
    private ImageButton btnEraser, btnClear, btnMoreColors;
    private LinearLayout colorContainer;

    // 颜色选择视图数组
    private View[] colorViews;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupColorPalette();
        setupToolbarButtons();
        updateButtonStates();
    }

    private void initViews() {
        drawingView = findViewById(R.id.drawingView);
        brushManager = drawingView.getBrushManager();

        btnUndo = findViewById(R.id.btnUndo);
        btnRedo = findViewById(R.id.btnRedo);
        btnEraser = findViewById(R.id.btnEraser);
        btnClear = findViewById(R.id.btnClear);
        btnMoreColors = findViewById(R.id.btnMoreColors);
        colorContainer = findViewById(R.id.colorContainer);

        findViewById(R.id.btnBrushSize).setOnClickListener(v -> showBrushSizeDialog());
        findViewById(R.id.btnSave).setOnClickListener(v -> saveDrawing());
    }

    private void setupColorPalette() {
        colorViews = new View[BrushManager.PRESET_COLORS.length];

        for (int i = 0; i < BrushManager.PRESET_COLORS.length; i++) {
            View colorView = createColorView(BrushManager.PRESET_COLORS[i], i);
            colorContainer.addView(colorView);
            colorViews[i] = colorView;
        }

        updateColorSelection();
    }

    private View createColorView(int color, int index) {
        View colorCircle = new View(this);
        int size = getResources().getDimensionPixelSize(R.dimen.color_circle_size);
        int margin = getResources().getDimensionPixelSize(R.dimen.color_spacing);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
        params.setMargins(margin, margin, margin, margin);
        colorCircle.setLayoutParams(params);

        colorCircle.setBackgroundColor(color);
        colorCircle.setTag(index);

        colorCircle.setOnClickListener(v -> {
            int colorIndex = (int) v.getTag();
            brushManager.setPresetColor(colorIndex);
            brushManager.setEraser(false);
            updateColorSelection();
            updateButtonStates();
        });

        return colorCircle;
    }

    private void updateColorSelection() {
        int selectedIndex = brushManager.getPresetColorIndex();

        for (int i = 0; i < colorViews.length; i++) {
            View colorView = colorViews[i];
            ViewGroup.LayoutParams params = colorView.getLayoutParams();

            if (i == selectedIndex) {
                // 高亮选中的颜色
                colorView.setElevation(8);
            } else {
                colorView.setElevation(0);
            }
        }
    }

    private void setupToolbarButtons() {
        // 撤销/重做
        btnUndo.setOnClickListener(v -> {
            drawingView.getHistoryManager().undo();
            drawingView.invalidate();
            updateButtonStates();
        });

        btnRedo.setOnClickListener(v -> {
            drawingView.getHistoryManager().redo();
            drawingView.invalidate();
            updateButtonStates();
        });

        // 更多颜色
        btnMoreColors.setOnClickListener(v -> showColorPickerDialog());

        // 橡皮擦
        btnEraser.setOnClickListener(v -> {
            brushManager.toggleEraser();
            updateButtonStates();
            showEraserToast();
        });

        // 清空
        btnClear.setOnClickListener(v -> showClearDialog());
    }

    private void updateButtonStates() {
        btnUndo.setEnabled(drawingView.getHistoryManager().canUndo());
        btnRedo.setEnabled(drawingView.getHistoryManager().canRedo());
        btnEraser.setSelected(brushManager.isEraser());
    }

    private void showEraserToast() {
        if (brushManager.isEraser()) {
            Toast.makeText(this, R.string.eraser_mode, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, R.string.draw_mode, Toast.LENGTH_SHORT).show();
        }
    }

    private void showClearDialog() {
        new AlertDialog.Builder(this)
            .setTitle(R.string.clear_dialog_title)
            .setMessage(R.string.clear_dialog_message)
            .setPositiveButton(R.string.clear_confirm, (dialog, which) -> {
                drawingView.clear();
                updateButtonStates();
            })
            .setNegativeButton(R.string.cancel, null)
            .show();
    }

    private void showColorPickerDialog() {
        new ColorPickerDialog(this, brushManager.getColor(), color -> {
            brushManager.setColor(color);
            brushManager.setEraser(false);
            updateColorSelection();
            updateButtonStates();
        }).show();
    }

    private void showBrushSizeDialog() {
        new BrushSizeDialog(this, brushManager.getWidth(), size -> {
            brushManager.setWidth(size);
        }).show();
    }

    private void saveDrawing() {
        drawingView.setDrawingCacheEnabled(true);
        Bitmap bitmap = drawingView.getDrawingCache();
        drawingView.setDrawingCacheEnabled(false);

        if (bitmap != null) {
            boolean success = FileSaver.saveToGallery(this, bitmap);

            Snackbar snackbar = Snackbar.make(
                findViewById(R.id.bottomToolbar),
                success ? R.string.saved_to_gallery : "保存失败",
                Snackbar.LENGTH_LONG
            );

            if (success) {
                snackbar.setAction(R.string.view, v -> {
                    // TODO: 打开相册查看保存的图片
                });
            }

            snackbar.show();
        }
    }
}
```

- [ ] **Step 2: 提交 MainActivity 基础结构**

```bash
git add app/src/main/java/com/chenjinxiang/doodleboard/MainActivity.java
git commit -m "feat: add MainActivity with basic UI setup"
```

---

### Task 14: 完善 DrawingView 保存功能

**Files:**
- Modify: `app/src/main/java/com/chenjinxiang/doodleboard/view/DrawingView.java`

- [ ] **Step 1: 添加保存 Bitmap 功能**

在 DrawingView.java 中添加以下方法：

```java
/**
 * 获取画布内容的 Bitmap
 */
public Bitmap getCanvasBitmap() {
    Bitmap result = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
    Canvas canvas = new Canvas(result);

    // 绘制白色背景
    canvas.drawColor(Color.WHITE);

    // 绘制所有笔画
    for (Stroke stroke : historyManager.getStrokes()) {
        drawStroke(canvas, stroke);
    }

    return result;
}
```

- [ ] **Step 2: 更新 MainActivity 保存方法**

修改 MainActivity.java 中的 saveDrawing 方法：

```java
private void saveDrawing() {
    Bitmap bitmap = drawingView.getCanvasBitmap();

    if (bitmap != null) {
        boolean success = FileSaver.saveToGallery(this, bitmap);

        Snackbar snackbar = Snackbar.make(
            findViewById(R.id.bottomToolbar),
            success ? R.string.saved_to_gallery : "保存失败",
            Snackbar.LENGTH_LONG
        );

        if (success) {
            snackbar.setAction(R.string.view, v -> {
                // TODO: 打开相册查看保存的图片
            });
        }

        snackbar.show();
    }
}
```

- [ ] **Step 3: 提交保存功能**

```bash
git add app/src/main/java/com/chenjinxiang/doodleboard/view/DrawingView.java app/src/main/java/com/chenjinxiang/doodleboard/MainActivity.java
git commit -m "feat: add save drawing functionality"
```

---

### Task 15: 添加动画和视觉反馈

**Files:**
- Modify: `app/src/main/java/com/chenjinxiang/doodleboard/MainActivity.java`

- [ ] **Step 1: 添加按钮选中状态样式**

在 MainActivity.java 中添加橡皮擦按钮选中状态的视觉效果：

```java
private void updateButtonStates() {
    btnUndo.setEnabled(drawingView.getHistoryManager().canUndo());
    btnRedo.setEnabled(drawingView.getHistoryManager().canRedo());

    boolean isEraser = brushManager.isEraser();
    btnEraser.setSelected(isEraser);

    if (isEraser) {
        btnEraser.setBackgroundColor(Color.parseColor("#E0E0E0"));
    } else {
        btnEraser.setBackgroundColor(Color.TRANSPARENT);
    }
}
```

在文件顶部确保已导入：

```java
import android.graphics.Color;
```

- [ ] **Step 2: 提交视觉反馈改进**

```bash
git add app/src/main/java/com/chenjinxiang/doodleboard/MainActivity.java
git commit -m "feat: add visual feedback for button states"
```

---

### Task 16: 创建 ProGuard 配置

**Files:**
- Create: `app/proguard-rules.pro`

- [ ] **Step 1: 创建 proguard-rules.pro**

```groovy
# Add project specific ProGuard rules here.
# You can control the set of applied configuration files using the
# proguardFiles setting in build.gradle.
#
# For more details, see
#   http://developer.android.com/guide/developing/tools/proguard.html

# If your project uses WebView with JS, uncomment the following
# and specify the fully qualified class name to the JavaScript interface
# class:
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}

# Uncomment this to preserve the line number information for
# debugging stack traces.
#-keepattributes SourceFile,LineNumberTable

# If you keep the line number information, uncomment this to
# hide the original source file name.
#-renamesourcefileattribute SourceFile
```

- [ ] **Step 2: 提交 ProGuard 配置**

```bash
git add app/proguard-rules.pro
git commit -m "chore: add ProGuard configuration"
```

---

### Task 17: 创建默认启动图标

**Files:**
- Create: `app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml`
- Create: `app/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml`
- Create: `app/src/main/res/mipmap-hdpi/ic_launcher.png`
- Create: `app/src/main/res/mipmap-hdpi/ic_launcher_round.png`
- Create: `app/src/main/res/mipmap-mdpi/ic_launcher.png`
- Create: `app/src/main/res/mipmap-mdpi/ic_launcher_round.png`
- Create: `app/src/main/res/mipmap-xhdpi/ic_launcher.png`
- Create: `app/src/main/res/mipmap-xhdpi/ic_launcher_round.png`
- Create: `app/src/main/res/mipmap-xxhdpi/ic_launcher.png`
- Create: `app/src/main/res/mipmap-xxhdpi/ic_launcher_round.png`
- Create: `app/src/main/res/mipmap-xxxhdpi/ic_launcher.png`
- Create: `app/src/main/res/mipmap-xxxhdpi/ic_launcher_round.png`

- [ ] **Step 1: 创建自适应图标 XML**

`app/src/main/res/mipmap-anydpi-v26/ic_launcher.xml`:
```xml
<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@color/canvas_white"/>
    <foreground android:drawable="@drawable/ic_save"/>
</adaptive-icon>
```

`app/src/main/res/mipmap-anydpi-v26/ic_launcher_round.xml`:
```xml
<?xml version="1.0" encoding="utf-8"?>
<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">
    <background android:drawable="@color/canvas_white"/>
    <foreground android:drawable="@drawable/ic_save"/>
</adaptive-icon>
```

- [ ] **Step 2: 为其他分辨率创建占位符**

对于第一版，使用简单的纯色占位符。在 Android Studio 中生成实际图标，或使用 Android Asset Studio。

- [ ] **Step 3: 提交启动图标**

```bash
git add app/src/main/res/mipmap-*/
git commit -m "chore: add launcher icon placeholders"
```

---

### Task 18: 最终测试和验证

**Files:**
- Test: 所有功能测试

- [ ] **Step 1: 验证构建**

运行以下命令确保项目可以成功构建：

```bash
./gradlew assembleDebug
```

预期输出：BUILD SUCCESSFUL

- [ ] **Step 2: 功能验证清单**

在设备/模拟器上验证以下功能：

- [ ] 应用启动，画布显示白色背景
- [ ] 手指触摸可以绘制笔画
- [ ] 笔画有半透明马克笔效果
- [ ] 切换颜色后笔画颜色改变
- [ ] 点击"更多颜色"可以选择自定义颜色
- [ ] 调节粗细后笔画粗细改变
- [ ] 预设档位正确设置粗细
- [ ] 橡皮擦可以擦除笔画
- [ ] 撤销按钮可以撤销笔画
- [ ] 重做按钮可以重做笔画
- [ ] 撤销/重做按钮状态正确启用/禁用
- [ ] 清空对话框显示确认信息
- [ ] 确认清空后画布清空
- [ ] 保存按钮保存图片到相册
- [ ] 保存成功显示 Snackbar
- [ ] 横竖屏保持竖屏

- [ ] **Step 3: 提交最终版本**

```bash
git add .
git commit -m "chore: finalize v1.0 with all features working"
```

---

## 完成

实施计划完成。应用包含以下功能：

1. 马克笔绘图（半透明、二阶贝塞尔平滑）
2. 8 个预设颜色 + 自定义颜色选择器
3. 笔刷粗细调节（滑块 + 4 个预设档位）
4. 橡皮擦（PorterDuff.Mode.CLEAR）
5. 撤销/重做（最多 50 笔）
6. 清空画布（带确认对话框）
7. 保存到相册（PNG 格式）
8. Material Design 界面
