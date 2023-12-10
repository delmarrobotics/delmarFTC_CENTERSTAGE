package common;

import android.util.Log;

/**
 *
*/

public final class Logger {
    private static final String TAG = "DELMAR";

    public static void error (Exception e, String msg) {
        Log.println(Log.ERROR, TAG, msg);
        Log.e(TAG, e.getMessage());
    }

    public static void message(String msg){
        Log.d(TAG, msg);
    }

    public static void message( String format, Object... args){
        Log.d(TAG, String.format(format, args));
    }

    public static void addLine(String msg) {
        Log.d(TAG, msg);
    }
}
