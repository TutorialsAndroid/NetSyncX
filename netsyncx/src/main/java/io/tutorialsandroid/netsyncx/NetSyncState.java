package io.tutorialsandroid.netsyncx;

import android.net.NetworkCapabilities;
import android.os.Build;

import java.util.Locale;
import java.util.Objects;

/**
 * Immutable snapshot of the current network state.
 */
public final class NetSyncState {

    private final NetSyncStatus status;
    private final NetSyncType type;
    private final boolean connected;
    private final boolean validated;
    private final boolean metered;
    private final boolean roaming;
    private final boolean vpn;
    private final int downstreamKbps;
    private final int upstreamKbps;
    private final long timestampMillis;
    private final String reason;

    private NetSyncState(Builder builder) {
        this.status = builder.status;
        this.type = builder.type;
        this.connected = builder.connected;
        this.validated = builder.validated;
        this.metered = builder.metered;
        this.roaming = builder.roaming;
        this.vpn = builder.vpn;
        this.downstreamKbps = builder.downstreamKbps;
        this.upstreamKbps = builder.upstreamKbps;
        this.timestampMillis = builder.timestampMillis;
        this.reason = builder.reason;
    }

    public static NetSyncState unavailable(String reason) {
        return new Builder()
                .status(NetSyncStatus.UNAVAILABLE)
                .type(NetSyncType.UNKNOWN)
                .connected(false)
                .validated(false)
                .metered(false)
                .roaming(false)
                .vpn(false)
                .reason(reason)
                .build();
    }

    public static NetSyncState lost(String reason) {
        return new Builder()
                .status(NetSyncStatus.LOST)
                .type(NetSyncType.UNKNOWN)
                .connected(false)
                .validated(false)
                .metered(false)
                .roaming(false)
                .vpn(false)
                .reason(reason)
                .build();
    }

    static NetSyncState fromCapabilities(
            NetworkCapabilities capabilities,
            NetSyncStatus forcedStatus,
            int maxMsToLive,
            NetSyncOptions options
    ) {
        if (capabilities == null) {
            if (forcedStatus == NetSyncStatus.LOST) {
                return lost("Network lost");
            }
            return unavailable("Network capabilities unavailable");
        }

        boolean hasInternet = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET);
        boolean validated = capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
        boolean connected = hasInternet && (!options.isRequireValidatedInternet() || validated);
        boolean metered = !capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED);
        boolean vpn = capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN);
        boolean roaming = false;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            roaming = !capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_ROAMING);
        }

        NetSyncStatus status;
        String reason;

        if (forcedStatus == NetSyncStatus.LOSING) {
            status = NetSyncStatus.LOSING;
            reason = "Network is losing in approximately " + maxMsToLive + " ms";
        } else if (forcedStatus == NetSyncStatus.LOST) {
            status = NetSyncStatus.LOST;
            reason = "Network lost";
            connected = false;
        } else if (connected) {
            status = NetSyncStatus.AVAILABLE;
            reason = "Validated internet connection available";
        } else {
            status = NetSyncStatus.UNAVAILABLE;
            if (!hasInternet) {
                reason = "Network does not provide internet capability";
            } else if (options.isRequireValidatedInternet() && !validated) {
                reason = "Network exists but is not validated by Android";
            } else {
                reason = "Network unavailable";
            }
        }

        return new Builder()
                .status(status)
                .type(resolveType(capabilities))
                .connected(connected)
                .validated(validated)
                .metered(metered)
                .roaming(roaming)
                .vpn(vpn)
                .downstreamKbps(capabilities.getLinkDownstreamBandwidthKbps())
                .upstreamKbps(capabilities.getLinkUpstreamBandwidthKbps())
                .reason(reason)
                .build();
    }

    private static NetSyncType resolveType(NetworkCapabilities capabilities) {
        if (capabilities == null) {
            return NetSyncType.UNKNOWN;
        }
        if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN)) {
            return NetSyncType.VPN;
        }
        if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
            return NetSyncType.WIFI;
        }
        if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
            return NetSyncType.CELLULAR;
        }
        if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
            return NetSyncType.ETHERNET;
        }
        if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH)) {
            return NetSyncType.BLUETOOTH;
        }
        return NetSyncType.UNKNOWN;
    }

    public NetSyncStatus getStatus() {
        return status;
    }

    public NetSyncType getType() {
        return type;
    }

    public boolean isConnected() {
        return connected;
    }

    public boolean isValidated() {
        return validated;
    }

    public boolean isMetered() {
        return metered;
    }

    public boolean isRoaming() {
        return roaming;
    }

    public boolean isVpn() {
        return vpn;
    }

    public int getDownstreamKbps() {
        return downstreamKbps;
    }

    public int getUpstreamKbps() {
        return upstreamKbps;
    }

    public long getTimestampMillis() {
        return timestampMillis;
    }

    public String getReason() {
        return reason;
    }

    @Override
    public String toString() {
        return String.format(
                Locale.US,
                "NetSyncState{status=%s, type=%s, connected=%s, validated=%s, metered=%s, vpn=%s, down=%dkbps, up=%dkbps, reason='%s'}",
                status,
                type,
                connected,
                validated,
                metered,
                vpn,
                downstreamKbps,
                upstreamKbps,
                reason
        );
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof NetSyncState)) return false;
        NetSyncState that = (NetSyncState) object;
        return connected == that.connected
                && validated == that.validated
                && metered == that.metered
                && roaming == that.roaming
                && vpn == that.vpn
                && downstreamKbps == that.downstreamKbps
                && upstreamKbps == that.upstreamKbps
                && status == that.status
                && type == that.type
                && Objects.equals(reason, that.reason);
    }

    @Override
    public int hashCode() {
        return Objects.hash(status, type, connected, validated, metered, roaming, vpn, downstreamKbps, upstreamKbps, reason);
    }

    public static final class Builder {
        private NetSyncStatus status = NetSyncStatus.UNAVAILABLE;
        private NetSyncType type = NetSyncType.UNKNOWN;
        private boolean connected;
        private boolean validated;
        private boolean metered;
        private boolean roaming;
        private boolean vpn;
        private int downstreamKbps;
        private int upstreamKbps;
        private long timestampMillis = System.currentTimeMillis();
        private String reason = "Unknown";

        public Builder status(NetSyncStatus status) {
            this.status = status == null ? NetSyncStatus.UNAVAILABLE : status;
            return this;
        }

        public Builder type(NetSyncType type) {
            this.type = type == null ? NetSyncType.UNKNOWN : type;
            return this;
        }

        public Builder connected(boolean connected) {
            this.connected = connected;
            return this;
        }

        public Builder validated(boolean validated) {
            this.validated = validated;
            return this;
        }

        public Builder metered(boolean metered) {
            this.metered = metered;
            return this;
        }

        public Builder roaming(boolean roaming) {
            this.roaming = roaming;
            return this;
        }

        public Builder vpn(boolean vpn) {
            this.vpn = vpn;
            return this;
        }

        public Builder downstreamKbps(int downstreamKbps) {
            this.downstreamKbps = Math.max(0, downstreamKbps);
            return this;
        }

        public Builder upstreamKbps(int upstreamKbps) {
            this.upstreamKbps = Math.max(0, upstreamKbps);
            return this;
        }

        public Builder timestampMillis(long timestampMillis) {
            this.timestampMillis = timestampMillis;
            return this;
        }

        public Builder reason(String reason) {
            this.reason = reason == null ? "Unknown" : reason;
            return this;
        }

        public NetSyncState build() {
            return new NetSyncState(this);
        }
    }
}
