package io.banditoz.dohmap.utils;

import jakarta.annotation.Nonnull;
import jakarta.annotation.Nullable;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * A simple array-backed work queue, with an {@link AtomicInteger}-based cursor, to track items.
 *
 * @param <T> Items in the work queue.
 */
public class WorkQueue<T> {
    private final T[] items;
    private final AtomicInteger cursor = new AtomicInteger(0);

    /**
     * @param items Items to use as the queue. This copies the array.
     * @throws NullPointerException If any item is null in the array.
     */
    @SuppressWarnings("unchecked")
    public WorkQueue(@Nonnull T... items) {
        if (items.length == 0) {
            throw new IllegalArgumentException("Array is empty");
        }
        this.items = (T[]) new Object[items.length];
        for (int i = 0; i < items.length; i++) {
            T thing = items[i];
            if (thing == null) {
                throw new NullPointerException("Item is null at index " + i);
            }
            this.items[i] = items[i];
        }
    }

    /**
     * Offsets the work queue to the first occurrence of <code>item</code>.
     *
     * @param item First occurrence to offset to.
     * @return The number of skipped elements in the array. Returns -1 if the element wasn't found. The cursor was not
     * advanced in this case.
     */
    public int offsetToFirstOccurrence(T item) {
        int i = 0;
        synchronized (this) {
            for (; i < items.length; i++) {
                if (items[i].equals(item)) {
                    cursor.addAndGet(i);
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     * Gets the next item from the <code>items</code> array atomically.
     *
     * @return The item, otherwise <code>null</code> if the work queue is finished.
     */
    @Nullable
    public T getNextItem() {
        int i = cursor.getAndIncrement();
        if (i >= items.length) {
            return null;
        } else {
            T item = items[i];
            items[i] = null;
            return item;
        }
    }

    public boolean hasMoreWork() {
        return cursor.get() < items.length;
    }
}
