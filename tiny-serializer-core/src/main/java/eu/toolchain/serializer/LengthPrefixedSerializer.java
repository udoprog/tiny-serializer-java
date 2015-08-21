package eu.toolchain.serializer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import lombok.RequiredArgsConstructor;
import eu.toolchain.serializer.io.ByteArraySerialReader;
import eu.toolchain.serializer.io.OutputStreamSerialWriter;

@RequiredArgsConstructor
public class LengthPrefixedSerializer<T> implements Serializer<T> {
    final Serializer<Integer> length;
    final Serializer<T> element;
    final LengthPolicy policy;

    @Override
    public void serialize(SerialWriter buffer, T value) throws IOException {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();

        try (final SerialWriter output = new OutputStreamSerialWriter(out)) {
            element.serialize(output, value);
        }

        final byte[] bytes = out.toByteArray();

        if (!policy.check(bytes.length)) {
            throw new IOException(String.format("Element violates policy %s", policy));
        }

        length.serialize(buffer, bytes.length);
        buffer.write(bytes);
    }

    @Override
    public T deserialize(SerialReader buffer) throws IOException {
        final int length = this.length.deserialize(buffer);

        if (!policy.check(length)) {
            throw new IOException(String.format("Element violates policy %s", policy));
        }

        final byte[] bytes = new byte[length];

        buffer.read(bytes);

        try (final ByteArraySerialReader reader = new ByteArraySerialReader(bytes)) {
            return element.deserialize(reader);
        }
    }
}