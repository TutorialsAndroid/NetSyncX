package io.tutorialsandroid.netsyncx;

/**
 * Optional callbacks for in-memory retry queue events.
 */
public interface NetSyncJobCallback {

    default void onQueued(String id) {
        // Optional override.
    }

    default void onStarted(String id, int attempt) {
        // Optional override.
    }

    default void onSuccess(String id) {
        // Optional override.
    }

    default void onRetry(String id, int nextAttempt, Throwable error) {
        // Optional override.
    }

    default void onFailed(String id, Throwable error) {
        // Optional override.
    }
}
