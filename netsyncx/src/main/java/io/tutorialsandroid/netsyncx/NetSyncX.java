package io.tutorialsandroid.netsyncx;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;

/**
 * Main entry point for NetSyncX.
 */
public final class NetSyncX {

    private static volatile NetSyncObserver sharedObserver;

    private NetSyncX() {
    }

    /**
     * Returns a shared observer using default options.
     */
    public static NetSyncObserver with(Context context) {
        return with(context, NetSyncOptions.defaults());
    }

    /**
     * Returns a shared observer using custom options.
     */
    public static NetSyncObserver with(Context context, NetSyncOptions options) {
        if (context == null) {
            throw new IllegalArgumentException("Context cannot be null");
        }
        if (sharedObserver == null) {
            synchronized (NetSyncX.class) {
                if (sharedObserver == null) {
                    sharedObserver = new NetSyncObserver(context.getApplicationContext(), options);
                }
            }
        }
        sharedObserver.updateOptions(options);
        return sharedObserver;
    }

    /**
     * Stops and clears the shared observer. Useful for tests or manual cleanup.
     */
    public static synchronized void reset() {
        if (sharedObserver != null) {
            sharedObserver.stop();
            sharedObserver.clearListeners();
            sharedObserver = null;
        }
    }

    public static boolean isConnected(Context context) {
        return getState(context).isConnected();
    }

    public static boolean isValidated(Context context) {
        return getState(context).isValidated();
    }

    public static boolean isMetered(Context context) {
        return getState(context).isMetered();
    }

    public static NetSyncType getConnectionType(Context context) {
        return getState(context).getType();
    }

    public static NetSyncState getState(Context context) {
        return getState(context, NetSyncOptions.defaults());
    }

    static NetSyncState getState(Context context, NetSyncOptions options) {
        if (context == null) {
            return NetSyncState.unavailable("Context is null");
        }

        ConnectivityManager connectivityManager = null;
        try {
            connectivityManager = (ConnectivityManager) context.getApplicationContext()
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
        } catch (RuntimeException ignored) {
            // Handled below.
        }

        if (connectivityManager == null) {
            return NetSyncState.unavailable("ConnectivityManager unavailable");
        }

        try {
            Network activeNetwork = connectivityManager.getActiveNetwork();
            if (activeNetwork == null) {
                return NetSyncState.lost("No active network");
            }

            NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(activeNetwork);
            return NetSyncState.fromCapabilities(
                    capabilities,
                    NetSyncStatus.AVAILABLE,
                    0,
                    options == null ? NetSyncOptions.defaults() : options
            );
        } catch (SecurityException securityException) {
            return NetSyncState.unavailable("Missing ACCESS_NETWORK_STATE permission");
        } catch (RuntimeException runtimeException) {
            return NetSyncState.unavailable("Unable to read network state");
        }
    }
}
