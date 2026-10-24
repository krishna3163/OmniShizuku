package af.shizuku.common.util;

import android.os.Build;

public class BuildUtils {

    private static final int SDK = Build.VERSION.SDK_INT;

    public static boolean atLeast30() {
        return SDK >= 30;
    }

    public static boolean atLeast29() {
        return SDK >= 29;
    }

}
