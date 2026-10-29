package moe.shizuku.manager.callrecorder;

import android.os.ParcelFileDescriptor;
import moe.shizuku.manager.callrecorder.ILogCallback;
import moe.shizuku.manager.callrecorder.IRecordingCallback;

interface IShellService {
    void startRecording(in ParcelFileDescriptor outputFile, int primarySource, int secondarySource, boolean channelSplit, int codec, int bitrate);
    void stopRecording();
    void pauseRecording();
    void resumeRecording();
    boolean isRecording();
    void setLogCallback(in ILogCallback callback, boolean redactionEnabled);
    void setRecordingCallback(in IRecordingCallback callback);
    void destroy();
}
