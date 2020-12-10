package de.dosmike.sponge.limiter;

import java.net.HttpURLConnection;

/** Limiter based on Response Headers X-RateLimit- */
public class XRateLimit implements Limiter {

    int bucket;
    long reset;

    String headerRemaining;
    String headerReset;
    boolean timestampMS;

    /** The headers often use different names, so please specify */
    public XRateLimit(String headerNameRemainingRequests, String headerNameResetTimestamp, boolean timestampMilliseconds) {
        headerRemaining = headerNameRemainingRequests;
        headerReset = headerNameResetTimestamp;
        timestampMS = timestampMilliseconds;
        bucket = 1; //assume we can request in order to get the first limit result
        reset = 0L;
    }

    @Override
    public boolean canRequest() {
        return bucket>0;
    }

    @Override
    public long nextRequestAt() {
        return bucket > 0 ? System.currentTimeMillis() : reset;
    }

    @Override
    public void takeRequest() {
        bucket--;
    }

    public void updateBucket(HttpURLConnection connection) {
        try {
            bucket = Integer.parseInt(connection.getHeaderField(headerRemaining));
            reset = Long.parseLong(connection.getHeaderField(headerReset));
            if (!timestampMS) reset /= 1000;
        } catch (Exception e) {
            // ignore missing bucket information, might deadlock the application!
        }
    }
}
