package com.chenjinxiang.doodleboard.projection;

import android.content.Context;
import android.hardware.display.DisplayManager;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;

import com.chenjinxiang.doodleboard.view.DrawingView;

public class ProjectorController {
    private static final String TAG = "ProjectorController";

    private final Context context;
    private final DrawingView sourceView;
    private final DisplayManager displayManager;
    private DrawingPresentation presentation;
    private boolean listenerRegistered;

    private final DisplayManager.DisplayListener displayListener =
        new DisplayManager.DisplayListener() {
            @Override
            public void onDisplayAdded(int displayId) {
                startIfAvailable();
            }

            @Override
            public void onDisplayRemoved(int displayId) {
                if (presentation != null && presentation.getDisplay().getDisplayId() == displayId) {
                    dismissPresentation();
                    startIfAvailable();
                }
            }

            @Override
            public void onDisplayChanged(int displayId) {
                invalidate();
            }
        };

    public ProjectorController(Context context, DrawingView sourceView) {
        this.context = context;
        this.sourceView = sourceView;
        this.displayManager = (DisplayManager) context.getSystemService(Context.DISPLAY_SERVICE);
    }

    public void start() {
        if (displayManager == null) {
            return;
        }
        if (!listenerRegistered) {
            displayManager.registerDisplayListener(displayListener, null);
            listenerRegistered = true;
        }
        startIfAvailable();
    }

    public void stop() {
        dismissPresentation();
        if (displayManager != null && listenerRegistered) {
            displayManager.unregisterDisplayListener(displayListener);
            listenerRegistered = false;
        }
    }

    public void destroy() {
        stop();
    }

    public void invalidate() {
        if (presentation != null) {
            presentation.refresh();
        }
    }

    private void dismissPresentation() {
        if (presentation != null) {
            presentation.dismiss();
            presentation = null;
        }
    }

    private void startIfAvailable() {
        if (displayManager == null || presentation != null) {
            return;
        }
        Display[] displays = displayManager.getDisplays(DisplayManager.DISPLAY_CATEGORY_PRESENTATION);
        if (displays.length == 0) {
            return;
        }
        try {
            presentation = new DrawingPresentation(context, displays[0], sourceView);
            presentation.show();
        } catch (WindowManager.InvalidDisplayException exception) {
            Log.w(TAG, "Projector display is no longer available", exception);
            presentation = null;
        }
    }
}
