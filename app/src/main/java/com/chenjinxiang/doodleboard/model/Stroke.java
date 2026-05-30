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
        this.path = new Path(path);     // 深拷贝 Path，确保不可变性
        this.color = color;
        this.alpha = alpha;
        this.width = width;
        this.eraser = eraser;
    }

    public Path getPath() {
        return new Path(path);    // 返回副本，确保不可变性
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
