package eu.toolchain.serializer;

import java.io.IOException;

import eu.toolchain.serializer.SerialReader;
import eu.toolchain.serializer.SerialWriter;
import eu.toolchain.serializer.Serializer;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class EnumSerializer<T extends Enum<T>> implements Serializer<T> {
    private final Serializer<Integer> ordinal;
    private final T[] values;

    @Override
    public void serialize(SerialWriter buffer, T value) throws IOException {
        this.ordinal.serialize(buffer, value.ordinal());
    }

    @Override
    public T deserialize(SerialReader buffer) throws IOException {
        return values[this.ordinal.deserialize(buffer)];
    }
}