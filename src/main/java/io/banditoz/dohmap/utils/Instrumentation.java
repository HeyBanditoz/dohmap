package io.banditoz.dohmap.utils;

import java.util.Objects;
import java.util.function.Supplier;

/**
 * Class to instrument a {@link Supplier}. Stores the result of the {@link Supplier}, and the nanoseconds it took to run
 * the {@link Supplier}.
 *
 * @param <T> The result type of which the {@link Supplier} has instrumented
 */
public class Instrumentation<T> {
    private final T result;
    private final long nanosTook;

    /**
     * Private constructor, see {@link Instrumentation#instrument(Supplier)}
     */
    private Instrumentation(T result, long nanosTook) {
        this.result = result;
        this.nanosTook = nanosTook;
    }

    /**
     * Instruments a supplier.
     *
     * @param supplier The {@link Supplier} to instrument.
     * @param <T>      The result type.
     * @return A new {@link Instrumentation} containing the result, and the time it took to run the {@link Supplier}, in
     * nanoseconds.
     */
    public static <T> Instrumentation<T> instrument(Supplier<T> supplier) {
        long before = System.nanoTime();
        T t = supplier.get();
        long after = System.nanoTime();
        return new Instrumentation<>(t, (after - before));
    }

    public T getResult() {
        return result;
    }

    public long getNanosTook() {
        return nanosTook;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Instrumentation<?> that = (Instrumentation<?>) o;
        return nanosTook == that.nanosTook && Objects.equals(result, that.result);
    }

    @Override
    public int hashCode() {
        return Objects.hash(result, nanosTook);
    }

    @Override
    public String toString() {
        return "Instrumentation{" +
                "result=" + result +
                ", nanosTook=" + nanosTook +
                '}';
    }
}
