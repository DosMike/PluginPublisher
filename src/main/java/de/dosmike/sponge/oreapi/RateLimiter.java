package de.dosmike.sponge.oreapi;

import de.dosmike.sponge.limiter.BucketLimiter;
import de.dosmike.sponge.limiter.Limiter;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.*;
import java.util.function.Supplier;

/** if you're concerned about dosing the ore servers, take this */
public class RateLimiter extends Thread {

    List<RunnableFuture<?>> tasks = new LinkedList<>();
    boolean running = true;
    private final Object taskMutex = new Object();
    private Limiter limit;

    public RateLimiter(Limiter limiter) {
        this.limit = limiter;
        try {
            Thread.currentThread().setName("Ore Query Limiter");
        } catch (SecurityException ignore) {}
    }
    public RateLimiter() {
        this(new BucketLimiter(2,80)); //bucket is faster than averaging
    }

    @Override
    public void run() {
        boolean nowIdle;
        while(running) {
            RunnableFuture<?> task = null;
            synchronized (taskMutex) {
                if (!tasks.isEmpty())
                    task = tasks.remove(0);
            }
            if (task != null) {
                try {
                    task.run();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                //on idle callback
                synchronized (taskMutex) {
                    nowIdle = tasks.isEmpty();
                }
                if (nowIdle && onIdleCallback != null)
                    try {
                        onIdleCallback.run();
                    } catch (Exception ignore) {/* not my fault */}
            } else nowIdle = true;
            if (nowIdle) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException ignore) { }
            } else {
                limit.waitForNext();
            }
        }
        //OreGet.l("Rate Limiter terminated");
    }

    public <T> Future<T> enqueue(Supplier<T> task) {
        if (!isAlive()) throw new IllegalStateException("The rate limiter has already terminated");
        RunnableFuture<T> future = new RunnableFuture<T>() {
            T result;
            boolean done;
            @Override
            public void run() {
                try {
                    result = task.get();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    done = true;
                }
            }
            @Override public boolean cancel(boolean mayInterruptIfRunning) { return false; }
            @Override public boolean isCancelled() { return false; }
            @Override public boolean isDone() { return done; }
            @Override public T get() throws InterruptedException, ExecutionException {
                while (!done) Thread.yield();
                return result;
            }
            /** not really supported! */
            @Override
            public T get(long timeout, @NotNull TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
                while (!done) Thread.yield();
                return result;
            }
        };
        synchronized (taskMutex) {
            tasks.add(future);
        }
        return future;
    }

    /** Shut down this RateLimiter after the current task finished */
    public void halt() {
        running = false;
    }

    private Runnable onIdleCallback=null;
    /** specify a runnable that gets executed after the last task ran. Does not execute if the limiter starts idle */
    public void onIdle(Runnable whenIdle) {
        onIdleCallback = whenIdle;
    }

    /** tries to resolve all futures and silently discards failing futures.
     * This method blocks until all futures have completed */
    public static <T> List<T> waitForAll(Collection<Future<T>> collection) {
        List<T> results = new LinkedList<>();
        for (Future<T> f : collection) {
            try {
                results.add(f.get());
            } catch (InterruptedException | ExecutionException ignore) { }
        }
        return results;
    }
    /** tries to resolve the future and silently discards failing futures.
     * This method is supposed to make code more readable by removing the need for try{} */
    public <T> Optional<T> waitFor(Supplier<T> task) {
        try {
            return Optional.ofNullable(enqueue(task).get());
        } catch (InterruptedException | ExecutionException ignore) { }
        return Optional.empty();
    }

    public void takeRequest() {
        limit.takeRequest();
    }

}
