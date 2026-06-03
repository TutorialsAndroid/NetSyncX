package io.tutorialsandroid.netsyncx;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.os.Handler;
import android.os.Looper;

import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Lifecycle-aware-ish network observer. Call start() when needed and stop() when finished.
 */
public final class NetSyncObserver {

    private final Context appContext;
    private final ConnectivityManager connectivityManager;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final Set<NetSyncListener> listeners = new CopyOnWriteArraySet<>();
    private final AtomicBoolean started = new AtomicBoolean(false);

    private NetSyncOptions options;
    private ConnectivityManager.NetworkCallback networkCallback;
    private volatile NetSyncState currentState;

    NetSyncObserver(Context context, NetSyncOptions options) {
        this.appContext = context.getApplicationContext();
        this.options = options == null ? NetSyncOptions.defaults() : options;
        this.connectivityManager = (ConnectivityManager) appContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        this.currentState = NetSyncX.getState(appContext, this.options);
    }

    public NetSyncObserver addListener(NetSyncListener listener) {
        if (listener != null) {
            listeners.add(listener);
        }
        return this;
    }

    public NetSyncObserver removeListener(NetSyncListener listener) {
        if (listener != null) {
            listeners.remove(listener);
        }
        return this;
    }

    public NetSyncObserver clearListeners() {
        listeners.clear();
        return this;
    }

    public synchronized NetSyncObserver updateOptions(NetSyncOptions options) {
        this.options = options == null ? NetSyncOptions.defaults() : options;
        return this;
    }

    public NetSyncState getCurrentState() {
        currentState = NetSyncX.getState(appContext, options);
        return currentState;
    }

    public boolean isStarted() {
        return started.get();
    }

    public synchronized NetSyncObserver start() {
        if (started.get()) {
            return this;
        }

        if (connectivityManager == null) {
            currentState = NetSyncState.unavailable("ConnectivityManager unavailable");
            dispatch(currentState, 0);
            return this;
        }

        networkCallback = new ConnectivityManager.NetworkCallback() {
            @Override
            public void onAvailable(Network network) {
                NetSyncLog.d(options, "onAvailable");
                emitFromNetwork(network, NetSyncStatus.AVAILABLE, 0);
            }

            @Override
            public void onCapabilitiesChanged(Network network, NetworkCapabilities networkCapabilities) {
                NetSyncLog.d(options, "onCapabilitiesChanged");
                NetSyncState state = NetSyncState.fromCapabilities(
                        networkCapabilities,
                        NetSyncStatus.AVAILABLE,
                        0,
                        options
                );
                dispatch(state, 0);
            }

            @Override
            public void onLost(Network network) {
                NetSyncLog.d(options, "onLost");
                dispatch(NetSyncState.lost("Network lost"), 0);
            }

            @Override
            public void onLosing(Network network, int maxMsToLive) {
                NetSyncLog.d(options, "onLosing");
                emitFromNetwork(network, NetSyncStatus.LOSING, maxMsToLive);
            }

            @Override
            public void onUnavailable() {
                NetSyncLog.d(options, "onUnavailable");
                dispatch(NetSyncState.unavailable("No matching network available"), 0);
            }
        };

        try {
            NetworkRequest request = new NetworkRequest.Builder()
                    .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    .build();
            connectivityManager.registerNetworkCallback(request, networkCallback);
            started.set(true);

            if (options.isNotifyInitialState()) {
                dispatch(NetSyncX.getState(appContext, options), 0);
            }
        } catch (SecurityException securityException) {
            currentState = NetSyncState.unavailable("Missing ACCESS_NETWORK_STATE permission");
            NetSyncLog.e(options, "Missing ACCESS_NETWORK_STATE permission", securityException);
            dispatch(currentState, 0);
        } catch (RuntimeException runtimeException) {
            currentState = NetSyncState.unavailable("Unable to register network callback");
            NetSyncLog.e(options, "Unable to register network callback", runtimeException);
            dispatch(currentState, 0);
        }

        return this;
    }

    public synchronized void stop() {
        if (!started.get()) {
            return;
        }

        if (connectivityManager != null && networkCallback != null) {
            try {
                connectivityManager.unregisterNetworkCallback(networkCallback);
            } catch (RuntimeException ignored) {
                // Safe no-op. Android throws if callback is already unregistered.
            }
        }

        networkCallback = null;
        started.set(false);
    }

    private void emitFromNetwork(Network network, NetSyncStatus status, int maxMsToLive) {
        NetworkCapabilities capabilities = null;
        try {
            capabilities = connectivityManager == null ? null : connectivityManager.getNetworkCapabilities(network);
        } catch (RuntimeException ignored) {
            // Use unavailable state below.
        }
        dispatch(NetSyncState.fromCapabilities(capabilities, status, maxMsToLive, options), maxMsToLive);
    }

    private void dispatch(NetSyncState state, int maxMsToLive) {
        currentState = state;
        Runnable task = () -> {
            for (NetSyncListener listener : listeners) {
                listener.onStateChanged(state);

                if (state.getStatus() == NetSyncStatus.AVAILABLE) {
                    listener.onAvailable(state);
                } else if (state.getStatus() == NetSyncStatus.LOST) {
                    listener.onLost(state);
                } else if (state.getStatus() == NetSyncStatus.UNAVAILABLE) {
                    listener.onUnavailable(state);
                } else if (state.getStatus() == NetSyncStatus.LOSING) {
                    listener.onLosing(state, maxMsToLive);
                }
            }
        };

        if (options.isDispatchOnMainThread() && Looper.myLooper() != Looper.getMainLooper()) {
            mainHandler.post(task);
        } else {
            task.run();
        }
    }
}
