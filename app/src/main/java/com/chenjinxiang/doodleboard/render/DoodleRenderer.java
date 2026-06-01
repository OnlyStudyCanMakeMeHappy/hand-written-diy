package com.chenjinxiang.doodleboard.render;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;

import com.chenjinxiang.doodleboard.model.Stroke;

import java.util.List;

public class DoodleRenderer {
    private final Paint strokePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public DoodleRenderer() {
        strokePaint.setStrokeCap(Paint.Cap.ROUND);
        strokePaint.setStrokeJoin(Paint.Join.ROUND);
        strokePaint.setStyle(Paint.Style.STROKE);
    }

    public void render(
        Canvas canvas,
        List<Stroke> strokes,
        Path currentPath,
        int currentColor,
        int currentAlpha,
        float currentWidth,
        boolean currentEraser
    ) {
        canvas.drawColor(Color.WHITE);
        for (Stroke stroke : strokes) {
            drawStroke(canvas, stroke);
        }
        if (currentPath != null) {
            strokePaint.setColor(currentEraser ? Color.WHITE : currentColor);
            strokePaint.setAlpha(currentEraser ? 255 : currentAlpha);
            strokePaint.setStrokeWidth(currentWidth);
            canvas.drawPath(currentPath, strokePaint);
        }
    }

    private void drawStroke(Canvas canvas, Stroke stroke) {
        strokePaint.setColor(stroke.isEraser() ? Color.WHITE : stroke.getColor());
        strokePaint.setAlpha(stroke.isEraser() ? 255 : stroke.getAlpha());
        strokePaint.setStrokeWidth(stroke.getWidth());
        canvas.drawPath(stroke.getPath(), strokePaint);
    }
}
