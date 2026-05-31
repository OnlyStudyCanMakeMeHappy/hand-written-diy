package com.chenjinxiang.doodleboard.ui;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.chenjinxiang.doodleboard.R;
import com.google.android.material.slider.Slider;

/**
 * 自定义颜色选择对话框
 */
public class ColorPickerDialog extends Dialog {

    private Slider redSlider, greenSlider, blueSlider;
    private View colorPreview;
    private View btnCancel;
    private Button btnConfirm;
    private TextView hexValue, rgbValue;
    private PaletteWheelView colorWheel;

    private int selectedColor = Color.BLACK;
    private OnColorSelectedListener listener;
    private boolean isUpdatingFromWheel = false;

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
        if (getWindow() != null) {
            getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        initViews();
        setupListeners();
        updatePreview();
    }

    private void initViews() {
        redSlider = findViewById(R.id.redSlider);
        greenSlider = findViewById(R.id.greenSlider);
        blueSlider = findViewById(R.id.blueSlider);
        colorPreview = findViewById(R.id.colorPreview);
        hexValue = findViewById(R.id.hexValue);
        rgbValue = findViewById(R.id.rgbValue);
        btnConfirm = findViewById(R.id.btnConfirm);
        btnCancel = findViewById(R.id.btnCancel);
        colorWheel = findViewById(R.id.colorWheel);

        // 设置初始值
        redSlider.setValue(Color.red(selectedColor));
        greenSlider.setValue(Color.green(selectedColor));
        blueSlider.setValue(Color.blue(selectedColor));
        colorWheel.setColor(selectedColor);
        setupCommonColors();
    }

    private void setupListeners() {
        Slider.OnChangeListener sliderListener = new Slider.OnChangeListener() {
            @Override
            public void onValueChange(Slider slider, float value, boolean fromUser) {
                if (!isUpdatingFromWheel) {
                    updateFromSliders();
                }
            }
        };

        redSlider.addOnChangeListener(sliderListener);
        greenSlider.addOnChangeListener(sliderListener);
        blueSlider.addOnChangeListener(sliderListener);

        // Color wheel change listener
        colorWheel.setOnColorChangeListener(color -> {
            isUpdatingFromWheel = true;
            redSlider.setValue(Color.red(color));
            greenSlider.setValue(Color.green(color));
            blueSlider.setValue(Color.blue(color));
            updatePreview();
            isUpdatingFromWheel = false;
        });

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

    private void updateFromSliders() {
        int color = Color.rgb(
            (int) redSlider.getValue(),
            (int) greenSlider.getValue(),
            (int) blueSlider.getValue()
        );
        colorWheel.setColor(color);
        updatePreview();
    }

    private void updatePreview() {
        int color = Color.rgb(
            (int) redSlider.getValue(),
            (int) greenSlider.getValue(),
            (int) blueSlider.getValue()
        );
        GradientDrawable drawable = new GradientDrawable();
        drawable.setColor(color);
        drawable.setCornerRadius(12 * getContext().getResources().getDisplayMetrics().density);
        colorPreview.setBackground(drawable);
        hexValue.setText(String.format("#%06X", 0xFFFFFF & color));
        rgbValue.setText(String.format(
            "%d, %d, %d",
            Color.red(color),
            Color.green(color),
            Color.blue(color)
        ));
    }

    private void setupCommonColors() {
        bindCommonColor(R.id.commonBlue, Color.rgb(26, 115, 232));
        bindCommonColor(R.id.commonRed, Color.RED);
        bindCommonColor(R.id.commonOrange, 0xFFFFA500);
        bindCommonColor(R.id.commonGreen, Color.GREEN);
        bindCommonColor(R.id.commonPurple, 0xFF800080);
        bindCommonColor(R.id.commonBrown, 0xFF8B4513);
    }

    private void bindCommonColor(int viewId, int color) {
        View swatch = findViewById(viewId);
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setCornerRadius(8 * getContext().getResources().getDisplayMetrics().density);
        drawable.setColor(color);
        swatch.setBackground(drawable);
        swatch.setOnClickListener(v -> {
            isUpdatingFromWheel = true;
            redSlider.setValue(Color.red(color));
            greenSlider.setValue(Color.green(color));
            blueSlider.setValue(Color.blue(color));
            colorWheel.setColor(color);
            updatePreview();
            isUpdatingFromWheel = false;
        });
    }
}
