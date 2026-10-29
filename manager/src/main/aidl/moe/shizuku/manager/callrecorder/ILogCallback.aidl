package moe.shizuku.manager.callrecorder;

interface ILogCallback {
    void onLog(int level, String tag, String message);
}
