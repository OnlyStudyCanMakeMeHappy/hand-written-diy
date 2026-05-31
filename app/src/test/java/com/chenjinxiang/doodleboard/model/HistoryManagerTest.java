package com.chenjinxiang.doodleboard.model;

import android.graphics.Path;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.*;

/**
 * HistoryManager 单元测试
 */
@RunWith(RobolectricTestRunner.class)
public class HistoryManagerTest {

    private HistoryManager historyManager;

    @Before
    public void setUp() {
        historyManager = new HistoryManager();
    }

    private Stroke createTestStroke() {
        Path path = new Path();
        path.moveTo(0, 0);
        path.lineTo(10, 10);
        return new Stroke(path, 0xFF000000, 180, 8f, false);
    }

    @Test
    public void testAddStroke_increasesStrokesList() {
        historyManager.addStroke(createTestStroke());

        assertEquals(1, historyManager.getStrokes().size());
        assertEquals(1, historyManager.getStrokes().size());
    }

    @Test
    public void testAddStroke_canUndo() {
        historyManager.addStroke(createTestStroke());

        assertTrue(historyManager.canUndo());
    }

    @Test
    public void testAddStroke_clearsRedoStack() {
        historyManager.addStroke(createTestStroke());
        historyManager.undo();
        assertTrue(historyManager.canRedo());

        // 新笔画后应清空 redoStack
        historyManager.addStroke(createTestStroke());
        assertFalse(historyManager.canRedo());
    }

    @Test
    public void testUndo_removesStroke() {
        Stroke stroke = createTestStroke();
        historyManager.addStroke(stroke);

        historyManager.undo();

        assertEquals(0, historyManager.getStrokes().size());
        assertFalse(historyManager.canUndo());
        assertTrue(historyManager.canRedo());
    }

    @Test
    public void testRedo_restoresStroke() {
        Stroke stroke = createTestStroke();
        historyManager.addStroke(stroke);
        historyManager.undo();

        historyManager.redo();

        assertEquals(1, historyManager.getStrokes().size());
        assertTrue(historyManager.canUndo());
        assertFalse(historyManager.canRedo());
    }

    @Test
    public void testMaxUndo_limit() {
        // 添加 60 笔，应只能撤销 50 笔
        for (int i = 0; i < 60; i++) {
            historyManager.addStroke(createTestStroke());
        }

        assertEquals(60, historyManager.getStrokes().size());  // 全部笔画保留

        // 撤销 50 笔
        for (int i = 0; i < 50; i++) {
            assertTrue("Undo " + i + " should succeed", historyManager.canUndo());
            historyManager.undo();
        }

        // 第 51 次撤销应失败
        assertFalse("Should not be able to undo beyond limit", historyManager.canUndo());
        assertEquals(10, historyManager.getStrokes().size());  // 前 10 笔已固化
    }

    @Test
    public void testClear_clearsAll() {
        historyManager.addStroke(createTestStroke());
        historyManager.addStroke(createTestStroke());
        historyManager.undo();

        historyManager.clear();

        assertEquals(0, historyManager.getStrokes().size());
        assertFalse(historyManager.canUndo());
        assertFalse(historyManager.canRedo());
    }

    @Test
    public void testMultipleUndoRedo() {
        historyManager.addStroke(createTestStroke());
        historyManager.addStroke(createTestStroke());
        historyManager.addStroke(createTestStroke());

        // 撤销 2 笔
        historyManager.undo();
        historyManager.undo();
        assertEquals(1, historyManager.getStrokes().size());
        assertTrue("Should have 2 strokes in redo stack", historyManager.canRedo());

        // 重做 1 笔
        historyManager.redo();
        assertEquals(2, historyManager.getStrokes().size());
        assertTrue("Should still have 1 stroke in redo stack", historyManager.canRedo());
    }
}
