package eu.toolchain.serializer.type;

import java.io.IOException;

import eu.toolchain.serializer.SerialReader;
import eu.toolchain.serializer.SerialWriter;
import eu.toolchain.serializer.Serializer;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SingletonSerializer<T> implements Serializer<T> {
    private final T value;

    @Override
    public void serialize(SerialWriter buffer, T value) throws IOException {
    }

    @Override
    public T deserialize(SerialReader buffer) throws IOException {
        return value;
    }
}