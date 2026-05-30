package com.chenjinxiang.doodleboard.model;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;

/**
 * 历史记录管理器
 * 负责撤销/重做逻辑，最多可撤销 50 笔
 */
public class HistoryManager {
    private static final int MAX_UNDO = 50;

    private final List<Stroke> strokes = new ArrayList<>();
    private final Deque<Stroke> undoableStrokes = new ArrayDeque<>();
    private final Deque<Stroke> redoStack = new ArrayDeque<>();

    /**
     * 添加新笔画
     */
    public void addStroke(Stroke stroke) {
        strokes.add(stroke);
        undoableStrokes.addLast(stroke);
        if (undoableStrokes.size() > MAX_UNDO) {
            undoableStrokes.removeFirst();
        }
        redoStack.clear();
    }

    /**
     * 撤销（移除最后一笔）
     */
    public void undo() {
        if (!undoableStrokes.isEmpty()) {
            Stroke stroke = undoableStrokes.removeLast();

            // 移除最后一笔（而不是搜索 stroke 对象）
            if (!strokes.isEmpty() && strokes.get(strokes.size() - 1) == stroke) {
                strokes.remove(strokes.size() - 1);
            }

            redoStack.addLast(stroke);
        }
    }

    /**
     * 重做
     */
    public void redo() {
        if (!redoStack.isEmpty()) {
            Stroke stroke = redoStack.removeLast();
            strokes.add(stroke);
            undoableStrokes.addLast(stroke);
            if (undoableStrokes.size() > MAX_UNDO) {
                undoableStrokes.removeFirst();
            }
        }
    }

    /**
     * 获取所有笔画（不可修改列表）
     */
    public List<Stroke> getStrokes() {
        return Collections.unmodifiableList(strokes);
    }

    /**
     * 是否可以撤销
     */
    public boolean canUndo() {
        return !undoableStrokes.isEmpty();
    }

    /**
     * 是否可以重做
     */
    public boolean canRedo() {
        return !redoStack.isEmpty();
    }

    /**
     * 清空所有笔画和历史
     */
    public void clear() {
        strokes.clear();
        undoableStrokes.clear();
        redoStack.clear();
    }
}
