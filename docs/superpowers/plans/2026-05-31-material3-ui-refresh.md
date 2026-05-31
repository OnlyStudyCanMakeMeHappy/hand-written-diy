# Material 3 UI Refresh Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Replace the current simple toolbar UI with the approved Material 3 prototype: white canvas, compact top action bar, floating bottom tool dock, brush/eraser size panel, and a full palette dialog opened directly from the color tool using the supplied SVG icon assets.

**Architecture:** Keep the existing MV structure and `DrawingView` stroke rendering intact. Treat this as a UI-layer refresh in `activity_main.xml`, resource files, and `MainActivity`; do not change `Stroke`, `HistoryManager`, `BrushManager`, or save behavior unless a UI binding requires it.

**Tech Stack:** Java, Android SDK 29-34, Material Components 1.9.0, ConstraintLayout 2.1.4, Android `VectorDrawable`, `Canvas/Path/Paint`.

---

## File Structure

- Modify `app/src/main/res/layout/activity_main.xml`
  - Convert the main screen to a full-canvas `ConstraintLayout`.
  - Add a compact top action bar, floating bottom toolbar, and inline brush-size panel.
  - Do not display the board title in the top bar.
  - Do not include a bottom “more” tool item.
- Modify `app/src/main/java/com/chenjinxiang/doodleboard/MainActivity.java`
  - Bind the new toolbar and panel views.
  - Make the back button finish the Activity and return to the launcher/desktop.
  - Make the right top action a trash/clear button that opens the clear confirmation dialog.
  - Make the color tool open the palette dialog directly.
  - Keep save, undo, redo, clear, eraser, and custom color behavior working.
- Modify `app/src/main/res/values/colors.xml`
  - Add Material 3 UI tokens: background, surface, outline, text, primary blue, active chip background.
- Modify `app/src/main/res/values/dimens.xml`
  - Add toolbar, panel, icon, swatch, and spacing dimensions.
- Modify `app/src/main/res/values/strings.xml`
  - Fix garbled Chinese strings and add labels matching the prototype.
- Modify `app/src/main/res/values/themes.xml`
  - Update status/navigation bar colors for a white Material 3 surface.
- Create or replace drawables in `app/src/main/res/drawable/`
  - Convert SVGs from `tuyaban_svg_icons.zip` to Android vector drawables.
  - Add a trash/delete icon for the clear action if the supplied zip does not include one.
  - Add shape drawables for floating surfaces, active icon backgrounds, color swatches, and panel buttons.
- Modify `app/src/androidTest/java/com/chenjinxiang/doodleboard/LayoutTest.java`
  - Update required view IDs to match the refreshed layout.
- Modify `app/src/main/java/com/chenjinxiang/doodleboard/ui/ColorPickerDialog.java`
  - Implement the palette UI structure: color wheel visual, current color preview, HEX value, RGB value, common colors, RGB sliders, and confirmation.

---

## Design Change Addendum

The following design changes supersede earlier steps in this plan:

1. The top bar must not show `未命名画板`.
2. `btnBack` is navigation only. It must call `finish()` and must not clear the canvas.
3. The right top action is a clear/trash button. It must use a garbage-bin-style icon and call `showClearDialog()`.
4. The bottom toolbar must not contain a “更多” button or `btnMore` ID.
5. The bottom color tool opens the palette dialog directly.
6. The palette dialog UI is part of this change, not deferred work.

---

## Task 1: Normalize Strings And Theme Tokens

**Files:**
- Modify: `app/src/main/res/values/strings.xml`
- Modify: `app/src/main/res/values/colors.xml`
- Modify: `app/src/main/res/values/dimens.xml`
- Modify: `app/src/main/res/values/themes.xml`

- [ ] **Step 1: Replace garbled strings with prototype labels**

Use these values in `strings.xml`:

```xml
<resources>
    <string name="app_name">涂鸦板</string>
    <string name="undo">撤销</string>
    <string name="redo">重做</string>
    <string name="brush">画笔</string>
    <string name="eraser">橡皮</string>
    <string name="clear">清空</string>
    <string name="save">保存</string>
    <string name="color">颜色</string>
    <string name="brush_size">粗细</string>
    <string name="color_panel_title">颜色</string>
    <string name="size_panel_title">粗细</string>
    <string name="custom_color_title">调色盘</string>
    <string name="clear_dialog_title">清空画布</string>
    <string name="clear_dialog_message">确定要清空所有内容吗？此操作不可撤销。</string>
    <string name="clear_confirm">清空</string>
    <string name="cancel">取消</string>
    <string name="confirm">确定</string>
    <string name="eraser_mode">橡皮模式</string>
    <string name="draw_mode">绘图模式</string>
    <string name="saved_to_gallery">已保存到相册</string>
    <string name="save_failed">保存失败</string>
    <string name="view">查看</string>
    <string name="current_size">%dpx</string>
    <string name="select_color">选择颜色</string>
</resources>
```

- [ ] **Step 2: Add UI colors**

Add these values to `colors.xml` without removing existing brush colors:

```xml
<color name="ui_background">#F8FAFC</color>
<color name="ui_canvas">#FFFFFF</color>
<color name="ui_surface">#FFFFFF</color>
<color name="ui_surface_translucent">#F7FFFFFF</color>
<color name="ui_text_primary">#111827</color>
<color name="ui_text_secondary">#64748B</color>
<color name="ui_outline">#E5E7EB</color>
<color name="ui_shadow_tint">#330F172A</color>
<color name="ui_primary_blue">#1A73E8</color>
<color name="ui_primary_blue_soft">#EAF2FF</color>
<color name="ui_primary_blue_outline">#93BDFF</color>
```

- [ ] **Step 3: Add dimensions**

Add these values to `dimens.xml`:

```xml
<dimen name="screen_edge_padding">16dp</dimen>
<dimen name="top_bar_height">56dp</dimen>
<dimen name="floating_toolbar_height">72dp</dimen>
<dimen name="floating_toolbar_bottom_margin">24dp</dimen>
<dimen name="floating_toolbar_radius">24dp</dimen>
<dimen name="tool_item_min_width">42dp</dimen>
<dimen name="tool_icon_touch_size">36dp</dimen>
<dimen name="tool_icon_size">22dp</dimen>
<dimen name="tool_label_text_size">10sp</dimen>
<dimen name="panel_corner_radius">24dp</dimen>
<dimen name="panel_padding">16dp</dimen>
<dimen name="color_swatch_size">40dp</dimen>
<dimen name="color_swatch_radius">10dp</dimen>
<dimen name="size_preset_height">36dp</dimen>
```

- [ ] **Step 4: Update theme bars**

In `themes.xml`, set white system bars:

```xml
<item name="android:statusBarColor">@color/ui_canvas</item>
<item name="android:navigationBarColor">@color/ui_canvas</item>
<item name="android:windowLightStatusBar">true</item>
<item name="android:windowLightNavigationBar">true</item>
```

- [ ] **Step 5: Verify resources compile**

Run:

```bash
./gradlew :app:assembleDebug
```

Expected: build succeeds with no resource merge errors.

---

## Task 2: Convert Supplied SVG Icons To Android Drawables

**Files:**
- Source: `tuyaban_svg_icons.zip`
- Create/replace: `app/src/main/res/drawable/ic_arrow_back.xml`
- Create/replace: `app/src/main/res/drawable/ic_more_vertical.xml`
- Create/replace: `app/src/main/res/drawable/ic_more_horizontal.xml`
- Create/replace: `app/src/main/res/drawable/ic_chevron_down.xml`
- Create/replace: `app/src/main/res/drawable/ic_close.xml`
- Create/replace: `app/src/main/res/drawable/ic_add.xml`
- Create/replace: `app/src/main/res/drawable/ic_undo.xml`
- Create/replace: `app/src/main/res/drawable/ic_redo.xml`
- Create/replace: `app/src/main/res/drawable/ic_pen.xml`
- Create/replace: `app/src/main/res/drawable/ic_eraser.xml`
- Create/replace: `app/src/main/res/drawable/ic_palette.xml`
- Create/replace: `app/src/main/res/drawable/ic_color_dot.xml`
- Create/replace: `app/src/main/res/drawable/ic_stroke_width.xml`
- Create/replace: `app/src/main/res/drawable/ic_save.xml`
- Create/replace: `app/src/main/res/drawable/ic_app_logo.xml`

- [ ] **Step 1: Extract the icon zip to a temporary local folder**

Run:

```powershell
New-Item -ItemType Directory -Force -Path .tmp_icons | Out-Null
Expand-Archive -LiteralPath tuyaban_svg_icons.zip -DestinationPath .tmp_icons -Force
Get-ChildItem .tmp_icons
```

Expected: the listed files include `ic_undo.svg`, `ic_redo.svg`, `ic_pen.svg`, `ic_eraser.svg`, `ic_save.svg`, and `ic_app_logo.svg`.

- [ ] **Step 2: Convert each SVG to VectorDrawable**

Use Android Studio's Vector Asset importer if available. If editing manually, preserve the SVG path geometry and convert common attributes:

```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24">
    <path
        android:fillColor="@android:color/transparent"
        android:strokeColor="@color/ui_text_primary"
        android:strokeWidth="2"
        android:strokeLineCap="round"
        android:strokeLineJoin="round"
        android:pathData="..." />
</vector>
```

For `ic_color_dot.xml`, use a filled circle:

```xml
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24">
    <path
        android:fillColor="@color/ui_primary_blue"
        android:pathData="M12,4a8,8 0,1 0,0.01 0" />
</vector>
```

- [ ] **Step 3: Add shape drawables for floating UI**

Create `app/src/main/res/drawable/bg_floating_toolbar.xml`:

```xml
<shape xmlns:android="http://schemas.android.com/apk/res/android">
    <solid android:color="@color/ui_surface" />
    <stroke android:width="1dp" android:color="@color/ui_outline" />
    <corners android:radius="@dimen/floating_toolbar_radius" />
</shape>
```

Create `app/src/main/res/drawable/bg_panel_surface.xml`:

```xml
<shape xmlns:android="http://schemas.android.com/apk/res/android">
    <solid android:color="@color/ui_surface" />
    <stroke android:width="1dp" android:color="@color/ui_outline" />
    <corners android:radius="@dimen/panel_corner_radius" />
</shape>
```

Create `app/src/main/res/drawable/bg_tool_active.xml`:

```xml
<shape xmlns:android="http://schemas.android.com/apk/res/android"
    android:shape="oval">
    <solid android:color="@color/ui_primary_blue_soft" />
</shape>
```

- [ ] **Step 4: Verify drawables compile**

Run:

```bash
./gradlew :app:assembleDebug
```

Expected: build succeeds. If Android resource linking fails on a vector path, fix that specific vector before proceeding.

---

## Task 3: Rebuild Main Layout Around The Prototype

**Files:**
- Modify: `app/src/main/res/layout/activity_main.xml`

- [ ] **Step 1: Replace the root with `ConstraintLayout`**

Use this structure:

```xml
<androidx.constraintlayout.widget.ConstraintLayout
    xmlns:android="http://schemas.android.com/apk/res/android"
    xmlns:app="http://schemas.android.com/apk/res-auto"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="@color/ui_canvas">

    <com.chenjinxiang.doodleboard.view.DrawingView
        android:id="@+id/drawingView"
        android:layout_width="0dp"
        android:layout_height="0dp"
        app:layout_constraintTop_toTopOf="parent"
        app:layout_constraintBottom_toBottomOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintEnd_toEndOf="parent" />

    <!-- topBar, panels, bottomToolbar go above DrawingView -->
</androidx.constraintlayout.widget.ConstraintLayout>
```

- [ ] **Step 2: Add the prototype top bar**

Add:

```xml
<LinearLayout
    android:id="@+id/topBar"
    android:layout_width="0dp"
    android:layout_height="@dimen/top_bar_height"
    android:gravity="center_vertical"
    android:orientation="horizontal"
    android:paddingStart="@dimen/screen_edge_padding"
    android:paddingEnd="@dimen/screen_edge_padding"
    android:background="@color/ui_surface"
    app:layout_constraintTop_toTopOf="parent"
    app:layout_constraintStart_toStartOf="parent"
    app:layout_constraintEnd_toEndOf="parent">

    <ImageButton
        android:id="@+id/btnBack"
        android:layout_width="@dimen/tool_icon_touch_size"
        android:layout_height="@dimen/tool_icon_touch_size"
        android:background="?attr/selectableItemBackgroundBorderless"
        android:src="@drawable/ic_arrow_back"
        android:contentDescription="@string/clear" />

    <Space
        android:layout_width="0dp"
        android:layout_height="match_parent"
        android:layout_weight="1" />

    <ImageButton
        android:id="@+id/btnTopClear"
        android:layout_width="@dimen/tool_icon_touch_size"
        android:layout_height="@dimen/tool_icon_touch_size"
        android:background="?attr/selectableItemBackgroundBorderless"
        android:src="@drawable/ic_delete"
        android:contentDescription="@string/clear" />
</LinearLayout>
```

- [ ] **Step 3: Add the floating bottom toolbar**

Replace the old `bottomToolbar` content with a `MaterialCardView` containing 7 equal tool items: undo, redo, brush, eraser, color, size, save.

```xml
<com.google.android.material.card.MaterialCardView
    android:id="@+id/bottomToolbar"
    android:layout_width="0dp"
    android:layout_height="@dimen/floating_toolbar_height"
    android:layout_marginStart="@dimen/screen_edge_padding"
    android:layout_marginEnd="@dimen/screen_edge_padding"
    android:layout_marginBottom="@dimen/floating_toolbar_bottom_margin"
    app:cardBackgroundColor="@color/ui_surface"
    app:cardCornerRadius="@dimen/floating_toolbar_radius"
    app:cardElevation="10dp"
    app:strokeColor="@color/ui_outline"
    app:strokeWidth="1dp"
    app:layout_constraintBottom_toBottomOf="parent"
    app:layout_constraintStart_toStartOf="parent"
    app:layout_constraintEnd_toEndOf="parent">

    <LinearLayout
        android:id="@+id/toolContainer"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:gravity="center"
        android:orientation="horizontal"
        android:paddingStart="8dp"
        android:paddingEnd="8dp" />
</com.google.android.material.card.MaterialCardView>
```

The child tool items can be declared in XML or added in Java. Prefer XML for stable layout tests.

- [ ] **Step 4: Add hidden color and size panels**

Add two `MaterialCardView` panels constrained above `bottomToolbar`, initially hidden:

```xml
android:id="@+id/colorPanel"
android:visibility="gone"
app:layout_constraintBottom_toTopOf="@id/bottomToolbar"
```

```xml
android:id="@+id/sizePanel"
android:visibility="gone"
app:layout_constraintBottom_toTopOf="@id/bottomToolbar"
```

- [ ] **Step 5: Verify layout inflation**

Run:

```bash
./gradlew :app:connectedDebugAndroidTest
```

Expected before updating `LayoutTest`: existing tests may fail because IDs changed or new required views are absent from assertions. Continue to Task 7 to align the tests.

---

## Task 4: Wire Toolbar State In MainActivity

**Files:**
- Modify: `app/src/main/java/com/chenjinxiang/doodleboard/MainActivity.java`

- [ ] **Step 1: Add view fields**

Add fields:

```java
private View colorPanel, sizePanel;
private ImageButton btnBack, btnTopClear;
private View btnBrush, btnEraserTool, btnColorTool, btnSizeTool;
private ImageButton btnUndo, btnRedo, btnSave;
private TextView tvSizeChip;
private View currentColorDot;
```

- [ ] **Step 2: Update `initViews()` bindings**

Bind new IDs and keep existing `drawingView` and `brushManager`:

```java
btnBack = findViewById(R.id.btnBack);
btnTopClear = findViewById(R.id.btnTopClear);
colorPanel = findViewById(R.id.colorPanel);
sizePanel = findViewById(R.id.sizePanel);
btnUndo = findViewById(R.id.btnUndo);
btnRedo = findViewById(R.id.btnRedo);
btnSave = findViewById(R.id.btnSave);
btnBrush = findViewById(R.id.btnBrush);
btnEraserTool = findViewById(R.id.btnEraser);
btnColorTool = findViewById(R.id.btnColor);
btnSizeTool = findViewById(R.id.btnSize);
tvSizeChip = findViewById(R.id.tvSizeChip);
currentColorDot = findViewById(R.id.currentColorDot);
```

- [ ] **Step 3: Add panel helpers**

Add:

```java
private void showPanel(View panelToShow) {
    colorPanel.setVisibility(panelToShow == colorPanel ? View.VISIBLE : View.GONE);
    sizePanel.setVisibility(panelToShow == sizePanel ? View.VISIBLE : View.GONE);
}

private void hidePanels() {
    colorPanel.setVisibility(View.GONE);
    sizePanel.setVisibility(View.GONE);
}
```

- [ ] **Step 4: Rewire clicks**

Use these mappings:

```java
btnBack.setOnClickListener(v -> finish());
btnTopClear.setOnClickListener(v -> showClearDialog());
btnUndo.setOnClickListener(v -> undoDrawing());
btnRedo.setOnClickListener(v -> redoDrawing());
btnBrush.setOnClickListener(v -> {
    brushManager.setEraser(false);
    hidePanels();
    updateButtonStates();
});
btnEraserTool.setOnClickListener(v -> {
    brushManager.setEraser(true);
    showPanel(sizePanel);
    updateButtonStates();
});
btnColorTool.setOnClickListener(v -> {
    brushManager.setEraser(false);
    hidePanels();
    showColorPickerDialog();
    updateButtonStates();
});
btnSizeTool.setOnClickListener(v -> showPanel(sizePanel));
btnSave.setOnClickListener(v -> saveDrawing());
```

- [ ] **Step 5: Keep undo/redo helper methods small**

Add:

```java
private void undoDrawing() {
    drawingView.getHistoryManager().undo();
    drawingView.invalidate();
    updateButtonStates();
}

private void redoDrawing() {
    drawingView.getHistoryManager().redo();
    drawingView.invalidate();
    updateButtonStates();
}
```

- [ ] **Step 6: Build**

Run:

```bash
./gradlew :app:assembleDebug
```

Expected: Java compile succeeds after all IDs are present.

---

## Task 5: Implement Inline Color Panel

**Files:**
- Modify: `app/src/main/res/layout/activity_main.xml`
- Modify: `app/src/main/java/com/chenjinxiang/doodleboard/MainActivity.java`
- Modify: `app/src/main/res/drawable/` shape resources as needed

- [ ] **Step 1: Add swatch views in `colorPanel`**

Add views with these IDs:

```xml
@+id/colorBlack
@+id/colorRed
@+id/colorBlue
@+id/colorGreen
@+id/colorYellow
@+id/colorOrange
@+id/colorPurple
@+id/colorBrown
@+id/colorCustom
```

- [ ] **Step 2: Replace dynamic `colorContainer` setup with explicit swatch setup**

In `MainActivity`, add:

```java
private void setupColorPanel() {
    int[] viewIds = {
        R.id.colorBlack, R.id.colorRed, R.id.colorBlue, R.id.colorGreen,
        R.id.colorYellow, R.id.colorOrange, R.id.colorPurple, R.id.colorBrown
    };

    for (int i = 0; i < viewIds.length; i++) {
        final int colorIndex = i;
        findViewById(viewIds[i]).setOnClickListener(v -> {
            brushManager.setPresetColor(colorIndex);
            brushManager.setEraser(false);
            updateColorSelection();
            updateButtonStates();
        });
    }

    findViewById(R.id.colorCustom).setOnClickListener(v -> showColorPickerDialog());
}
```

- [ ] **Step 3: Update current color indicator**

Add:

```java
private void updateCurrentColorIndicator() {
    currentColorDot.getBackground().setTint(brushManager.getColor());
}
```

Call it from `updateColorSelection()` and after custom color selection.

- [ ] **Step 4: Use selected swatch state**

For selected preset colors, set `View#setSelected(true)` on the matching swatch and `false` on the others. Use a selector drawable with a blue outline for selected state.

- [ ] **Step 5: Build and smoke test**

Run:

```bash
./gradlew :app:assembleDebug
```

Expected: tapping color opens the panel; selecting a swatch changes subsequent stroke color and updates the toolbar dot.

---

## Task 6: Implement Brush Size And Eraser Panel

**Files:**
- Modify: `app/src/main/res/layout/activity_main.xml`
- Modify: `app/src/main/java/com/chenjinxiang/doodleboard/MainActivity.java`

- [ ] **Step 1: Add size panel controls**

`sizePanel` must contain:

```xml
<com.google.android.material.slider.Slider
    android:id="@+id/inlineSizeSlider"
    android:valueFrom="1"
    android:valueTo="50"
    android:stepSize="1" />

<Button android:id="@+id/btnSize2" android:text="2px" />
<Button android:id="@+id/btnSize8" android:text="8px" />
<Button android:id="@+id/btnSize20" android:text="20px" />
<Button android:id="@+id/btnSize40" android:text="40px" />
```

- [ ] **Step 2: Bind size controls**

Add fields:

```java
private Slider inlineSizeSlider;
private TextView tvPanelSizeValue;
```

Bind:

```java
inlineSizeSlider = findViewById(R.id.inlineSizeSlider);
tvPanelSizeValue = findViewById(R.id.tvPanelSizeValue);
```

- [ ] **Step 3: Add setup method**

```java
private void setupSizePanel() {
    inlineSizeSlider.setValue(brushManager.getWidth());
    inlineSizeSlider.addOnChangeListener((slider, value, fromUser) -> {
        brushManager.setWidth(value);
        updateSizeIndicators(value);
    });

    findViewById(R.id.btnSize2).setOnClickListener(v -> inlineSizeSlider.setValue(2));
    findViewById(R.id.btnSize8).setOnClickListener(v -> inlineSizeSlider.setValue(8));
    findViewById(R.id.btnSize20).setOnClickListener(v -> inlineSizeSlider.setValue(20));
    findViewById(R.id.btnSize40).setOnClickListener(v -> inlineSizeSlider.setValue(40));
}

private void updateSizeIndicators(float size) {
    String label = getString(R.string.current_size, (int) size);
    tvSizeChip.setText(label);
    tvPanelSizeValue.setText(label);
}
```

- [ ] **Step 4: Keep eraser activation visible**

In `updateButtonStates()`, set selected state:

```java
btnEraserTool.setSelected(brushManager.isEraser());
btnBrush.setSelected(!brushManager.isEraser());
```

- [ ] **Step 5: Build and smoke test**

Run:

```bash
./gradlew :app:assembleDebug
```

Expected: tapping `粗细` opens the size panel; slider and preset buttons update brush width immediately; tapping `橡皮` turns on eraser and opens the same panel.

---

## Task 7: Keep Custom Color Picker Available

**Files:**
- Modify: `app/src/main/java/com/chenjinxiang/doodleboard/MainActivity.java`
- Modify: `app/src/main/res/layout/dialog_color_picker.xml`
- Modify: `app/src/main/java/com/chenjinxiang/doodleboard/ui/ColorPickerDialog.java`

- [ ] **Step 1: Keep existing RGB dialog as the full picker entry**

Use the existing `showColorPickerDialog()` from the color tool and optional custom-color swatch:

```java
private void showColorPickerDialog() {
    new ColorPickerDialog(this, brushManager.getColor(), color -> {
        brushManager.setColor(color);
        brushManager.setEraser(false);
        updateColorSelection();
        updateButtonStates();
        hidePanels();
    }).show();
}
```

- [x] **Step 2: Implement interactive color wheel** ✅ COMPLETED (2026-05-31)

Created `PaletteWheelView.java` with:
- Touch event handling for hue ring and saturation/value square
- HSV to RGB color calculation
- `OnColorChangeListener` callback interface
- Dynamic handle position updates

The color wheel is now fully interactive - users can tap/drag on the ring to change hue, or tap/drag in the center square to adjust saturation and brightness.

- [x] **Step 3: Integrate color wheel with ColorPickerDialog** ✅ COMPLETED (2026-05-31)

- Added `colorWheel` reference in ColorPickerDialog
- Implemented bidirectional sync between wheel and RGB sliders
- Added `isUpdatingFromWheel` flag to prevent update loops
- Common color swatches now sync with the color wheel

- [x] **Step 4: Build and verify**

Run:
```bash
./gradlew :app:assembleDebug
```

Result: ✅ Build successful. The original phase-1 note "Do not show a hue wheel in this phase unless it is interactive" has been satisfied - the hue wheel is now fully implemented with touch interaction.

- [ ] **Step 3: Build**

Run:

```bash
./gradlew :app:assembleDebug
```

Expected: custom color can still be selected and applied to new strokes.

---

## Task 8: Update Layout Tests

**Files:**
- Modify: `app/src/androidTest/java/com/chenjinxiang/doodleboard/LayoutTest.java`

- [ ] **Step 1: Update required IDs**

Assert these IDs exist:

```java
assertNotNull(rootView.findViewById(R.id.drawingView));
assertNotNull(rootView.findViewById(R.id.topBar));
assertNotNull(rootView.findViewById(R.id.btnBack));
assertNotNull(rootView.findViewById(R.id.btnTopClear));
assertEquals(0, context.getResources().getIdentifier("tvBoardTitle", "id", context.getPackageName()));
assertNotNull(rootView.findViewById(R.id.bottomToolbar));
assertNotNull(rootView.findViewById(R.id.btnUndo));
assertNotNull(rootView.findViewById(R.id.btnRedo));
assertNotNull(rootView.findViewById(R.id.btnBrush));
assertNotNull(rootView.findViewById(R.id.btnEraser));
assertNotNull(rootView.findViewById(R.id.btnColor));
assertNotNull(rootView.findViewById(R.id.btnSize));
assertEquals(0, context.getResources().getIdentifier("btnMore", "id", context.getPackageName()));
assertNotNull(rootView.findViewById(R.id.btnSave));
assertNotNull(rootView.findViewById(R.id.colorPanel));
assertNotNull(rootView.findViewById(R.id.sizePanel));
```

- [ ] **Step 2: Assert panels start hidden**

Add:

```java
assertEquals(View.GONE, rootView.findViewById(R.id.colorPanel).getVisibility());
assertEquals(View.GONE, rootView.findViewById(R.id.sizePanel).getVisibility());
```

- [ ] **Step 3: Run tests**

Run:

```bash
./gradlew :app:connectedDebugAndroidTest
```

Expected: layout tests pass on a connected emulator/device.

---

## Task 9: Final Verification

**Files:**
- No code changes expected.

- [ ] **Step 1: Run unit tests**

Run:

```bash
./gradlew :app:testDebugUnitTest
```

Expected: all unit tests pass.

- [ ] **Step 2: Build debug APK**

Run:

```bash
./gradlew :app:assembleDebug
```

Expected: `app/build/outputs/apk/debug/app-debug.apk` is produced.

- [ ] **Step 3: Manual visual QA on emulator**

Install:

```bash
./gradlew :app:installDebug
```

Check:

- Main screen matches the approved prototype structure.
- Toolbar icons use supplied SVG assets, not text glyph stand-ins.
- Undo/redo enabled state still updates after drawing.
- Color panel opens and closes cleanly.
- Size panel opens from `粗细` and from `橡皮`.
- Save still writes a PNG to the gallery.
- Drawing area remains usable and is not blocked except by visible floating panels.

- [ ] **Step 4: Review git diff**

Run:

```bash
git diff -- app/src/main docs/superpowers/plans/2026-05-31-material3-ui-refresh.md
```

Expected: diff contains only the UI refresh, icon/resource additions, test updates, and this plan.

---

## Deferred Work

- Flood fill tool.
- Layer management.
- ~~Pixel-perfect hue wheel implementation.~~ ✅ COMPLETED (2026-05-31)
- Animated panel transitions.
- Landscape/tablet-specific layouts.

These are intentionally outside this UI refresh so the first implementation stays reviewable and stable.

**Update (2026-05-31):** The color wheel (PaletteWheelView) has been implemented with full touch interaction, HSV color calculation, and bidirectional sync with RGB sliders. This feature is now functional.

---

## v1.4 Updates (2026-05-31)

### Task: UI Refinements

**Status:** ✅ COMPLETED

- [x] **Remove eraser mode toast**
  - Removed `showEraserToast()` call from eraser button click handler
  - Deleted `showEraserToast()` method from MainActivity

- [x] **Fix size chip text wrapping**
  - Increased `tvSizeChip` width from 34dp to 40dp
  - Added `maxLines="1"` to prevent text wrapping
  - Ensures "10px", "20px", "40px" display on single line

---

## Additional Deferred Work

- **Activity state persistence** - Prevent canvas clearing on back button (requires Stroke serialization, HistoryManager bundle support, MainActivity lifecycle handling)
- Flood fill tool
- Layer management
- Animated panel transitions
- Landscape/tablet-specific layouts
