package eu.toolchain.serializer;

import java.io.IOException;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class LengthPrefixedSerializer<T> implements Serializer<T> {
    final Serializer<Long> length;
    final Serializer<T> element;
    final LengthPolicy policy;

    @Override
    public void serialize(SerialWriter buffer, T value) throws IOException {

    }

    @Override
    public T deserialize(SerialReader buffer) throws IOException {
        return null;
    }
}