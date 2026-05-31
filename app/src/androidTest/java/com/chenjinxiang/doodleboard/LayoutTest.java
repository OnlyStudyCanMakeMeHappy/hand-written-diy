package com.chenjinxiang.doodleboard;

import android.content.Context;
import android.view.LayoutInflater;

import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

/**
 * TDD: 主界面布局测试
 * Task 9: 创建主界面布局 activity_main.xml
 */
public class LayoutTest {

    @Test
    public void activity_main_layout_exists_and_can_be_inflated() {
        // GIVEN: 应用上下文
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();

        // WHEN: 尝试加载主界面布局
        try {
            LayoutInflater.from(context).inflate(
                    com.chenjinxiang.doodleboard.R.layout.activity_main,
                    null
            );

            // THEN: 布局应成功加载而不抛出异常
            // 如果到这里，说明布局文件存在且格式正确
        } catch (Exception e) {
            fail("activity_main.xml should exist and be valid: " + e.getMessage());
        }
    }

    @Test
    public void layout_contains_required_views() {
        // GIVEN: 应用上下文
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();

        // WHEN: 加载主界面布局
        android.view.View rootView = LayoutInflater.from(context).inflate(
                com.chenjinxiang.doodleboard.R.layout.activity_main,
                null
        );

        // THEN: 所有关键视图 ID 应存在
        // 撤销/重做按钮
        assertNotNull("btnUndo should exist", rootView.findViewById(
                com.chenjinxiang.doodleboard.R.id.btnUndo));
        assertNotNull("btnRedo should exist", rootView.findViewById(
                com.chenjinxiang.doodleboard.R.id.btnRedo));

        // 绘图视图
        assertNotNull("drawingView should exist", rootView.findViewById(
                com.chenjinxiang.doodleboard.R.id.drawingView));

        // 底部工具栏
        assertNotNull("bottomToolbar should exist", rootView.findViewById(
                com.chenjinxiang.doodleboard.R.id.bottomToolbar));

        // 颜色容器
        assertNotNull("colorContainer should exist", rootView.findViewById(
                com.chenjinxiang.doodleboard.R.id.colorContainer));

        // 橡皮擦、清空、保存
        assertNotNull("btnEraser should exist", rootView.findViewById(
                com.chenjinxiang.doodleboard.R.id.btnEraser));
        assertNotNull("btnClear should exist", rootView.findViewById(
                com.chenjinxiang.doodleboard.R.id.btnClear));

        // 更多颜色按钮
        assertNotNull("btnMoreColors should exist", rootView.findViewById(
                com.chenjinxiang.doodleboard.R.id.btnMoreColors));
    }
}
