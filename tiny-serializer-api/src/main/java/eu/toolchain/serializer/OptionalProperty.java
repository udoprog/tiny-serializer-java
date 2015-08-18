package eu.toolchain.serializer;

public interface OptionalProperty<T> {
    boolean isPresent();

    T get();

    T or(T other);

    T or(PropertySupplier<T> other);
}