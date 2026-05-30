package com.chenjinxiang.doodleboard.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import com.chenjinxiang.doodleboard.model.BrushManager;
import com.chenjinxiang.doodleboard.model.HistoryManager;
import com.chenjinxiang.doodleboard.model.Stroke;

/**
 * 绘图 View
 * 处理触摸事件和绘制逻辑
 * 使用纯 Stroke 方案，每次 onDraw 重绘所有笔画
 */
public class DrawingView extends View {
    private Paint paint;
    private Path currentPath;
    private float lastX, lastY;

    private HistoryManager historyManager;
    private BrushManager brushManager;

    public DrawingView(Context context) {
        super(context);
        init();
    }

    public DrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        historyManager = new HistoryManager();
        brushManager = new BrushManager();

        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStyle(Paint.Style.STROKE);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // 1. 绘制白色背景
        canvas.drawColor(Color.WHITE);

        // 2. 绘制所有已保存的笔画
        for (Stroke stroke : historyManager.getStrokes()) {
            drawStroke(canvas, stroke);
        }

        // 3. 绘制当前正在画的笔画
        if (currentPath != null) {
            paint.setColor(brushManager.isEraser() ? Color.WHITE : brushManager.getColor());
            paint.setAlpha(brushManager.isEraser() ? 255 : brushManager.getAlpha());
            paint.setStrokeWidth(brushManager.getWidth());
            canvas.drawPath(currentPath, paint);
        }
    }

    private void drawStroke(Canvas canvas, Stroke stroke) {
        if (stroke.isEraser()) {
            // MVP: 橡皮擦使用白色画笔模拟
            paint.setColor(Color.WHITE);
            paint.setAlpha(255);
        } else {
            paint.setColor(stroke.getColor());
            paint.setAlpha(stroke.getAlpha());
        }
        paint.setStrokeWidth(stroke.getWidth());
        canvas.drawPath(stroke.getPath(), paint);
    }

    // Getter 方法
    public HistoryManager getHistoryManager() {
        return historyManager;
    }

    public BrushManager getBrushManager() {
        return brushManager;
    }

    public void clear() {
        historyManager.clear();
        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                handleActionDown(x, y);
                break;

            case MotionEvent.ACTION_MOVE:
                handleActionMove(x, y);
                break;

            case MotionEvent.ACTION_UP:
                handleActionUp();
                break;
        }

        return true;
    }

    private void handleActionDown(float x, float y) {
        currentPath = new Path();
        currentPath.moveTo(x, y);
        lastX = x;
        lastY = y;
    }

    private void handleActionMove(float x, float y) {
        if (currentPath != null) {
            // 二阶贝塞尔平滑处理
            float midX = (lastX + x) / 2;
            float midY = (lastY + y) / 2;
            currentPath.quadTo(lastX, lastY, midX, midY);

            lastX = x;
            lastY = y;

            invalidate(); // 实时刷新
        }
    }

    private void handleActionUp() {
        if (currentPath != null) {
            // 保存笔画
            Stroke stroke = new Stroke(
                currentPath,
                brushManager.getColor(),
                brushManager.getAlpha(),
                brushManager.getWidth(),
                brushManager.isEraser()
            );
            historyManager.addStroke(stroke);

            currentPath = null;
            invalidate();
        }
    }
}
