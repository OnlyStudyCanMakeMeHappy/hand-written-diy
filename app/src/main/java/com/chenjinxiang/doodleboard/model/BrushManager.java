package com.chenjinxiang.doodleboard.model;

import android.graphics.Color;

/**
 * 笔刷管理器
 * 管理笔刷颜色、粗细、透明度和橡皮擦状态
 */
public class BrushManager {
    // 预设颜色（黑、红、蓝、绿、黄、橙、紫、棕）
    public static final int[] PRESET_COLORS = {
        Color.BLACK,       // 0
        Color.RED,         // 1
        Color.BLUE,        // 2
        Color.GREEN,       // 3
        Color.YELLOW,      // 4
        0xFFFFA500,        // 5 - 橙色
        0xFF800080,        // 6 - 紫色
        0xFF8B4513         // 7 - 棕色
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
