package com.chenjinxiang.doodleboard.model;

import android.graphics.Color;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * BrushManager 单元测试
 */
public class BrushManagerTest {

    private BrushManager brushManager;

    @Before
    public void setUp() {
        brushManager = new BrushManager();
    }

    // ========== 默认值测试 ==========

    @Test
    public void testDefaultValues() {
        assertEquals("默认颜色应为黑色", Color.BLACK, brushManager.getColor());
        assertEquals("默认粗细应为 8px", 8f, brushManager.getWidth(), 0.01f);
        assertEquals("默认透明度应为 180", 180, brushManager.getAlpha());
        assertFalse("默认不应为橡皮擦模式", brushManager.isEraser());
    }

    // ========== 预设颜色测试 ==========

    @Test
    public void testPresetColors_count() {
        assertEquals("应有 8 个预设颜色", 8, BrushManager.PRESET_COLORS.length);
    }

    @Test
    public void testPresetColors_containsBrown() {
        boolean hasBrown = false;
        for (int color : BrushManager.PRESET_COLORS) {
            if (color == 0xFF8B4513) {  // 棕色
                hasBrown = true;
                break;
            }
        }
        assertTrue("应包含棕色，不包含白色", hasBrown);
    }

    @Test
    public void testSetPresetColor_validIndex() {
        brushManager.setPresetColor(1);  // 红色
        assertEquals(Color.RED, brushManager.getColor());
    }

    @Test
    public void testSetPresetColor_invalidIndex() {
        int originalColor = brushManager.getColor();
        brushManager.setPresetColor(-1);   // 无效索引
        assertEquals("负索引不应改变颜色", originalColor, brushManager.getColor());

        brushManager.setPresetColor(999); // 超出范围
        assertEquals("超出范围索引不应改变颜色", originalColor, brushManager.getColor());
    }

    @Test
    public void testSetPresetColor_allPresets() {
        for (int i = 0; i < BrushManager.PRESET_COLORS.length; i++) {
            brushManager.setPresetColor(i);
            assertEquals("预设颜色 " + i + " 应正确设置",
                    BrushManager.PRESET_COLORS[i], brushManager.getColor());
        }
    }

    // ========== 颜色测试 ==========

    @Test
    public void testSetColor() {
        brushManager.setColor(0xFF123456);
        assertEquals(0xFF123456, brushManager.getColor());
    }

    @Test
    public void testGetPresetColorIndex_withPreset() {
        brushManager.setPresetColor(2);  // 蓝色
        assertEquals("预设颜色应返回对应索引", 2, brushManager.getPresetColorIndex());
    }

    @Test
    public void testGetPresetColorIndex_withCustom() {
        brushManager.setColor(0xFF123456);  // 自定义颜色
        assertEquals("自定义颜色应返回 -1", -1, brushManager.getPresetColorIndex());
    }

    // ========== 粗细测试 ==========

    @Test
    public void testPresetSizes_count() {
        assertEquals("应有 4 个预设粗细", 4, BrushManager.PRESET_SIZES.length);
    }

    @Test
    public void testPresetSizes_values() {
        assertArrayEquals("预设粗细应为 2, 8, 20, 40",
                new float[]{2f, 8f, 20f, 40f}, BrushManager.PRESET_SIZES, 0.01f);
    }

    @Test
    public void testSetPresetSize_validIndex() {
        brushManager.setPresetSize(0);  // 2px
        assertEquals(2f, brushManager.getWidth(), 0.01f);

        brushManager.setPresetSize(3);  // 40px
        assertEquals(40f, brushManager.getWidth(), 0.01f);
    }

    @Test
    public void testSetPresetSize_invalidIndex() {
        float originalWidth = brushManager.getWidth();
        brushManager.setPresetSize(-1);
        assertEquals("负索引不应改变粗细", originalWidth, brushManager.getWidth(), 0.01f);
    }

    @Test
    public void testSetWidth_withinRange() {
        brushManager.setWidth(15);
        assertEquals(15f, brushManager.getWidth(), 0.01f);
    }

    @Test
    public void testSetWidth_belowMinimum() {
        brushManager.setWidth(0);
        assertEquals("低于最小值应限制为 1", 1f, brushManager.getWidth(), 0.01f);

        brushManager.setWidth(-10);
        assertEquals("负值应限制为 1", 1f, brushManager.getWidth(), 0.01f);
    }

    @Test
    public void testSetWidth_aboveMaximum() {
        brushManager.setWidth(100);
        assertEquals("超过最大值应限制为 50", 50f, brushManager.getWidth(), 0.01f);
    }

    @Test
    public void testSetWidth_boundaryValues() {
        brushManager.setWidth(1);
        assertEquals(1f, brushManager.getWidth(), 0.01f);

        brushManager.setWidth(50);
        assertEquals(50f, brushManager.getWidth(), 0.01f);
    }

    // ========== 透明度测试 ==========

    @Test
    public void testDefaultAlpha() {
        assertEquals("马克笔默认透明度应为 180", 180, brushManager.getAlpha());
    }

    @Test
    public void testSetAlpha_withinRange() {
        brushManager.setAlpha(128);
        assertEquals(128, brushManager.getAlpha());
    }

    @Test
    public void testSetAlpha_belowMinimum() {
        brushManager.setAlpha(-10);
        assertEquals("低于最小值应限制为 0", 0, brushManager.getAlpha());
    }

    @Test
    public void testSetAlpha_aboveMaximum() {
        brushManager.setAlpha(300);
        assertEquals("超过最大值应限制为 255", 255, brushManager.getAlpha());
    }

    @Test
    public void testSetAlpha_boundaryValues() {
        brushManager.setAlpha(0);
        assertEquals(0, brushManager.getAlpha());

        brushManager.setAlpha(255);
        assertEquals(255, brushManager.getAlpha());
    }

    // ========== 橡皮擦测试 ==========

    @Test
    public void testDefaultEraserState() {
        assertFalse("默认不应为橡皮擦模式", brushManager.isEraser());
    }

    @Test
    public void testSetEraser_true() {
        brushManager.setEraser(true);
        assertTrue("应启用橡皮擦模式", brushManager.isEraser());
    }

    @Test
    public void testSetEraser_false() {
        brushManager.setEraser(true);
        brushManager.setEraser(false);
        assertFalse("应禁用橡皮擦模式", brushManager.isEraser());
    }

    @Test
    public void testToggleEraser() {
        assertFalse("初始状态非橡皮擦", brushManager.isEraser());

        brushManager.toggleEraser();
        assertTrue("切换后应为橡皮擦", brushManager.isEraser());

        brushManager.toggleEraser();
        assertFalse("再次切换后应非橡皮擦", brushManager.isEraser());
    }
}
