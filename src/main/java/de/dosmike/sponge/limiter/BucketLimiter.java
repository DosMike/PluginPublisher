package de.dosmike.sponge.limiter;

public class BucketLimiter implements Limiter {

    private int requestsPerSecond;
    private int requestsPerMinute;

    private int bucketSecond;
    private int bucketMinute;

    private long resetSecondAt;
    private long resetMinuteAt;

    public BucketLimiter(int perSecond, int perMinute) {
        requestsPerSecond = perSecond;
        requestsPerMinute = perMinute;
        bucketSecond = requestsPerSecond;
        bucketMinute = requestsPerMinute;
        resetSecondAt = System.currentTimeMillis()+ 1_001L;
        resetMinuteAt = System.currentTimeMillis()+60_001L;
    }
    public BucketLimiter(){
        this(2,80);
    }

    @Override
    public boolean canRequest() {
        _update();
        return bucketSecond > 0 && bucketMinute > 0;
    }

    @Override
    public long nextRequestAt() {
        _update();
        if (bucketSecond > 0 && bucketMinute > 0) return 0L;
        if (bucketSecond == 0 && bucketMinute == 0) return Math.max(resetSecondAt, resetMinuteAt);
        if (bucketSecond == 0) return resetSecondAt;
        return resetMinuteAt;
    }

    @Override
    public void takeRequest() {
        if (bucketSecond > 0) bucketSecond--;
        if (bucketMinute > 0) bucketMinute--;
    }

    private void _update() {
        long now = System.currentTimeMillis();
        if (now > resetMinuteAt) {
            resetMinuteAt = now + 60_001L;
            bucketMinute = requestsPerMinute;
        }
        if (now > resetSecondAt) {
            resetSecondAt = now + 1_001L;
            bucketSecond = requestsPerSecond;
        }
    }
}
