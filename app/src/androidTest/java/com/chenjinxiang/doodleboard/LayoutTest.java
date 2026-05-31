package com.chenjinxiang.doodleboard;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;

import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

public class LayoutTest {

    @Test
    public void activity_main_layout_exists_and_can_be_inflated() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();

        try {
            LayoutInflater.from(context).inflate(
                    com.chenjinxiang.doodleboard.R.layout.activity_main,
                    null
            );
        } catch (Exception e) {
            fail("activity_main.xml should exist and be valid: " + e.getMessage());
        }
    }

    @Test
    public void layout_contains_material3_toolbar_and_panels() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        View rootView = LayoutInflater.from(context).inflate(
                com.chenjinxiang.doodleboard.R.layout.activity_main,
                null
        );

        assertNotNull(rootView.findViewById(R.id.drawingView));
        assertNotNull(rootView.findViewById(R.id.topBar));
        assertNotNull(rootView.findViewById(R.id.btnBack));
        assertNotNull(rootView.findViewById(R.id.btnTopClear));
        assertEquals(0, context.getResources().getIdentifier(
                "tvBoardTitle",
                "id",
                context.getPackageName()
        ));
        assertNotNull(rootView.findViewById(R.id.bottomToolbar));
        assertNotNull(rootView.findViewById(R.id.btnUndo));
        assertNotNull(rootView.findViewById(R.id.btnRedo));
        assertNotNull(rootView.findViewById(R.id.btnBrush));
        assertNotNull(rootView.findViewById(R.id.btnEraser));
        assertNotNull(rootView.findViewById(R.id.btnColor));
        assertNotNull(rootView.findViewById(R.id.btnSize));
        assertEquals(0, context.getResources().getIdentifier(
                "btnMore",
                "id",
                context.getPackageName()
        ));
        assertNotNull(rootView.findViewById(R.id.btnSave));
        assertNotNull(rootView.findViewById(R.id.colorPanel));
        assertNotNull(rootView.findViewById(R.id.sizePanel));
    }

    @Test
    public void inline_panels_start_hidden() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        View rootView = LayoutInflater.from(context).inflate(
                com.chenjinxiang.doodleboard.R.layout.activity_main,
                null
        );

        assertEquals(View.GONE, rootView.findViewById(R.id.colorPanel).getVisibility());
        assertEquals(View.GONE, rootView.findViewById(R.id.sizePanel).getVisibility());
    }

    @Test
    public void color_picker_layout_contains_palette_controls() {
        Context context = InstrumentationRegistry.getInstrumentation().getTargetContext();
        View rootView = LayoutInflater.from(context).inflate(
                com.chenjinxiang.doodleboard.R.layout.dialog_color_picker,
                null
        );

        assertNotNull(rootView.findViewById(R.id.colorWheel));
        assertNotNull(rootView.findViewById(R.id.hexValue));
        assertNotNull(rootView.findViewById(R.id.rgbValue));
        assertNotNull(rootView.findViewById(R.id.commonColorContainer));
    }
}
