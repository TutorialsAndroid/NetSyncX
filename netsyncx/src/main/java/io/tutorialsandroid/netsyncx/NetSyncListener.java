package io.tutorialsandroid.netsyncx;

/**
 * Listener for network state changes.
 *
 * Java and Kotlin users can override only the methods they need.
 */
public interface NetSyncListener {

    default void onStateChanged(NetSyncState state) {
        // Optional override.
    }

    default void onAvailable(NetSyncState state) {
        // Optional override.
    }

    default void onLost(NetSyncState state) {
        // Optional override.
    }

    default void onUnavailable(NetSyncState state) {
        // Optional override.
    }

    default void onLosing(NetSyncState state, int maxMsToLive) {
        // Optional override.
    }
}
