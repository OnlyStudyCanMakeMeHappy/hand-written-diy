package com.chenjinxiang.doodleboard.ui;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * TDD: 笔刷粗细对话框测试
 * Task 11: 创建 BrushSizeDialog
 */
public class BrushSizeDialogTest {

    @Test
    public void brushSizeDialog_class_exists() {
        // GIVEN: 一个上下文和初始粗细
        // WHEN: 创建 BrushSizeDialog
        // THEN: 类应该存在且可编译

        assertNotNull("BrushSizeDialog should be loadable", BrushSizeDialog.class);
    }

    @Test
    public void brushSizeDialog_has_listener_interface() {
        // THEN: OnSizeSelectedListener 接口应该存在
        assertNotNull("OnSizeSelectedListener should exist",
                BrushSizeDialog.OnSizeSelectedListener.class);
    }
}
