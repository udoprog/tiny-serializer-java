package eu.toolchain.serializer;

import java.io.IOException;
import java.util.Map;

import com.google.common.collect.ImmutableMap;

public class StringEnumSerializer<T extends Enum<T>> implements Serializer<T> {
    private final Serializer<String> string;
    private final DefaultAction<T> defaultAction;
    private final Map<String, T> mapping;

    public StringEnumSerializer(Serializer<String> string, T[] values, DefaultAction<T> defaultAction) {
        this.string = string;
        this.defaultAction = defaultAction;
        this.mapping = buildNameMapping(values);
    }

    private Map<String, T> buildNameMapping(T[] values) {
        final ImmutableMap.Builder<String, T> mapping = ImmutableMap.builder();

        for (final T value : values) {
            mapping.put(value.name(), value);
        }

        return mapping.build();
    }

    @Override
    public void serialize(SerialWriter buffer, T value) throws IOException {
        this.string.serialize(buffer, value.name());
    }

    @Override
    public T deserialize(SerialReader buffer) throws IOException {
        final T result = mapping.get(this.string.deserialize(buffer));

        if (result == null) {
            return defaultAction.call();
        }

        return result;
    }
}