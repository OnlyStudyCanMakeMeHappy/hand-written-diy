package com.chenjinxiang.doodleboard.view;

import android.content.Context;
import android.graphics.Bitmap;
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

    /**
     * 历史状态变化监听器
     */
    public interface OnHistoryChangeListener {
        void onHistoryChanged();
    }

    private OnHistoryChangeListener historyChangeListener;
    private Paint paint;
    private Paint cursorPaint;
    private Path currentPath;
    private float lastX, lastY;

    // 橡皮擦光标相关
    private float cursorX = -1;
    private float cursorY = -1;
    private boolean isTouching = false;

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

        // 初始化橡皮擦光标画笔
        cursorPaint = new Paint();
        cursorPaint.setAntiAlias(true);
        cursorPaint.setStyle(Paint.Style.STROKE);
        cursorPaint.setColor(Color.GRAY);
        cursorPaint.setStrokeWidth(2f);
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

        // 4. 绘制橡皮擦光标指示器
        if (brushManager.isEraser() && isTouching && cursorX >= 0 && cursorY >= 0) {
            float brushSize = brushManager.getWidth();
            float cursorRadius = brushSize / 2f + 4f; // 稍微大一点以便看到

            // 绘制虚线圆圈表示橡皮擦范围
            cursorPaint.setStyle(Paint.Style.STROKE);
            cursorPaint.setColor(Color.GRAY);
            cursorPaint.setStrokeWidth(2f);
            canvas.drawCircle(cursorX, cursorY, cursorRadius, cursorPaint);

            // 绘制中心点
            cursorPaint.setStyle(Paint.Style.FILL);
            cursorPaint.setColor(Color.parseColor("#E0E0E0"));
            canvas.drawCircle(cursorX, cursorY, 4f, cursorPaint);
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
        notifyHistoryChanged();
    }

    /**
     * 设置历史状态变化监听器
     */
    public void setOnHistoryChangeListener(OnHistoryChangeListener listener) {
        this.historyChangeListener = listener;
    }

    /**
     * 通知监听器历史状态已变化
     */
    private void notifyHistoryChanged() {
        if (historyChangeListener != null) {
            historyChangeListener.onHistoryChanged();
        }
    }

    /**
     * 获取画布内容的 Bitmap
     */
    public Bitmap getCanvasBitmap() {
        Bitmap result = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);

        // 绘制白色背景
        canvas.drawColor(Color.WHITE);

        // 绘制所有笔画
        for (Stroke stroke : historyManager.getStrokes()) {
            drawStroke(canvas, stroke);
        }

        return result;
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
                handleActionUp(x, y);
                break;

            case MotionEvent.ACTION_CANCEL:
                // 手势被系统中断，保存当前笔画
                handleActionUp(x, y);
                break;
        }

        return true;
    }

    private void handleActionDown(float x, float y) {
        isTouching = true;
        cursorX = x;
        cursorY = y;

        currentPath = new Path();
        currentPath.moveTo(x, y);
        lastX = x;
        lastY = y;

        // 橡皮擦模式下需要立即刷新以显示光标
        if (brushManager.isEraser()) {
            invalidate();
        }
    }

    private void handleActionMove(float x, float y) {
        // 更新光标位置
        cursorX = x;
        cursorY = y;

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

    private void handleActionUp(float x, float y) {
        isTouching = false;
        cursorX = -1;
        cursorY = -1;

        if (currentPath != null) {
            // 连接最后一点，避免线条末端略短
            currentPath.lineTo(x, y);

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
            notifyHistoryChanged(); // 通知历史状态变化
        }
    }
}
