package de.dosmike.sponge.limiter;

public interface Limiter {

    boolean canRequest();

    /** @return the unix timestamp, where the next request can be made */
    long nextRequestAt();

    /** blocks until a request can be made */
    default void waitForNext() {
        long nextAt = nextRequestAt();
        while (nextAt-System.currentTimeMillis() > 0) {
            try { Thread.sleep(nextAt-System.currentTimeMillis()); } catch (InterruptedException ignore) {}
        }
    }

    void takeRequest();

}
