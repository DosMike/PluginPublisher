package de.dosmike.sponge.utils;

import com.itwookie.utils.Expiring;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class CachingCollection<E> implements Collection<E> {

    private Collection<Expiring<E>> container;
    private long lifespan;
    /** timeSpan and timeUnit specify how long entries will be valid.
     * Elements will be lazy-checked (removed if expired & values requested) */
    public CachingCollection(Collection<Expiring<E>> container, long timeSpan, TimeUnit timeUnit) {
        this.container = container;
        lifespan = timeUnit.toMillis(timeSpan);
    }
    /** Wraps a {@link HashSet} with this caching collection, so not only
     * will elements expire, but they'll also be unique */
    public CachingCollection(long timeSpan, TimeUnit timeUnit) {
        this.container = new HashSet<>();
        lifespan = timeUnit.toMillis(timeSpan);
    }

    @Override
    public int size() {
        timeout();
        return container.size();
    }

    @Override
    public boolean isEmpty() {
        timeout();
        return container.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        timeout();
        return container.stream().anyMatch(expiring-> (Objects.equals(o, expiring.getAnyways())));
    }

    @NotNull
    @Override
    public Iterator<E> iterator() {
        timeout();
        return container.stream().map(Expiring::getAnyways).iterator();
    }

    @NotNull
    @Override
    public Object[] toArray() {
        timeout();
        return container.stream().map(Expiring::getAnyways).toArray();
    }

    @NotNull
    @Override
    public <T> T[] toArray(@NotNull T[] a) {
        timeout();
        return container.stream().map(Expiring::getAnyways).collect(Collectors.toList()).toArray(a);
    }

    /** if the container is a set and the container already contains an equal
     * element, the elements lifespan will be reset instead (change will be reported)<br>
     * See {@link Collection#add} for more information */
    @Override
    public boolean add(E e) {
        if (container instanceof Set) {
            timeout();
            Optional<Expiring<E>> element = container.stream()
                    .filter(expiring->Objects.equals(e, expiring.getAnyways()))
                    .findFirst();
            if (element.isPresent()) {
                container.remove(element.get());
                container.add(Expiring.expireIn(element.get().getAnyways(), lifespan));
                return true;
            } else {
                return container.add(Expiring.expireIn(e, lifespan));
            }
        } else {
            return container.add(Expiring.expireIn(e, lifespan));
        }
    }

    @Override
    public boolean remove(Object o) {
        Optional<Expiring<E>> wrapper = container.stream().filter(e->Objects.equals(o, e.getAnyways())).findFirst();
        return wrapper.filter(expiring -> container.remove(expiring)).isPresent();
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        timeout();
        for (Object x : c)
            if (container.stream().noneMatch(expiring-> (Objects.equals(x, expiring.getAnyways()))))
                return false;
        return true;
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends E> c) {
        boolean changed = false;
        if (container instanceof Set) {
            timeout();
            for (E x : c) {
                Optional<Expiring<E>> element = container.stream()
                        .filter(expiring -> Objects.equals(x, expiring.getAnyways()))
                        .findFirst();
                if (element.isPresent()) {
                    container.remove(element.get());
                    container.add(Expiring.expireIn(element.get().getAnyways(), lifespan));
                    return true;
                } else {
                    return container.add(Expiring.expireIn(x, lifespan));
                }
            }
        } else {
            for (E x : c) {
                changed |= container.add(Expiring.expireIn(x, lifespan));
            }
        }
        return changed;
    }

    @Override
    public boolean removeAll(@NotNull Collection<?> c) {
        timeout();
        return container.removeIf(e->c.contains(e.getAnyways()));
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
        timeout();
        return container.removeIf(e->!c.contains(e.getAnyways()));
    }

    @Override
    public void clear() {
        container.clear();
    }

    private void timeout() {
        container.removeIf(Expiring::isExpired);
    }

}
