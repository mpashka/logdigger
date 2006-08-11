package com.iv.logView.ui;

public class FindResult {
    private final int pos;
    private final int length;

    public FindResult(int pos, int length) {
        this.pos = pos;
        this.length = length;
    }

    public int getPos() {
        return pos;
    }

    public int getLength() {
        return length;
    }
}
