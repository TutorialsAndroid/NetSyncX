package io.tutorialsandroid.netsyncx;

/**
 * Configuration for NetSyncX monitoring behavior.
 */
public final class NetSyncOptions {

    private final boolean requireValidatedInternet;
    private final boolean dispatchOnMainThread;
    private final boolean notifyInitialState;
    private final boolean debug;

    private NetSyncOptions(Builder builder) {
        this.requireValidatedInternet = builder.requireValidatedInternet;
        this.dispatchOnMainThread = builder.dispatchOnMainThread;
        this.notifyInitialState = builder.notifyInitialState;
        this.debug = builder.debug;
    }

    public static NetSyncOptions defaults() {
        return new Builder().build();
    }

    public boolean isRequireValidatedInternet() {
        return requireValidatedInternet;
    }

    public boolean isDispatchOnMainThread() {
        return dispatchOnMainThread;
    }

    public boolean isNotifyInitialState() {
        return notifyInitialState;
    }

    public boolean isDebug() {
        return debug;
    }

    public static final class Builder {
        private boolean requireValidatedInternet = true;
        private boolean dispatchOnMainThread = true;
        private boolean notifyInitialState = true;
        private boolean debug = false;

        /**
         * When true, NetSyncX only reports AVAILABLE if Android marks the network as validated.
         */
        public Builder requireValidatedInternet(boolean requireValidatedInternet) {
            this.requireValidatedInternet = requireValidatedInternet;
            return this;
        }

        /**
         * When true, callbacks are delivered on the main thread.
         */
        public Builder dispatchOnMainThread(boolean dispatchOnMainThread) {
            this.dispatchOnMainThread = dispatchOnMainThread;
            return this;
        }

        /**
         * When true, observer.start() immediately emits the current network state.
         */
        public Builder notifyInitialState(boolean notifyInitialState) {
            this.notifyInitialState = notifyInitialState;
            return this;
        }

        /**
         * Enables internal Android Logcat diagnostics.
         */
        public Builder debug(boolean debug) {
            this.debug = debug;
            return this;
        }

        public NetSyncOptions build() {
            return new NetSyncOptions(this);
        }
    }
}
