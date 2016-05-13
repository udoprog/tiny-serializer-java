package eu.toolchain.serializer.type;

import eu.toolchain.serializer.SerialReader;
import eu.toolchain.serializer.SerialWriter;
import eu.toolchain.serializer.Serializer;
import lombok.RequiredArgsConstructor;

import java.io.IOException;

@RequiredArgsConstructor
public class OrdinalEnumSerializer<T extends Enum<T>> implements Serializer<T> {
    private final Serializer<Integer> ordinal;
    private final T[] values;
    private final DefaultAction<T> defaultAction;

    @Override
    public void serialize(SerialWriter buffer, T value) throws IOException {
        this.ordinal.serialize(buffer, value.ordinal());
    }

    @Override
    public T deserialize(SerialReader buffer) throws IOException {
        final int ordinal = this.ordinal.deserialize(buffer);

        if (ordinal < 0 || ordinal >= values.length) {
            return defaultAction.call();
        }

        return values[ordinal];
    }
}
