package io.tutorialsandroid.netsyncx;

import android.util.Log;

final class NetSyncLog {
    private static final String TAG = "NetSyncX";

    private NetSyncLog() {
    }

    static void d(NetSyncOptions options, String message) {
        if (options != null && options.isDebug()) {
            Log.d(TAG, message);
        }
    }

    static void e(NetSyncOptions options, String message, Throwable throwable) {
        if (options != null && options.isDebug()) {
            Log.e(TAG, message, throwable);
        }
    }
}
