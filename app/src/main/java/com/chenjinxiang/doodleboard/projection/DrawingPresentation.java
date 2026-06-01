package com.chenjinxiang.doodleboard.projection;

import android.app.Presentation;
import android.content.Context;
import android.os.Bundle;
import android.view.Display;

import com.chenjinxiang.doodleboard.view.DrawingView;

public class DrawingPresentation extends Presentation {
    private final DrawingView sourceView;
    private ProjectorDrawingView projectorView;

    public DrawingPresentation(Context context, Display display, DrawingView sourceView) {
        super(context, display);
        this.sourceView = sourceView;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        projectorView = new ProjectorDrawingView(getContext());
        projectorView.bindSource(sourceView);
        setContentView(projectorView);
    }

    public void refresh() {
        if (projectorView != null) {
            projectorView.invalidate();
        }
    }
}
