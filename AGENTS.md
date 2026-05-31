# AGENTS.md

This file provides guidance to Codex (Codex.ai/code) when working with code in this repository.

## Project Overview

A simple Android drawing app (涂鸦板) with Material Design 3, supporting marker-style drawing, undo/redo, color/size adjustment, eraser, and save functionality.

**Tech Stack:** Java, Android SDK 29-34, Material Design Components, Canvas/Path/Paint API

## Build Commands

```bash
# Build debug APK
./gradlew assembleDebug

# Build release APK
./gradlew assembleRelease

# Clean build
./gradlew clean

# Install on connected device
./gradlew installDebug
```

## Architecture

The app uses an MV (Model-View) pattern with clear separation:

- **MainActivity**: UI coordination, event handling
- **DrawingView**: Custom View for touch events and rendering
- **Stroke**: Immutable data class (Path + attributes)
- **HistoryManager**: Undo/redo logic (max 50 strokes)
- **BrushManager**: Brush parameters (8 presets, 1-50px range)

### Key Architectural Decisions

**Pure Stroke Rendering (not Bitmap-based)**
- All strokes are redrawn on every `onDraw()` call
- No off-screen Bitmap cache
- Keeps memory usage low, enables无损 editing

**Stroke Immutability**
- `Stroke` constructor performs `new Path(path)` deep copy
- Ensures Path objects can't be modified after Stroke creation
- Critical for undo/redo correctness

**HistoryManager Three-Tier Structure**
```java
List<Stroke> strokes;              // All strokes (full canvas state)
Deque<Stroke> undoableStrokes;      // Only last 50 strokes (undoable range)
Deque<Stroke> redoStack;            // Currently undone strokes
```
- When drawing: add to `strokes` + `undoableStrokes`, clear `redoStack`
- When undo: move from `strokes` + `undoableStrokes` to `redoStack`
- When redo: move from `redoStack` to `strokes` + `undoableStrokes`

**Eraser Implementation**
- MVP: White brush simulation (`Color.WHITE`, alpha 255)
- Not `PorterDuff.Mode.CLEAR` (over-engineering for white background)

### Package Structure

```
com.chenjinxiang.doodleboard
├── MainActivity.java
├── view/
│   └── DrawingView.java
├── model/
│   ├── Stroke.java
│   ├── BrushManager.java
│   └── HistoryManager.java
├── ui/
│   ├── ColorPickerDialog.java
│   └── BrushSizeDialog.java
└── utils/
    └── FileSaver.java
```

## Design Documents

- Spec: `docs/superpowers/specs/2026-05-30-doodle-app-design.md`
- Plan: `docs/superpowers/plans/2026-05-30-doodle-board-implementation.md`

## Android-Specific Notes

- **Screen Orientation**: Portrait only (`android:screenOrientation="portrait"`)
- **Theme**: `Theme.Material3.Light.NoActionBar` (no dark mode support)
- **Min SDK**: 29 (Android 10)
- **Permissions**: None needed (Scoped Storage + MediaStore)
- **Save Format**: PNG to `Pictures/涂鸦板/` with filename `涂鸦_YYYY-MM-DD_HHMMSS.png`



