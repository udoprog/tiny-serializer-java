package eu.toolchain.serializer;

import java.io.IOException;

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