package moe.shizuku.manager.callrecorder;

interface IRecordingCallback {
    void onRecordingStarted();
    void onRecordingStopped();
    void onRecordingPaused();
    void onRecordingResumed();
    void onRecordingFailure(int errorCode, String message);
    void onRecordingProgress(long durationMs, long fileSizeBytes);
}
