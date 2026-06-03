package io.tutorialsandroid.netsyncx;

/**
 * A small unit of work that can be retried when internet is available.
 *
 * Keep jobs lightweight. For persistent background work across process death,
 * use WorkManager in your app layer.
 */
public interface NetSyncJob {
    void execute() throws Exception;
}
