package rocketzly.componentinitializer.api;

import android.util.Log;

/**
 * Created by rocketzly on 2019/7/23.
 */
class Logger {

    private static boolean isDebug = false;
    private static String TAG = "ComponentInitializer";

    public static void setDebug(boolean isDebug) {
        Logger.isDebug = isDebug;
    }

    public static void d(String msg) {
        if (isDebug) {
            Log.d(TAG, msg);
        }
    }

    public static void w(String msg) {
        if (isDebug) {
            Log.w(TAG, msg);
        }
    }

    public static void e(String msg) {
        if (isDebug) {
            Log.e(TAG, msg);
        }
    }

    public static void v(String msg) {
        if (isDebug) {
            Log.v(TAG, msg);
        }
    }

    public static void i(String msg) {
        if (isDebug) {
            Log.i(TAG, msg);
        }
    }

}
