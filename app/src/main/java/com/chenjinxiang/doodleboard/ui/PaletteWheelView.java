package com.chenjinxiang.doodleboard.ui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.SweepGradient;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

public class PaletteWheelView extends View {

    private final Paint ringPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint squarePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint handlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF squareRect = new RectF();

    // HSV color state
    private float hue = 0f;           // 0-360
    private float saturation = 0f;   // 0-1
    private float value = 1f;        // 0-1

    // Touch interaction
    private boolean isTouchOnRing = false;

    private OnColorChangeListener colorChangeListener;

    public interface OnColorChangeListener {
        void onColorChanged(int color);
    }

    public PaletteWheelView(Context context) {
        super(context);
        init();
    }

    public PaletteWheelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        ringPaint.setStyle(Paint.Style.STROKE);
        ringPaint.setStrokeCap(Paint.Cap.ROUND);
        handlePaint.setStyle(Paint.Style.STROKE);
        handlePaint.setStrokeWidth(dp(2));
        handlePaint.setColor(Color.WHITE);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float size = Math.min(getWidth(), getHeight());
        float center = size / 2f;
        float ringWidth = size * 0.13f;
        float radius = center - ringWidth;

        // Draw color ring
        ringPaint.setStrokeWidth(ringWidth);
        ringPaint.setShader(new SweepGradient(center, center, new int[] {
            Color.RED, Color.YELLOW, Color.GREEN, Color.CYAN, Color.BLUE, Color.MAGENTA, Color.RED
        }, null));
        canvas.drawCircle(center, center, radius, ringPaint);

        // Draw saturation/value square
        float squareSize = size * 0.47f;
        float squareLeft = center - squareSize / 2f;
        float squareTop = center - squareSize / 2f;
        squareRect.set(squareLeft, squareTop, squareLeft + squareSize, squareTop + squareSize);

        // Horizontal gradient: white to current hue color
        int hueColor = Color.HSVToColor(new float[]{hue, 1f, 1f});
        squarePaint.setShader(new LinearGradient(
            squareRect.left, squareRect.top,
            squareRect.right, squareRect.top,
            Color.WHITE, hueColor,
            Shader.TileMode.CLAMP
        ));
        canvas.drawRoundRect(squareRect, dp(8), dp(8), squarePaint);

        // Vertical gradient: transparent to black
        squarePaint.setShader(new LinearGradient(
            squareRect.left, squareRect.top,
            squareRect.left, squareRect.bottom,
            Color.TRANSPARENT, Color.BLACK,
            Shader.TileMode.CLAMP
        ));
        canvas.drawRoundRect(squareRect, dp(8), dp(8), squarePaint);

        // Draw hue handle on ring
        handlePaint.setShader(null);
        float hueAngle = (float) Math.toRadians(hue - 90);
        float hueHandleX = center + radius * (float) Math.cos(hueAngle);
        float hueHandleY = center + radius * (float) Math.sin(hueAngle);
        canvas.drawCircle(hueHandleX, hueHandleY, dp(7), handlePaint);

        // Draw saturation/value handle in square
        float svHandleX = squareRect.left + squareSize * saturation;
        float svHandleY = squareRect.bottom - squareSize * value;
        canvas.drawCircle(svHandleX, svHandleY, dp(5), handlePaint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float size = Math.min(getWidth(), getHeight());
        float center = size / 2f;
        float ringWidth = size * 0.13f;
        float radius = center - ringWidth;
        float squareSize = size * 0.47f;

        float x = event.getX();
        float y = event.getY();

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                float dx = x - center;
                float dy = y - center;
                float distance = (float) Math.sqrt(dx * dx + dy * dy);

                // Determine if touch is on ring or in square
                if (distance >= radius * 0.7f && distance <= radius * 1.2f) {
                    // Touch on color ring - update hue
                    isTouchOnRing = true;
                    hue = (float) ((Math.atan2(dy, dx) * 180 / Math.PI + 90 + 360) % 360);
                    calculateAndNotifyColor();
                    invalidate();
                    return true;
                } else if (x >= squareRect.left && x <= squareRect.right &&
                           y >= squareRect.top && y <= squareRect.bottom) {
                    // Touch in square - update saturation and value
                    isTouchOnRing = false;
                    saturation = (x - squareRect.left) / squareSize;
                    saturation = Math.max(0, Math.min(1, saturation));
                    value = 1f - (y - squareRect.top) / squareSize;
                    value = Math.max(0, Math.min(1, value));
                    calculateAndNotifyColor();
                    invalidate();
                    return true;
                }
                break;

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_CANCEL:
                isTouchOnRing = false;
                break;
        }

        return super.onTouchEvent(event);
    }

    private void calculateAndNotifyColor() {
        int color = Color.HSVToColor(new float[]{hue, saturation, value});
        if (colorChangeListener != null) {
            colorChangeListener.onColorChanged(color);
        }
    }

    public void setOnColorChangeListener(OnColorChangeListener listener) {
        this.colorChangeListener = listener;
    }

    public int getColor() {
        return Color.HSVToColor(new float[]{hue, saturation, value});
    }

    public void setColor(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        this.hue = hsv[0];
        this.saturation = hsv[1];
        this.value = hsv[2];
        invalidate();
    }

    private float dp(int value) {
        return value * getResources().getDisplayMetrics().density;
    }
}
