<div align="center">

# NetSyncX

### Smart Android Network Monitoring + Offline Retry for Java & Kotlin Apps

NetSyncX is a lightweight, production-ready Android library that helps developers monitor network state, detect connection quality, identify transport type, and retry lightweight offline jobs automatically when internet is available again.

Built for modern Android apps that need reliable network-aware behavior without writing repetitive `ConnectivityManager` code again and again.

<br/>

<a href="https://central.sonatype.com/artifact/io.github.tutorialsandroid/netsyncx" target="_blank">
  <img src="https://img.shields.io/maven-central/v/io.github.tutorialsandroid/netsyncx?style=for-the-badge&label=Maven%20Central&color=2563EB" alt="Maven Central" />
</a>
<a href="https://github.com/TutorialsAndroid/NetSyncX" target="_blank">
  <img src="https://img.shields.io/github/stars/TutorialsAndroid/NetSyncX?style=for-the-badge&logo=github" alt="GitHub Stars" />
</a>
<a href="https://github.com/TutorialsAndroid/NetSyncX/blob/main/LICENSE" target="_blank">
  <img src="https://img.shields.io/github/license/TutorialsAndroid/NetSyncX?style=for-the-badge" alt="License" />
</a>
<img src="https://img.shields.io/badge/Java-Compatible-F97316?style=for-the-badge&logo=openjdk&logoColor=white" alt="Java Compatible" />
<img src="https://img.shields.io/badge/Kotlin-Compatible-7C3AED?style=for-the-badge&logo=kotlin&logoColor=white" alt="Kotlin Compatible" />
<img src="https://img.shields.io/badge/Android-minSdk%2024-22C55E?style=for-the-badge&logo=android&logoColor=white" alt="Android minSdk 24" />

<br/><br/>

<img src="https://raw.githubusercontent.com/TutorialsAndroid/NetSyncX/main/assets/netsyncx-banner.png" alt="NetSyncX Banner" width="100%" />

</div>

---

## Table of Contents

* [Overview](#overview)
* [Why NetSyncX?](#why-netsyncx)
* [Features](#features)
* [Installation](#installation)
* [Required Permissions](#required-permissions)
* [Quick Start](#quick-start)
* [Java Usage](#java-usage)
* [Kotlin Usage](#kotlin-usage)
* [Check Current Network State](#check-current-network-state)
* [Offline Retry Queue](#offline-retry-queue)
* [Retry Policy](#retry-policy)
* [Callbacks](#callbacks)
* [API Reference](#api-reference)
* [Screenshots](#screenshots)
* [Best Practices](#best-practices)
* [Troubleshooting](#troubleshooting)
* [ProGuard / R8](#proguard--r8)
* [Sample App](#sample-app)
* [Use Cases](#use-cases)
* [Roadmap](#roadmap)
* [Contributing](#contributing)
* [License](#license)

---

## Overview

**NetSyncX** is an Android Java library, fully usable in Kotlin, designed to make network-aware app development simple and professional.

It helps you answer important questions inside your Android app:

* Is the device connected to the internet?
* Is the current network validated by Android?
* Is the user connected through Wi-Fi, mobile data, Ethernet, VPN, or Bluetooth?
* Is the connection metered?
* Is the network losing or lost?
* Can lightweight failed tasks be retried when internet comes back?

Instead of writing repeated network-checking code in every app, NetSyncX gives you a clean and reusable API.

---

## Why NetSyncX?

Many Android apps depend on internet connectivity for:

* Login
* Firebase sync
* API requests
* CRM lead submission
* Form upload
* Chat messages
* Analytics
* Payment flows
* Booking requests
* Background sync
* Image or file upload

When internet drops, apps often fail silently or show a poor user experience.

NetSyncX helps you build apps that react professionally to network changes and retry lightweight offline work automatically.

---

## Features

### Network Monitoring

* Real-time network state monitoring
* Initial network state callback
* Online / offline detection
* Losing / lost / unavailable state handling
* Validated internet detection
* Metered network detection
* Roaming detection
* VPN detection
* Estimated upstream and downstream bandwidth
* Human-readable network state reason

### Connection Type Detection

NetSyncX can detect:

* Wi-Fi
* Cellular / mobile data
* Ethernet
* VPN
* Bluetooth
* Unknown transport

### Offline Retry Queue

* Lightweight in-memory job queue
* Runs queued jobs when internet becomes available
* Custom job IDs
* Retry callbacks
* Configurable max retries
* Configurable retry delay
* Optional exponential backoff
* Main-thread callback delivery

### Developer Friendly

* Java-first API
* Kotlin compatible
* Builder-style configuration
* No UI dependency
* Lightweight implementation
* Easy integration
* Clean callback system
* Maven Central available

---

## Installation

NetSyncX is available on **Maven Central**.

### Gradle Kotlin DSL

Add Maven Central in your project-level `settings.gradle.kts`:

```kotlin
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}
```

Then add NetSyncX in your app module `build.gradle.kts`:

```kotlin
dependencies {
    implementation("io.github.tutorialsandroid:netsyncx:1.0.0")
}
```

### Gradle Groovy DSL

Add Maven Central in your project-level `settings.gradle`:

```gradle
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}
```

Then add NetSyncX in your app module `build.gradle`:

```gradle
dependencies {
    implementation 'io.github.tutorialsandroid:netsyncx:1.0.0'
}
```

---

## Required Permissions

Add these permissions in your app `AndroidManifest.xml`:

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.INTERNET" />

    <application>
        ...
    </application>

</manifest>
```

### Permission Notes

`ACCESS_NETWORK_STATE` is required to read network status, connection type, validation status, metered state, VPN state, and other network capabilities.

`INTERNET` is required if your app performs API calls, Firebase sync, CRM sync, uploads, downloads, or any internet-based operation.

These are normal Android permissions. They do not require runtime permission dialogs.

---

## Quick Start

```java
NetSyncX.with(this)
        .addListener(new NetSyncListener() {
            @Override
            public void onStateChanged(NetSyncState state) {
                if (state.isConnected()) {
                    Log.d("NetSyncX", "Online: " + state.getType());
                } else {
                    Log.d("NetSyncX", "Offline: " + state.getReason());
                }
            }
        })
        .start();
```

---

## Java Usage

### Start Observing Network Changes

```java
import io.tutorialsandroid.netsyncx.NetSyncListener;
import io.tutorialsandroid.netsyncx.NetSyncOptions;
import io.tutorialsandroid.netsyncx.NetSyncState;
import io.tutorialsandroid.netsyncx.NetSyncX;

public class MainActivity extends AppCompatActivity {

    private final NetSyncListener netSyncListener = new NetSyncListener() {
        @Override
        public void onStateChanged(NetSyncState state) {
            Log.d("NetSyncX", "State: " + state);
        }

        @Override
        public void onAvailable(NetSyncState state) {
            Log.d("NetSyncX", "Internet available: " + state.getType());
        }

        @Override
        public void onLost(NetSyncState state) {
            Log.d("NetSyncX", "Internet lost");
        }

        @Override
        public void onUnavailable(NetSyncState state) {
            Log.d("NetSyncX", "Internet unavailable: " + state.getReason());
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        NetSyncOptions options = new NetSyncOptions.Builder()
                .requireValidatedInternet(true)
                .dispatchOnMainThread(true)
                .notifyInitialState(true)
                .debug(false)
                .build();

        NetSyncX.with(this, options)
                .addListener(netSyncListener)
                .start();
    }

    @Override
    protected void onDestroy() {
        NetSyncX.with(this).removeListener(netSyncListener);
        super.onDestroy();
    }
}
```

---

## Kotlin Usage

```kotlin
import io.tutorialsandroid.netsyncx.NetSyncListener
import io.tutorialsandroid.netsyncx.NetSyncOptions
import io.tutorialsandroid.netsyncx.NetSyncState
import io.tutorialsandroid.netsyncx.NetSyncX

class MainActivity : AppCompatActivity() {

    private val netSyncListener = object : NetSyncListener {
        override fun onStateChanged(state: NetSyncState) {
            Log.d("NetSyncX", "State: $state")
        }

        override fun onAvailable(state: NetSyncState) {
            Log.d("NetSyncX", "Internet available: ${state.type}")
        }

        override fun onLost(state: NetSyncState) {
            Log.d("NetSyncX", "Internet lost")
        }

        override fun onUnavailable(state: NetSyncState) {
            Log.d("NetSyncX", "Internet unavailable: ${state.reason}")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val options = NetSyncOptions.Builder()
            .requireValidatedInternet(true)
            .dispatchOnMainThread(true)
            .notifyInitialState(true)
            .debug(false)
            .build()

        NetSyncX.with(this, options)
            .addListener(netSyncListener)
            .start()
    }

    override fun onDestroy() {
        NetSyncX.with(this).removeListener(netSyncListener)
        super.onDestroy()
    }
}
```

---

## Check Current Network State

You can check the current network state at any time.

### Java

```java
NetSyncState state = NetSyncX.getState(this);

boolean connected = state.isConnected();
boolean validated = state.isValidated();
boolean metered = state.isMetered();
boolean vpn = state.isVpn();

Log.d("NetSyncX", "Connected: " + connected);
Log.d("NetSyncX", "Type: " + state.getType());
Log.d("NetSyncX", "Reason: " + state.getReason());
```

### Kotlin

```kotlin
val state = NetSyncX.getState(this)

val connected = state.isConnected
val validated = state.isValidated
val metered = state.isMetered
val vpn = state.isVpn

Log.d("NetSyncX", "Connected: $connected")
Log.d("NetSyncX", "Type: ${state.type}")
Log.d("NetSyncX", "Reason: ${state.reason}")
```

---

## Helper Methods

NetSyncX also provides direct helper methods:

### Java

```java
boolean connected = NetSyncX.isConnected(context);
boolean validated = NetSyncX.isValidated(context);
boolean metered = NetSyncX.isMetered(context);

NetSyncType type = NetSyncX.getConnectionType(context);
NetSyncState state = NetSyncX.getState(context);
```

### Kotlin

```kotlin
val connected = NetSyncX.isConnected(context)
val validated = NetSyncX.isValidated(context)
val metered = NetSyncX.isMetered(context)

val type = NetSyncX.getConnectionType(context)
val state = NetSyncX.getState(context)
```

---

## Offline Retry Queue

NetSyncX includes a lightweight in-memory retry queue for small tasks that should run when internet is available.

This is useful for:

* Retrying a failed form submission
* Retrying a CRM lead push
* Retrying Firebase sync
* Retrying analytics event upload
* Retrying small API calls
* Retrying local pending operations

### Java Example

```java
NetSyncRetryQueue retryQueue = NetSyncRetryQueue.with(this)
        .policy(new NetSyncRetryPolicy.Builder()
                .maxRetries(3)
                .retryDelayMillis(1500)
                .exponentialBackoff(true)
                .build())
        .addCallback(new NetSyncJobCallback() {
            @Override
            public void onQueued(String id) {
                Log.d("NetSyncX", "Queued: " + id);
            }

            @Override
            public void onStarted(String id, int attempt) {
                Log.d("NetSyncX", "Started: " + id + " | attempt " + attempt);
            }

            @Override
            public void onSuccess(String id) {
                Log.d("NetSyncX", "Success: " + id);
            }

            @Override
            public void onRetry(String id, int nextAttempt, Throwable error) {
                Log.d("NetSyncX", "Retry: " + id + " | next attempt " + nextAttempt);
            }

            @Override
            public void onFailed(String id, Throwable error) {
                Log.e("NetSyncX", "Failed: " + id, error);
            }
        });

retryQueue.enqueue("sync-lead", new NetSyncJob() {
    @Override
    public void execute() throws Exception {
        // Add your API call, Firebase sync, CRM sync, or upload logic here.
        // Throw an exception if the task fails and should be retried.
    }
});
```

### Java Lambda Example

```java
NetSyncRetryQueue.with(this).enqueue("sync-user-profile", () -> {
    // Run your lightweight sync task here.
});
```

### Kotlin Example

```kotlin
val retryQueue = NetSyncRetryQueue.with(this)
    .policy(
        NetSyncRetryPolicy.Builder()
            .maxRetries(3)
            .retryDelayMillis(1500)
            .exponentialBackoff(true)
            .build()
    )
    .addCallback(object : NetSyncJobCallback {
        override fun onQueued(id: String) {
            Log.d("NetSyncX", "Queued: $id")
        }

        override fun onStarted(id: String, attempt: Int) {
            Log.d("NetSyncX", "Started: $id | attempt $attempt")
        }

        override fun onSuccess(id: String) {
            Log.d("NetSyncX", "Success: $id")
        }

        override fun onRetry(id: String, nextAttempt: Int, error: Throwable) {
            Log.d("NetSyncX", "Retry: $id | next attempt $nextAttempt")
        }

        override fun onFailed(id: String, error: Throwable) {
            Log.e("NetSyncX", "Failed: $id", error)
        }
    })

retryQueue.enqueue("sync-lead") {
    // Add your API call, Firebase sync, CRM sync, or upload logic here.
    // Throw an exception if the task fails and should be retried.
}
```

---

## Retry Policy

Customize retry behavior using `NetSyncRetryPolicy`.

```java
NetSyncRetryPolicy policy = new NetSyncRetryPolicy.Builder()
        .maxRetries(5)
        .retryDelayMillis(2000)
        .exponentialBackoff(true)
        .build();

NetSyncRetryQueue.with(context)
        .policy(policy);
```

### Retry Policy Options

| Option                        | Description                                     | Default |
| ----------------------------- | ----------------------------------------------- | ------- |
| `maxRetries(int)`             | Maximum retry attempts after failure            | `3`     |
| `retryDelayMillis(long)`      | Delay before retrying                           | `1000`  |
| `exponentialBackoff(boolean)` | Increases retry delay after each failed attempt | `true`  |

---

## Callbacks

### NetSyncListener

Use `NetSyncListener` to listen to network state changes.

```java
new NetSyncListener() {
    @Override
    public void onStateChanged(NetSyncState state) {
        // Called for every state update.
    }

    @Override
    public void onAvailable(NetSyncState state) {
        // Called when internet becomes available.
    }

    @Override
    public void onLost(NetSyncState state) {
        // Called when network is lost.
    }

    @Override
    public void onUnavailable(NetSyncState state) {
        // Called when network is unavailable.
    }

    @Override
    public void onLosing(NetSyncState state, int maxMsToLive) {
        // Called when Android reports the network is about to be lost.
    }
};
```

### NetSyncJobCallback

Use `NetSyncJobCallback` to listen to retry queue events.

```java
new NetSyncJobCallback() {
    @Override
    public void onQueued(String id) {
        // Job added to queue.
    }

    @Override
    public void onStarted(String id, int attempt) {
        // Job execution started.
    }

    @Override
    public void onSuccess(String id) {
        // Job completed successfully.
    }

    @Override
    public void onRetry(String id, int nextAttempt, Throwable error) {
        // Job failed and will retry.
    }

    @Override
    public void onFailed(String id, Throwable error) {
        // Job failed permanently after max retries.
    }
};
```

---

## API Reference

### NetSyncX

Main entry point of the library.

| Method                          | Description                                     |
| ------------------------------- | ----------------------------------------------- |
| `with(Context)`                 | Returns shared observer with default options    |
| `with(Context, NetSyncOptions)` | Returns shared observer with custom options     |
| `isConnected(Context)`          | Returns whether internet is currently available |
| `isValidated(Context)`          | Returns whether Android validates the network   |
| `isMetered(Context)`            | Returns whether the current network is metered  |
| `getConnectionType(Context)`    | Returns current connection type                 |
| `getState(Context)`             | Returns full current network state              |
| `reset()`                       | Stops and clears the shared observer            |

---

### NetSyncObserver

Real-time network observer.

| Method                            | Description                        |
| --------------------------------- | ---------------------------------- |
| `addListener(NetSyncListener)`    | Adds a network listener            |
| `removeListener(NetSyncListener)` | Removes a network listener         |
| `clearListeners()`                | Removes all listeners              |
| `start()`                         | Starts monitoring network changes  |
| `stop()`                          | Stops monitoring network changes   |
| `getCurrentState()`               | Returns latest known state         |
| `isStarted()`                     | Returns whether observer is active |
| `updateOptions(NetSyncOptions)`   | Updates observer options           |

---

### NetSyncOptions

Configuration for network monitoring.

```java
NetSyncOptions options = new NetSyncOptions.Builder()
        .requireValidatedInternet(true)
        .dispatchOnMainThread(true)
        .notifyInitialState(true)
        .debug(false)
        .build();
```

| Option                              | Description                                            | Default |
| ----------------------------------- | ------------------------------------------------------ | ------- |
| `requireValidatedInternet(boolean)` | Reports available only when Android validates internet | `true`  |
| `dispatchOnMainThread(boolean)`     | Delivers callbacks on main thread                      | `true`  |
| `notifyInitialState(boolean)`       | Emits current state immediately after start            | `true`  |
| `debug(boolean)`                    | Enables internal debug logs                            | `false` |

---

### NetSyncState

Immutable snapshot of the current network state.

| Method                 | Description                                             |
| ---------------------- | ------------------------------------------------------- |
| `getStatus()`          | Returns `AVAILABLE`, `UNAVAILABLE`, `LOSING`, or `LOST` |
| `getType()`            | Returns current network transport type                  |
| `isConnected()`        | Returns whether internet is available                   |
| `isValidated()`        | Returns whether network is validated                    |
| `isMetered()`          | Returns whether network is metered                      |
| `isRoaming()`          | Returns whether network is roaming                      |
| `isVpn()`              | Returns whether VPN is active                           |
| `getDownstreamKbps()`  | Returns estimated downstream bandwidth                  |
| `getUpstreamKbps()`    | Returns estimated upstream bandwidth                    |
| `getTimestampMillis()` | Returns state timestamp                                 |
| `getReason()`          | Returns human-readable reason                           |

---

### NetSyncStatus

```java
AVAILABLE
UNAVAILABLE
LOSING
LOST
```

---

### NetSyncType

```java
WIFI
CELLULAR
ETHERNET
VPN
BLUETOOTH
UNKNOWN
```

---

### NetSyncRetryQueue

Lightweight in-memory queue for retrying jobs when internet is available.

| Method                               | Description                                   |
| ------------------------------------ | --------------------------------------------- |
| `with(Context)`                      | Returns shared retry queue                    |
| `policy(NetSyncRetryPolicy)`         | Applies retry policy                          |
| `addCallback(NetSyncJobCallback)`    | Adds queue callback                           |
| `removeCallback(NetSyncJobCallback)` | Removes queue callback                        |
| `enqueue(NetSyncJob)`                | Adds a job with auto-generated ID             |
| `enqueue(String, NetSyncJob)`        | Adds a job with custom ID                     |
| `size()`                             | Returns pending job count                     |
| `clear()`                            | Clears pending jobs                           |
| `flush()`                            | Attempts to run jobs if internet is available |

---

### NetSyncRetryPolicy

Retry configuration for `NetSyncRetryQueue`.

| Method                        | Description                             |
| ----------------------------- | --------------------------------------- |
| `maxRetries(int)`             | Sets max retry count                    |
| `retryDelayMillis(long)`      | Sets retry delay                        |
| `exponentialBackoff(boolean)` | Enables or disables exponential backoff |

---

## Screenshots

Add your screenshots inside:

```text
assets/screenshots/
```

Then update this section:

| Online State                                            | Offline State                                            | Retry Queue                                                  |
| ------------------------------------------------------- | -------------------------------------------------------- | ------------------------------------------------------------ |
| <img src="assets/screenshots/online.png" width="250" /> | <img src="assets/screenshots/offline.png" width="250" /> | <img src="assets/screenshots/retry-queue.png" width="250" /> |

---

## Best Practices

### Remove Listeners

Always remove listeners when your Activity or Fragment is destroyed.

```java
@Override
protected void onDestroy() {
    NetSyncX.with(this).removeListener(netSyncListener);
    super.onDestroy();
}
```

### Keep Retry Jobs Lightweight

The retry queue is designed for lightweight tasks.

Good examples:

* Small API call
* Lead form sync
* CRM push
* Firebase status update
* Analytics event sync

Avoid using it for:

* Large uploads
* Long-running background work
* Guaranteed sync after app restart
* Heavy database operations

For persistent background work across app restarts or process death, use WorkManager in your application layer.

### Use Validated Internet for Real API Flows

For API-based apps, keep this enabled:

```java
.requireValidatedInternet(true)
```

This helps avoid treating captive portals or limited networks as fully usable internet.

### Disable Debug Logs in Release

```java
.debug(false)
```

---

## Troubleshooting

### App Always Shows Offline

Make sure this permission exists in your app manifest:

```xml
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

Also make sure the permission is outside the `<application>` tag.

---

### Reason Shows Missing ACCESS_NETWORK_STATE Permission

Your manifest is missing:

```xml
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

Correct structure:

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android">

    <uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
    <uses-permission android:name="android.permission.INTERNET" />

    <application>
        ...
    </application>

</manifest>
```

---

### Network Exists but Is Not Validated

This means Android can see a network, but it does not consider the connection to have validated internet access.

Common reasons:

* Captive portal
* No actual internet access
* DNS problem
* Restricted Wi-Fi
* Emulator network issue
* VPN or proxy problem

You can relax this behavior:

```java
NetSyncOptions options = new NetSyncOptions.Builder()
        .requireValidatedInternet(false)
        .build();
```

---

### Retry Queue Does Not Run

Check these points:

1. Internet must be available.
2. `NetSyncX.isConnected(context)` must return `true`.
3. The job must not be blocking forever.
4. The job must throw an exception if it fails.
5. The queue is in-memory, so pending jobs are not restored after app restart.

---

## ProGuard / R8

NetSyncX does not require special ProGuard rules for normal usage.

If your app uses aggressive shrinking and reflection around callbacks, you can add:

```proguard
-keep class io.tutorialsandroid.netsyncx.** { *; }
```

---

## Sample App

The repository includes a sample app showing:

* Current network status
* Network type
* Validated state
* Metered state
* VPN state
* Upstream and downstream bandwidth
* Offline retry queue demo
* Queue callbacks
* Manual current state check

Sample job example:

```java
retryQueue.enqueue("sample-job", () -> {
    Thread.sleep(600);
    // Replace this with your API call, CRM sync, Firebase sync, etc.
});
```

---

## Use Cases

NetSyncX is useful for:

* Android apps with API calls
* Firebase apps
* CRM lead capture apps
* Real estate inquiry apps
* Chat apps
* Booking apps
* Payment flow apps
* Admin dashboards
* Offline-first utilities
* Background sync preparation
* Apps that need professional network state handling

---

## What NetSyncX Is Not

NetSyncX is not:

* A full API client
* A Retrofit replacement
* A persistent background task manager
* A speed test library
* A download manager
* A large file uploader
* A database sync engine

It is a lightweight network monitoring and in-memory retry toolkit.

---

## Roadmap

### v1.x

* Core network monitoring
* Connection type detection
* Validated internet detection
* Metered network detection
* In-memory retry queue

### Planned

* Lifecycle-aware extensions
* Flow support for Kotlin users
* LiveData support
* Persistent retry queue module
* WorkManager integration module
* Network quality scoring
* Demo app improvements
* More sample recipes

---

## Contributing

Contributions are welcome.

You can contribute by:

* Reporting bugs
* Suggesting features
* Improving documentation
* Adding sample use cases
* Opening pull requests

Before opening a pull request, please make sure:

* Code builds successfully
* API remains Java and Kotlin friendly
* Public methods are documented
* Changes are useful for real Android apps
* README examples stay updated

---

## Author

Developed and maintained by **TutorialsAndroid**.

GitHub: [TutorialsAndroid](https://github.com/TutorialsAndroid)

---

## License

```text
MIT License
```

---

<div align="center">

## NetSyncX

### Build Android apps that understand network state professionally.

If this library helps you, consider giving the repository a star.

<a href="https://github.com/TutorialsAndroid/NetSyncX" target="_blank">
  <img src="https://img.shields.io/badge/Star%20on%20GitHub-181717?style=for-the-badge&logo=github" alt="Star on GitHub" />
</a>
<a href="https://central.sonatype.com/artifact/io.github.tutorialsandroid/netsyncx" target="_blank">
  <img src="https://img.shields.io/badge/Available%20on-Maven%20Central-2563EB?style=for-the-badge" alt="Available on Maven Central" />
</a>

</div>
