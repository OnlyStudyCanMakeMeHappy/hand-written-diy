package com.chenjinxiang.doodleboard.projection;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;

import com.chenjinxiang.doodleboard.render.DoodleRenderer;
import com.chenjinxiang.doodleboard.view.DrawingView;

public class ProjectorDrawingView extends View {
    private final DoodleRenderer renderer = new DoodleRenderer();
    private DrawingView sourceView;

    public ProjectorDrawingView(Context context) {
        super(context);
    }

    public ProjectorDrawingView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void bindSource(DrawingView sourceView) {
        this.sourceView = sourceView;
        invalidate();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (sourceView == null) {
            canvas.drawColor(Color.WHITE);
            return;
        }

        float sourceWidth = Math.max(1f, sourceView.getWidth());
        float sourceHeight = Math.max(1f, sourceView.getHeight());
        float scale = Math.min(getWidth() / sourceWidth, getHeight() / sourceHeight);
        float dx = (getWidth() - sourceWidth * scale) / 2f;
        float dy = (getHeight() - sourceHeight * scale) / 2f;

        canvas.save();
        canvas.translate(dx, dy);
        canvas.scale(scale, scale);
        renderer.render(
            canvas,
            sourceView.getHistoryManager().getStrokes(),
            sourceView.getCurrentPathSnapshot(),
            sourceView.getCurrentRenderColor(),
            sourceView.getCurrentRenderAlpha(),
            sourceView.getCurrentRenderWidth(),
            sourceView.isCurrentRenderEraser()
        );
        canvas.restore();
    }
}
