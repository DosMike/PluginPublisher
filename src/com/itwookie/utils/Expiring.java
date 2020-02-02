package com.itwookie.utils;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class Expiring<T> {
    T value;
    long expirationDate;

    private Expiring() {
        value = null;
        expirationDate = 0L;
    }

    private Expiring(T object, long lifespan) {
        value = object;
        expirationDate = System.currentTimeMillis() + lifespan;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() > expirationDate;
    }

    public boolean isAlive() {
        return System.currentTimeMillis() <= expirationDate;
    }

    /**
     * get this object, if it has not yet expired. otherwise throws exception
     *
     * @return the object
     * @throws IllegalStateException if the object expired
     */
    public T get() {
        if (isExpired())
            throw new IllegalStateException("This object expired");
        return value;
    }

    /**
     * get this object, if it has not yet expired. otherwise other is returned
     */
    public T orElse(T other) {
        return isExpired() ? other : value;
    }

    /**
     * get this object, if it has not yet expired. otherwise other is supplied
     */
    public T orElseGet(Supplier<T> other) {
        return isExpired() ? other.get() : value;
    }

    /**
     * applies consumer to object only if the object has not yet expired
     */
    public void ifAlive(Consumer<T> consumer) {
        if (isAlive()) consumer.accept(value);
    }

    /**
     * tries to get the object
     *
     * @return the object wrapped in a optional
     */
    public Optional<T> poll() {
        return isExpired() ? Optional.empty() : Optional.of(value);
    }

    /**
     * It is not recommended to use expired values. If you insist on using them
     * anyways you can use this method.
     *
     * @return the value, regardless of it's expiration state
     */
    public T getAnyways() {
        return value;
    }

    /**
     * Makes this object expire at the specified time
     *
     * @param object a time limited object
     * @param timeAt the unix timestamp in ms at which this object expires
     */
    public static <Y> Expiring<Y> expireAt(Y object, long timeAt) {
        return new Expiring<>(object, timeAt - System.currentTimeMillis());
    }

    /**
     * Makes this object expire after some specified time
     *
     * @param object   a time limited object
     * @param lifespan the time in ms after which this object expires
     */
    public static <Y> Expiring<Y> expireIn(Y object, long lifespan) {
        return new Expiring<>(object, lifespan);
    }

    public static <Y> Expiring<Y> expired() {
        return new Expiring<>();
    }

    public long getExpirationDate() {
        return expirationDate;
    }

    public long getRemainingLifespan() {
        return Math.min(0, expirationDate - System.currentTimeMillis());
    }

    @Override
    public String toString() {
        return "Expiring<" + String.valueOf(value) + ">";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Expiring<?> expiring = (Expiring<?>) o;
        return expirationDate == expiring.expirationDate &&
                Objects.equals(value, expiring.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, expirationDate);
    }
}
