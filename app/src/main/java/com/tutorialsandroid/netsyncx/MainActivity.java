package com.tutorialsandroid.netsyncx;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import io.tutorialsandroid.netsyncx.NetSyncJobCallback;
import io.tutorialsandroid.netsyncx.NetSyncListener;
import io.tutorialsandroid.netsyncx.NetSyncOptions;
import io.tutorialsandroid.netsyncx.NetSyncRetryPolicy;
import io.tutorialsandroid.netsyncx.NetSyncRetryQueue;
import io.tutorialsandroid.netsyncx.NetSyncState;
import io.tutorialsandroid.netsyncx.NetSyncX;


public class MainActivity extends Activity {

    private TextView statusView;
    private TextView detailsView;
    private TextView queueView;
    private NetSyncRetryQueue retryQueue;

    private final NetSyncListener listener = new NetSyncListener() {
        @Override
        public void onStateChanged(NetSyncState state) {
            renderState(state);
        }
    };

    private final NetSyncJobCallback jobCallback = new NetSyncJobCallback() {
        @Override
        public void onQueued(String id) {
            updateQueue("Queued: " + id);
        }

        @Override
        public void onStarted(String id, int attempt) {
            updateQueue("Started: " + id + " | attempt " + attempt);
        }

        @Override
        public void onSuccess(String id) {
            updateQueue("Success: " + id);
        }

        @Override
        public void onRetry(String id, int nextAttempt, Throwable error) {
            updateQueue("Retry: " + id + " | next attempt " + nextAttempt);
        }

        @Override
        public void onFailed(String id, Throwable error) {
            updateQueue("Failed: " + id + " | " + error.getMessage());
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(createContentView());

        NetSyncOptions options = new NetSyncOptions.Builder()
                .requireValidatedInternet(true)
                .dispatchOnMainThread(true)
                .notifyInitialState(true)
                .debug(true)
                .build();

        NetSyncX.with(this, options)
                .addListener(listener)
                .start();

        retryQueue = NetSyncRetryQueue.with(this)
                .policy(new NetSyncRetryPolicy.Builder()
                        .maxRetries(3)
                        .retryDelayMillis(1200)
                        .exponentialBackoff(true)
                        .build())
                .addCallback(jobCallback);
    }

    @Override
    protected void onDestroy() {
        NetSyncX.with(this).removeListener(listener);
        if (retryQueue != null) {
            retryQueue.removeCallback(jobCallback);
        }
        super.onDestroy();
    }

    private ScrollView createContentView() {
        ScrollView scrollView = new ScrollView(this);
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setPadding(dp(20), dp(32), dp(20), dp(32));
        scrollView.addView(root, new ScrollView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        TextView title = text("NetSyncX", 30, true);
        title.setTextColor(Color.rgb(30, 64, 175));
        root.addView(title);

        TextView subtitle = text("Smart Android network monitoring + offline retry", 15, false);
        subtitle.setGravity(Gravity.CENTER);
        subtitle.setTextColor(Color.rgb(75, 85, 99));
        root.addView(subtitle);

        statusView = text("Checking network...", 22, true);
        statusView.setGravity(Gravity.CENTER);
        statusView.setPadding(0, dp(28), 0, dp(8));
        root.addView(statusView);

        detailsView = text("", 14, false);
        detailsView.setTextColor(Color.rgb(55, 65, 81));
        detailsView.setPadding(dp(14), dp(14), dp(14), dp(14));
        root.addView(detailsView, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        Button checkButton = new Button(this);
        checkButton.setText("Check Current State");
        checkButton.setAllCaps(false);
        checkButton.setOnClickListener(view -> renderState(NetSyncX.getState(this)));
        root.addView(checkButton, buttonParams());

        Button queueButton = new Button(this);
        queueButton.setText("Queue Sample Offline Job");
        queueButton.setAllCaps(false);
        queueButton.setOnClickListener(view -> enqueueSampleJob());
        root.addView(queueButton, buttonParams());

        queueView = text("Queue events will appear here.", 14, false);
        queueView.setTextColor(Color.rgb(31, 41, 55));
        queueView.setPadding(dp(14), dp(18), dp(14), dp(14));
        root.addView(queueView, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        return scrollView;
    }

    private void enqueueSampleJob() {
        String id = "sample-job-" + System.currentTimeMillis();
        retryQueue.enqueue(id, () -> {
            Thread.sleep(600);
            // Replace this with your API call, CRM sync, Firebase sync, etc.
        });
        Toast.makeText(this, "Job queued", Toast.LENGTH_SHORT).show();
    }

    private void renderState(NetSyncState state) {
        statusView.setText(state.isConnected() ? "Online" : "Offline");
        statusView.setTextColor(state.isConnected() ? Color.rgb(22, 163, 74) : Color.rgb(220, 38, 38));

        detailsView.setText(
                "Status: " + state.getStatus() + "\n" +
                        "Type: " + state.getType() + "\n" +
                        "Validated: " + state.isValidated() + "\n" +
                        "Metered: " + state.isMetered() + "\n" +
                        "VPN: " + state.isVpn() + "\n" +
                        "Downstream: " + state.getDownstreamKbps() + " kbps\n" +
                        "Upstream: " + state.getUpstreamKbps() + " kbps\n" +
                        "Reason: " + state.getReason()
        );
    }

    private void updateQueue(String message) {
        queueView.setText(message + "\nPending jobs: " + retryQueue.size());
    }

    private TextView text(String value, int sp, boolean bold) {
        TextView textView = new TextView(this);
        textView.setText(value);
        textView.setTextSize(sp);
        textView.setGravity(Gravity.CENTER_VERTICAL);
        if (bold) {
            textView.setTypeface(Typeface.DEFAULT, Typeface.BOLD);
        }
        return textView;
    }

    private LinearLayout.LayoutParams buttonParams() {
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.topMargin = dp(12);
        return params;
    }

    private int dp(int value) {
        return (int) (value * getResources().getDisplayMetrics().density + 0.5f);
    }
}
