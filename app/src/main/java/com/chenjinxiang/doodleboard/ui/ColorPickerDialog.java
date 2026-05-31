package com.chenjinxiang.doodleboard.ui;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;

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
