package moe.shizuku.privileged.api;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

public class ForwardReceiver extends BroadcastReceiver {

    public static final String OMNISHIZUKU_PACKAGE = "com.krishna.omnishizuku";
    public static final String SHEVERY_PACKAGE = "com.hamondev.shevery";
    public static final String SHEVERY_RECEIVER = "moe.shizuku.manager.receiver.ShizukuReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            String targetPkg = getTargetPackage(context);
            if (targetPkg == null) {
                return;
            }

            Intent forward = new Intent(intent);
            forward.setPackage(targetPkg);
            forward.setComponent(new ComponentName(targetPkg, SHEVERY_RECEIVER));

            context.sendBroadcast(forward);
        } catch (Throwable ignored) {
        }
    }

    private String getTargetPackage(Context context) {
        try {
            context.getPackageManager().getPackageInfo(OMNISHIZUKU_PACKAGE, 0);
            return OMNISHIZUKU_PACKAGE;
        } catch (PackageManager.NameNotFoundException ignored) {}
        try {
            context.getPackageManager().getPackageInfo(SHEVERY_PACKAGE, 0);
            return SHEVERY_PACKAGE;
        } catch (PackageManager.NameNotFoundException ignored) {
            return null;
        }
    }
}