package io.tutorialsandroid.netsyncx;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Lightweight in-memory queue that runs jobs when internet is available.
 *
 * This is intentionally in-memory. For guaranteed persistent work across app restarts,
 * use AndroidX WorkManager in the application layer.
 */
public final class NetSyncRetryQueue {

    private static volatile NetSyncRetryQueue sharedQueue;

    private final Context appContext;
    private final ConcurrentLinkedQueue<Entry> queue = new ConcurrentLinkedQueue<>();
    private final Set<NetSyncJobCallback> callbacks = new CopyOnWriteArraySet<>();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final AtomicBoolean draining = new AtomicBoolean(false);

    private NetSyncRetryPolicy retryPolicy = NetSyncRetryPolicy.defaults();

    private final NetSyncListener autoFlushListener = new NetSyncListener() {
        @Override
        public void onAvailable(NetSyncState state) {
            flush();
        }
    };

    private NetSyncRetryQueue(Context context) {
        this.appContext = context.getApplicationContext();
        NetSyncX.with(appContext).addListener(autoFlushListener).start();
    }

    public static NetSyncRetryQueue with(Context context) {
        if (context == null) {
            throw new IllegalArgumentException("Context cannot be null");
        }
        if (sharedQueue == null) {
            synchronized (NetSyncRetryQueue.class) {
                if (sharedQueue == null) {
                    sharedQueue = new NetSyncRetryQueue(context.getApplicationContext());
                }
            }
        }
        return sharedQueue;
    }

    public NetSyncRetryQueue policy(NetSyncRetryPolicy retryPolicy) {
        this.retryPolicy = retryPolicy == null ? NetSyncRetryPolicy.defaults() : retryPolicy;
        return this;
    }

    public NetSyncRetryQueue addCallback(NetSyncJobCallback callback) {
        if (callback != null) {
            callbacks.add(callback);
        }
        return this;
    }

    public NetSyncRetryQueue removeCallback(NetSyncJobCallback callback) {
        if (callback != null) {
            callbacks.remove(callback);
        }
        return this;
    }

    public String enqueue(NetSyncJob job) {
        return enqueue(UUID.randomUUID().toString(), job);
    }

    public String enqueue(String id, NetSyncJob job) {
        if (job == null) {
            throw new IllegalArgumentException("NetSyncJob cannot be null");
        }
        String safeId = id == null || id.trim().isEmpty() ? UUID.randomUUID().toString() : id;
        queue.offer(new Entry(safeId, job));
        notifyQueued(safeId);
        flush();
        return safeId;
    }

    public int size() {
        return queue.size();
    }

    public void clear() {
        queue.clear();
    }

    public void flush() {
        if (!NetSyncX.isConnected(appContext)) {
            return;
        }
        if (!draining.compareAndSet(false, true)) {
            return;
        }

        executor.execute(() -> {
            try {
                while (NetSyncX.isConnected(appContext)) {
                    Entry entry = queue.poll();
                    if (entry == null) {
                        break;
                    }
                    runEntry(entry);
                }
            } finally {
                draining.set(false);
                if (!queue.isEmpty() && NetSyncX.isConnected(appContext)) {
                    flush();
                }
            }
        });
    }

    private void runEntry(Entry entry) {
        int attempt = entry.attempt + 1;
        entry.attempt = attempt;
        notifyStarted(entry.id, attempt);

        try {
            entry.job.execute();
            notifySuccess(entry.id);
        } catch (Throwable throwable) {
            if (attempt <= retryPolicy.getMaxRetries()) {
                long delay = retryPolicy.delayForAttempt(attempt);
                notifyRetry(entry.id, attempt + 1, throwable);
                mainHandler.postDelayed(() -> {
                    queue.offer(entry);
                    flush();
                }, delay);
            } else {
                notifyFailed(entry.id, throwable);
            }
        }
    }

    private void notifyQueued(String id) {
        post(() -> {
            for (NetSyncJobCallback callback : callbacks) {
                callback.onQueued(id);
            }
        });
    }

    private void notifyStarted(String id, int attempt) {
        post(() -> {
            for (NetSyncJobCallback callback : callbacks) {
                callback.onStarted(id, attempt);
            }
        });
    }

    private void notifySuccess(String id) {
        post(() -> {
            for (NetSyncJobCallback callback : callbacks) {
                callback.onSuccess(id);
            }
        });
    }

    private void notifyRetry(String id, int nextAttempt, Throwable error) {
        post(() -> {
            for (NetSyncJobCallback callback : callbacks) {
                callback.onRetry(id, nextAttempt, error);
            }
        });
    }

    private void notifyFailed(String id, Throwable error) {
        post(() -> {
            for (NetSyncJobCallback callback : callbacks) {
                callback.onFailed(id, error);
            }
        });
    }

    private void post(Runnable runnable) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            runnable.run();
        } else {
            mainHandler.post(runnable);
        }
    }

    private static final class Entry {
        private final String id;
        private final NetSyncJob job;
        private int attempt;

        private Entry(String id, NetSyncJob job) {
            this.id = id;
            this.job = job;
        }
    }
}
