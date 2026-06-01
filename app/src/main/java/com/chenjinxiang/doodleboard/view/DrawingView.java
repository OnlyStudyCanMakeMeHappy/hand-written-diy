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
import com.chenjinxiang.doodleboard.render.DoodleRenderer;

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

    public interface OnDrawingChangeListener {
        void onDrawingChanged();
    }

    private OnHistoryChangeListener historyChangeListener;
    private OnDrawingChangeListener drawingChangeListener;
    private Paint cursorPaint;
    private DoodleRenderer renderer;
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
        renderer = new DoodleRenderer();

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

        renderer.render(
            canvas,
            historyManager.getStrokes(),
            currentPath,
            brushManager.getColor(),
            brushManager.getAlpha(),
            brushManager.getWidth(),
            brushManager.isEraser()
        );

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
        notifyDrawingChanged();
        notifyHistoryChanged();
    }

    /**
     * 设置历史状态变化监听器
     */
    public void setOnHistoryChangeListener(OnHistoryChangeListener listener) {
        this.historyChangeListener = listener;
    }

    public void setOnDrawingChangeListener(OnDrawingChangeListener listener) {
        this.drawingChangeListener = listener;
    }

    /**
     * 通知监听器历史状态已变化
     */
    private void notifyHistoryChanged() {
        if (historyChangeListener != null) {
            historyChangeListener.onHistoryChanged();
        }
    }

    private void notifyDrawingChanged() {
        if (drawingChangeListener != null) {
            drawingChangeListener.onDrawingChanged();
        }
    }

    public Path getCurrentPathSnapshot() {
        return currentPath == null ? null : new Path(currentPath);
    }

    public int getCurrentRenderColor() {
        return brushManager.getColor();
    }

    public int getCurrentRenderAlpha() {
        return brushManager.getAlpha();
    }

    public float getCurrentRenderWidth() {
        return brushManager.getWidth();
    }

    public boolean isCurrentRenderEraser() {
        return brushManager.isEraser();
    }

    public void notifyExternalDrawingChanged() {
        notifyDrawingChanged();
    }

    /**
     * 获取画布内容的 Bitmap
     */
    public Bitmap getCanvasBitmap() {
        Bitmap result = Bitmap.createBitmap(getWidth(), getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(result);

        renderer.render(
            canvas,
            historyManager.getStrokes(),
            null,
            brushManager.getColor(),
            brushManager.getAlpha(),
            brushManager.getWidth(),
            brushManager.isEraser()
        );

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

        invalidate();
        notifyDrawingChanged();
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
            notifyDrawingChanged();
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
            notifyDrawingChanged();
            notifyHistoryChanged(); // 通知历史状态变化
        }
    }
}
