package com.chenjinxiang.doodleboard.ui;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import com.chenjinxiang.doodleboard.R;
import com.chenjinxiang.doodleboard.model.BrushManager;
import com.google.android.material.slider.Slider;

/**
 * 笔刷粗细调节对话框
 */
public class BrushSizeDialog extends Dialog {

    private Slider sizeSlider;
    private TextView tvSizeValue;
    private Button btnThin, btnMedium, btnThick, btnExtra;
    private Button btnConfirm, btnCancel;

    private float initialSize;
    private float selectedSize;
    private OnSizeSelectedListener listener;

    public interface OnSizeSelectedListener {
        void onSizeSelected(float size);
    }

    public BrushSizeDialog(Context context, float initialSize, OnSizeSelectedListener listener) {
        super(context);
        this.initialSize = initialSize;
        this.selectedSize = initialSize;
        this.listener = listener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_brush_size);

        initViews();
        setupListeners();
    }

    private void initViews() {
        sizeSlider = findViewById(R.id.sizeSlider);
        tvSizeValue = findViewById(R.id.tvSizeValue);
        btnThin = findViewById(R.id.btnSizeThin);
        btnMedium = findViewById(R.id.btnSizeMedium);
        btnThick = findViewById(R.id.btnSizeThick);
        btnExtra = findViewById(R.id.btnSizeExtra);
        btnConfirm = findViewById(R.id.btnConfirm);
        btnCancel = findViewById(R.id.btnCancel);

        // 设置初始值
        sizeSlider.setValue(initialSize);
        updateSizeValue(initialSize);
    }

    private void setupListeners() {
        sizeSlider.addOnChangeListener((slider, value, fromUser) -> {
            updateSizeValue(value);
        });

        // 预设档位按钮
        btnThin.setOnClickListener(v -> {
            sizeSlider.setValue(2);
        });

        btnMedium.setOnClickListener(v -> {
            sizeSlider.setValue(8);
        });

        btnThick.setOnClickListener(v -> {
            sizeSlider.setValue(20);
        });

        btnExtra.setOnClickListener(v -> {
            sizeSlider.setValue(40);
        });

        btnConfirm.setOnClickListener(v -> {
            selectedSize = sizeSlider.getValue();
            if (listener != null) {
                listener.onSizeSelected(selectedSize);
            }
            dismiss();
        });

        btnCancel.setOnClickListener(v -> dismiss());
    }

    private void updateSizeValue(float size) {
        tvSizeValue.setText(String.format(getContext().getString(R.string.current_size), (int) size));
    }
}
