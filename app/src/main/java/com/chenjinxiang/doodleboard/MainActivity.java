package com.chenjinxiang.doodleboard;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.chenjinxiang.doodleboard.model.BrushManager;
import com.chenjinxiang.doodleboard.ui.BrushSizeDialog;
import com.chenjinxiang.doodleboard.ui.ColorPickerDialog;
import com.chenjinxiang.doodleboard.utils.FileSaver;
import com.chenjinxiang.doodleboard.view.DrawingView;
import com.google.android.material.snackbar.Snackbar;

public class MainActivity extends AppCompatActivity {

    private DrawingView drawingView;
    private BrushManager brushManager;

    // 底部工具栏控件
    private ImageButton btnUndo, btnRedo;
    private ImageButton btnEraser, btnClear, btnMoreColors;
    private LinearLayout colorContainer;

    // 颜色选择视图数组
    private View[] colorViews;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        setupColorPalette();
        setupToolbarButtons();
        updateButtonStates();
    }

    private void initViews() {
        drawingView = findViewById(R.id.drawingView);
        brushManager = drawingView.getBrushManager();

        btnUndo = findViewById(R.id.btnUndo);
        btnRedo = findViewById(R.id.btnRedo);
        btnEraser = findViewById(R.id.btnEraser);
        btnClear = findViewById(R.id.btnClear);
        btnMoreColors = findViewById(R.id.btnMoreColors);
        colorContainer = findViewById(R.id.colorContainer);

        findViewById(R.id.btnBrushSize).setOnClickListener(v -> showBrushSizeDialog());
        findViewById(R.id.btnSave).setOnClickListener(v -> saveDrawing());
    }

    private void setupColorPalette() {
        colorViews = new View[BrushManager.PRESET_COLORS.length];

        for (int i = 0; i < BrushManager.PRESET_COLORS.length; i++) {
            View colorView = createColorView(BrushManager.PRESET_COLORS[i], i);
            colorContainer.addView(colorView);
            colorViews[i] = colorView;
        }

        updateColorSelection();
    }

    private View createColorView(int color, int index) {
        View colorCircle = new View(this);
        int size = getResources().getDimensionPixelSize(R.dimen.color_circle_size);
        int margin = getResources().getDimensionPixelSize(R.dimen.color_spacing);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(size, size);
        params.setMargins(margin, margin, margin, margin);
        colorCircle.setLayoutParams(params);

        colorCircle.setBackgroundColor(color);
        colorCircle.setTag(index);

        colorCircle.setOnClickListener(v -> {
            int colorIndex = (int) v.getTag();
            brushManager.setPresetColor(colorIndex);
            brushManager.setEraser(false);
            updateColorSelection();
            updateButtonStates();
        });

        return colorCircle;
    }

    private void updateColorSelection() {
        int selectedIndex = brushManager.getPresetColorIndex();

        for (int i = 0; i < colorViews.length; i++) {
            View colorView = colorViews[i];
            ViewGroup.LayoutParams params = colorView.getLayoutParams();

            if (i == selectedIndex) {
                // 高亮选中的颜色
                colorView.setElevation(8);
            } else {
                colorView.setElevation(0);
            }
        }
    }

    private void setupToolbarButtons() {
        // 撤销/重做
        btnUndo.setOnClickListener(v -> {
            drawingView.getHistoryManager().undo();
            drawingView.invalidate();
            updateButtonStates();
        });

        btnRedo.setOnClickListener(v -> {
            drawingView.getHistoryManager().redo();
            drawingView.invalidate();
            updateButtonStates();
        });

        // 更多颜色
        btnMoreColors.setOnClickListener(v -> showColorPickerDialog());

        // 橡皮擦
        btnEraser.setOnClickListener(v -> {
            brushManager.toggleEraser();
            updateButtonStates();
            showEraserToast();
        });

        // 清空
        btnClear.setOnClickListener(v -> showClearDialog());
    }

    private void updateButtonStates() {
        btnUndo.setEnabled(drawingView.getHistoryManager().canUndo());
        btnRedo.setEnabled(drawingView.getHistoryManager().canRedo());

        boolean isEraser = brushManager.isEraser();
        btnEraser.setSelected(isEraser);

        if (isEraser) {
            btnEraser.setBackgroundColor(Color.parseColor("#E0E0E0"));
        } else {
            btnEraser.setBackgroundColor(Color.TRANSPARENT);
        }
    }

    private void showEraserToast() {
        if (brushManager.isEraser()) {
            Toast.makeText(this, R.string.eraser_mode, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, R.string.draw_mode, Toast.LENGTH_SHORT).show();
        }
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
        }).show();
    }

    private void showBrushSizeDialog() {
        new BrushSizeDialog(this, brushManager.getWidth(), size -> {
            brushManager.setWidth(size);
        }).show();
    }

    private void saveDrawing() {
        Bitmap bitmap = drawingView.getCanvasBitmap();

        if (bitmap != null) {
            boolean success = FileSaver.saveToGallery(this, bitmap);

            String message = success ? getString(R.string.saved_to_gallery) : "保存失败";

            Snackbar snackbar = Snackbar.make(
                findViewById(R.id.bottomToolbar),
                message,
                Snackbar.LENGTH_LONG
            );

            if (success) {
                snackbar.setAction(R.string.view, v -> {
                    // TODO: 打开相册查看保存的图片
                });
            }

            snackbar.show();
        }
    }
}
