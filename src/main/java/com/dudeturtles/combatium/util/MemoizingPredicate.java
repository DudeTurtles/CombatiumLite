package com.dudeturtles.combatium.util;

import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

public class MemoizingPredicate<T> implements Predicate<T> {
    private final Object lock = new Object();
    private volatile Predicate<T> delegate;
    private final Map<T, Boolean> cache = Collections.synchronizedMap(new WeakHashMap<>());

    public MemoizingPredicate(Predicate<T> delegate) {
        this.delegate = Preconditions.checkNotNull(delegate);
    }

    public boolean apply(T test) {
        synchronized(this.lock) {
            if (!cache.containsKey(test)) {
                boolean b = this.delegate.apply(test);
                cache.put(test, b);
                return b;
            }
            return cache.get(test);
        }
    }

    public void resetCache() {
        cache.clear();
    }

    public String toString() {
        Predicate<T> delegate = this.delegate;
        return "MemoizingPredicate[" + delegate + " that caches " + cache + "]";
    }
}