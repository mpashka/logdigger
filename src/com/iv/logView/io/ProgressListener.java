package com.iv.logView.io;

public interface ProgressListener {

    void onBegin();

    void onEnd();

    void onProgress(int percent);

}
