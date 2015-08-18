package eu.toolchain.serializer;

public class AbsentProperty<T> implements OptionalProperty<T> {
    @Override
    public boolean isPresent() {
        return false;
    }

    @Override
    public T get() {
        throw new IllegalStateException("Property is not available");
    }

    @Override
    public T or(final T other) {
        return other;
    }

    @Override
    public T or(final PropertySupplier<T> other) {
        return other.supply();
    }

    static final OptionalProperty<?> absent = new AbsentProperty<>();

    @SuppressWarnings("unchecked")
    public static <T> OptionalProperty<T> absent() {
        return (OptionalProperty<T>) absent;
    }
}