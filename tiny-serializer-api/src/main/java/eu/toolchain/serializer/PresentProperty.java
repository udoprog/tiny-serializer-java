package eu.toolchain.serializer;

public class PresentProperty<T> implements OptionalProperty<T> {
    final T property;

    public PresentProperty(T property) {
        this.property = property;
    }

    @Override
    public boolean isPresent() {
        return true;
    }

    @Override
    public T get() {
        return property;
    }

    @Override
    public T or(T other) {
        return property;
    }

    @Override
    public T or(PropertySupplier<T> other) {
        return property;
    }
}