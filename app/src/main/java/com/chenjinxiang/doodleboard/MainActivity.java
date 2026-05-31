package com.chenjinxiang.doodleboard;

import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.chenjinxiang.doodleboard.model.BrushManager;
import com.chenjinxiang.doodleboard.ui.ColorPickerDialog;
import com.chenjinxiang.doodleboard.utils.FileSaver;
import com.chenjinxiang.doodleboard.view.DrawingView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.slider.Slider;
import com.google.android.material.snackbar.Snackbar;

public class MainActivity extends AppCompatActivity {

    private DrawingView drawingView;
    private BrushManager brushManager;

    private View colorPanel, sizePanel, bottomToolbar;
    private ImageButton btnBack, btnTopClear, btnCloseColorPanel, btnCloseSizePanel;
    private View btnUndo, btnRedo, btnBrush, btnEraser, btnColor, btnSize, btnSave;
    private TextView tvSizeChip, tvPanelSizeValue;
    private View currentColorDot;
    private Slider inlineSizeSlider;

    private View[] colorSwatches;
    private MaterialButton[] sizeButtons;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupToolbarButtons();
        setupColorPanel();
        setupSizePanel();
        updateColorSelection();
        updateSizeIndicators(brushManager.getWidth());
        updateButtonStates();
    }

    private void initViews() {
        drawingView = findViewById(R.id.drawingView);
        brushManager = drawingView.getBrushManager();

        bottomToolbar = findViewById(R.id.bottomToolbar);
        colorPanel = findViewById(R.id.colorPanel);
        sizePanel = findViewById(R.id.sizePanel);

        btnBack = findViewById(R.id.btnBack);
        btnTopClear = findViewById(R.id.btnTopClear);
        btnCloseColorPanel = findViewById(R.id.btnCloseColorPanel);
        btnCloseSizePanel = findViewById(R.id.btnCloseSizePanel);

        btnUndo = findViewById(R.id.btnUndo);
        btnRedo = findViewById(R.id.btnRedo);
        btnBrush = findViewById(R.id.btnBrush);
        btnEraser = findViewById(R.id.btnEraser);
        btnColor = findViewById(R.id.btnColor);
        btnSize = findViewById(R.id.btnSize);
        btnSave = findViewById(R.id.btnSave);

        tvSizeChip = findViewById(R.id.tvSizeChip);
        tvPanelSizeValue = findViewById(R.id.tvPanelSizeValue);
        currentColorDot = findViewById(R.id.currentColorDot);
        inlineSizeSlider = findViewById(R.id.inlineSizeSlider);

        drawingView.setOnHistoryChangeListener(this::updateButtonStates);
    }

    private void setupToolbarButtons() {
        btnBack.setOnClickListener(v -> finish());
        btnTopClear.setOnClickListener(v -> showClearDialog());
        btnCloseColorPanel.setOnClickListener(v -> hidePanels());
        btnCloseSizePanel.setOnClickListener(v -> hidePanels());

        btnUndo.setOnClickListener(v -> undoDrawing());
        btnRedo.setOnClickListener(v -> redoDrawing());

        btnBrush.setOnClickListener(v -> {
            brushManager.setEraser(false);
            hidePanels();
            updateButtonStates();
        });

        btnEraser.setOnClickListener(v -> {
            brushManager.setEraser(true);
            showPanel(sizePanel);
            updateButtonStates();
        });

        btnColor.setOnClickListener(v -> {
            brushManager.setEraser(false);
            hidePanels();
            showColorPickerDialog();
            updateButtonStates();
        });

        btnSize.setOnClickListener(v -> showPanel(sizePanel));
        btnSave.setOnClickListener(v -> saveDrawing());
    }

    private void setupColorPanel() {
        int[] colorViewIds = {
            R.id.colorBlack, R.id.colorRed, R.id.colorBlue, R.id.colorGreen,
            R.id.colorYellow, R.id.colorOrange, R.id.colorPurple, R.id.colorBrown
        };
        colorSwatches = new View[colorViewIds.length];

        for (int i = 0; i < colorViewIds.length; i++) {
            final int colorIndex = i;
            View swatch = findViewById(colorViewIds[i]);
            swatch.setBackground(createSwatchDrawable(BrushManager.PRESET_COLORS[i], false));
            swatch.setOnClickListener(v -> {
                brushManager.setPresetColor(colorIndex);
                brushManager.setEraser(false);
                updateColorSelection();
                updateButtonStates();
            });
            colorSwatches[i] = swatch;
        }

        findViewById(R.id.colorCustom).setOnClickListener(v -> showColorPickerDialog());
    }

    private void setupSizePanel() {
        sizeButtons = new MaterialButton[] {
            findViewById(R.id.btnSize2),
            findViewById(R.id.btnSize8),
            findViewById(R.id.btnSize20),
            findViewById(R.id.btnSize40)
        };

        inlineSizeSlider.setValue(brushManager.getWidth());
        inlineSizeSlider.addOnChangeListener((slider, value, fromUser) -> {
            brushManager.setWidth(value);
            updateSizeIndicators(value);
        });

        float[] sizes = BrushManager.PRESET_SIZES;
        for (int i = 0; i < sizeButtons.length; i++) {
            final float size = sizes[i];
            sizeButtons[i].setOnClickListener(v -> inlineSizeSlider.setValue(size));
        }
    }

    private void showPanel(View panelToShow) {
        colorPanel.setVisibility(panelToShow == colorPanel ? View.VISIBLE : View.GONE);
        sizePanel.setVisibility(panelToShow == sizePanel ? View.VISIBLE : View.GONE);
    }

    private void hidePanels() {
        colorPanel.setVisibility(View.GONE);
        sizePanel.setVisibility(View.GONE);
    }

    private void undoDrawing() {
        drawingView.getHistoryManager().undo();
        drawingView.invalidate();
        updateButtonStates();
    }

    private void redoDrawing() {
        drawingView.getHistoryManager().redo();
        drawingView.invalidate();
        updateButtonStates();
    }

    private void updateButtonStates() {
        updateHistoryButtonState(btnUndo, drawingView.getHistoryManager().canUndo());
        updateHistoryButtonState(btnRedo, drawingView.getHistoryManager().canRedo());

        boolean isEraser = brushManager.isEraser();
        btnBrush.setSelected(!isEraser);
        btnEraser.setSelected(isEraser);
        setToolActive(btnBrush, !isEraser);
        setToolActive(btnEraser, isEraser);
    }

    private void updateHistoryButtonState(View button, boolean enabled) {
        button.setEnabled(enabled);
        button.setAlpha(enabled ? 1f : 0.38f);
    }

    private void setToolActive(View tool, boolean active) {
        View icon = tool instanceof android.view.ViewGroup
            ? ((android.view.ViewGroup) tool).getChildAt(0)
            : tool;
        icon.setBackgroundResource(active ? R.drawable.bg_tool_active : R.drawable.bg_tool_inactive);
    }

    private void updateColorSelection() {
        int selectedIndex = brushManager.getPresetColorIndex();

        if (colorSwatches != null) {
            for (int i = 0; i < colorSwatches.length; i++) {
                colorSwatches[i].setBackground(createSwatchDrawable(
                    BrushManager.PRESET_COLORS[i],
                    i == selectedIndex
                ));
            }
        }

        updateCurrentColorIndicator();
    }

    private void updateCurrentColorIndicator() {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.OVAL);
        drawable.setColor(brushManager.getColor());
        drawable.setStroke(dp(2), Color.WHITE);
        currentColorDot.setBackground(drawable);
    }

    private GradientDrawable createSwatchDrawable(int color, boolean selected) {
        GradientDrawable drawable = new GradientDrawable();
        drawable.setShape(GradientDrawable.RECTANGLE);
        drawable.setCornerRadius(dp(10));
        drawable.setColor(color);
        drawable.setStroke(
            dp(selected ? 4 : 1),
            selected ? getColorCompat(R.color.ui_primary_blue_outline) : getColorCompat(R.color.ui_outline)
        );
        return drawable;
    }

    private void updateSizeIndicators(float size) {
        String label = getString(R.string.current_size, (int) size);
        tvSizeChip.setText(label);
        tvPanelSizeValue.setText(label);

        float[] sizes = BrushManager.PRESET_SIZES;
        if (sizeButtons != null) {
            for (int i = 0; i < sizeButtons.length; i++) {
                boolean selected = Math.round(size) == Math.round(sizes[i]);
                sizeButtons[i].setTextColor(getColorCompat(
                    selected ? R.color.ui_primary_blue : R.color.ui_text_primary
                ));
                sizeButtons[i].setBackgroundTintList(ColorStateList.valueOf(getColorCompat(
                    selected ? R.color.ui_primary_blue_soft : R.color.ui_surface
                )));
                sizeButtons[i].setStrokeColor(ColorStateList.valueOf(getColorCompat(
                    selected ? R.color.ui_primary_blue_outline : R.color.ui_outline
                )));
            }
        }
    }

    private int getColorCompat(int colorRes) {
        return androidx.core.content.ContextCompat.getColor(this, colorRes);
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    private void showClearDialog() {
        new AlertDialog.Builder(this)
            .setTitle(R.string.clear_dialog_title)
            .setMessage(R.string.clear_dialog_message)
            .setPositiveButton(R.string.clear_confirm, (dialog, which) -> {
                drawingView.clear();
                updateButtonStates();
            })
            .setNegativeButton(R.string.cancel, null)
            .show();
    }

    private void showColorPickerDialog() {
        new ColorPickerDialog(this, brushManager.getColor(), color -> {
            brushManager.setColor(color);
            brushManager.setEraser(false);
            updateColorSelection();
            updateButtonStates();
            hidePanels();
        }).show();
    }

    private void saveDrawing() {
        Bitmap bitmap = drawingView.getCanvasBitmap();

        if (bitmap != null) {
            boolean success = FileSaver.saveToGallery(this, bitmap);
            String message = success
                ? getString(R.string.saved_to_gallery)
                : getString(R.string.save_failed);

            Snackbar snackbar = Snackbar.make(bottomToolbar, message, Snackbar.LENGTH_LONG);
            if (success) {
                snackbar.setAction(R.string.view, v -> {
                    // Gallery deep-linking needs the saved Uri, which FileSaver does not expose yet.
                });
            }
            snackbar.show();
        }
    }
}
