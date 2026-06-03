package io.tutorialsandroid.netsyncx;

/**
 * Retry behavior for NetSyncRetryQueue.
 */
public final class NetSyncRetryPolicy {

    private final int maxRetries;
    private final long retryDelayMillis;
    private final boolean exponentialBackoff;

    private NetSyncRetryPolicy(Builder builder) {
        this.maxRetries = builder.maxRetries;
        this.retryDelayMillis = builder.retryDelayMillis;
        this.exponentialBackoff = builder.exponentialBackoff;
    }

    public static NetSyncRetryPolicy defaults() {
        return new Builder().build();
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    public long getRetryDelayMillis() {
        return retryDelayMillis;
    }

    public boolean isExponentialBackoff() {
        return exponentialBackoff;
    }

    long delayForAttempt(int attempt) {
        if (!exponentialBackoff) {
            return retryDelayMillis;
        }
        long multiplier = 1L << Math.max(0, Math.min(attempt - 1, 10));
        return retryDelayMillis * multiplier;
    }

    public static final class Builder {
        private int maxRetries = 3;
        private long retryDelayMillis = 1000L;
        private boolean exponentialBackoff = true;

        public Builder maxRetries(int maxRetries) {
            this.maxRetries = Math.max(0, maxRetries);
            return this;
        }

        public Builder retryDelayMillis(long retryDelayMillis) {
            this.retryDelayMillis = Math.max(0L, retryDelayMillis);
            return this;
        }

        public Builder exponentialBackoff(boolean exponentialBackoff) {
            this.exponentialBackoff = exponentialBackoff;
            return this;
        }

        public NetSyncRetryPolicy build() {
            return new NetSyncRetryPolicy(this);
        }
    }
}
