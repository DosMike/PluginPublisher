package de.dosmike.sponge.limiter;

import de.dosmike.sponge.utils.CachingCollection;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class AveragingLimiter implements Limiter {

    private int requestsPerSecond;
    private int requestsPerMinute;

    private CachingCollection<Long> requestCache = new CachingCollection<>(new ArrayList<>(requestsPerMinute), 1, TimeUnit.MINUTES);

    public AveragingLimiter(int perSecond, int perMinute) {
        requestsPerSecond = perSecond;
        requestsPerMinute = perMinute;
    }
    public AveragingLimiter(){
        requestsPerSecond = 2;
        requestsPerMinute = 80;
    }

    /** @return true if no limit was exceeded */
    @Override
    public boolean canRequest() {
        if (requestCache.size() >= requestsPerMinute) return false;
        int lastSecond=0;
        Long now = System.currentTimeMillis();
        for (Long timestamp : requestCache) {
            if (now-timestamp <= 1000) lastSecond++;
        }
        return lastSecond < requestsPerSecond;
    }

    /** @return the unix timestamp, where the next request can be made */
    @Override
    public long nextRequestAt() {
        long now = System.currentTimeMillis();
        long oldestTimeMin = now; //oldest time within this minute expires
        long oldestTimeSec = now; //oldest time within this second expires
        int lastSecond=0;
        for (long timestamp : requestCache) {
            if (now-timestamp <= 1000) {
                if (timestamp < oldestTimeSec) oldestTimeSec = timestamp;
                lastSecond++;
            }
            if (timestamp < oldestTimeMin) oldestTimeMin = timestamp;
        }
        // no limit hit
        if (requestCache.size() < requestsPerMinute && lastSecond < requestsPerSecond) return now;

        //convert oldestTime timestamps to "when will they expire"
        // -> expires at = created at + max age

        //limit for second hit, would also free minute limit if hit
        if (lastSecond >= requestsPerSecond) return oldestTimeSec + 1_001L;
        //minute limit was hit, wait for that to expire
        else return oldestTimeMin + 60_001L;
    }

    @Override
    public void takeRequest() {
        requestCache.add(System.currentTimeMillis());
    }

}
