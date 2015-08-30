package eu.toolchain.serializer.type;

import java.io.IOException;

import eu.toolchain.serializer.BytesSerialWriter;
import eu.toolchain.serializer.LengthPolicy;
import eu.toolchain.serializer.SerialReader;
import eu.toolchain.serializer.SerialWriter;
import eu.toolchain.serializer.Serializer;
import eu.toolchain.serializer.SerializerFramework;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class LengthPrefixedSerializer<T> implements Serializer<T> {
    final SerializerFramework framework;
    final Serializer<Integer> length;
    final Serializer<T> element;
    final LengthPolicy policy;

    @Override
    public void serialize(SerialWriter buffer, T value) throws IOException {
        final byte[] bytes;

        try (final BytesSerialWriter output = framework.writeBytes()) {
            element.serialize(output, value);
            bytes = output.toByteArray();
        }

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

        try (final SerialReader reader = framework.readByteArray(bytes)) {
            return element.deserialize(reader);
        }
    }
}