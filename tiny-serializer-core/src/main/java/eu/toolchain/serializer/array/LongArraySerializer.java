package eu.toolchain.serializer.array;

import eu.toolchain.serializer.SerialReader;
import eu.toolchain.serializer.SerialWriter;
import eu.toolchain.serializer.Serializer;
import eu.toolchain.serializer.primitive.LongSerializer;
import lombok.RequiredArgsConstructor;

import java.io.IOException;

@RequiredArgsConstructor
public class LongArraySerializer implements Serializer<long[]> {
    private static final int BYTES = 8;

    private final Serializer<Integer> size;

    @Override
    public void serialize(SerialWriter buffer, long[] value) throws IOException {
        final int size = value.length * BYTES;
        final byte[] bytes = new byte[size];

        for (int i = 0; i < value.length; i++) {
            LongSerializer.toBytes(value[i], bytes, i * BYTES);
        }

        this.size.serialize(buffer, value.length);
        buffer.write(bytes);
    }

    @Override
    public long[] deserialize(SerialReader buffer) throws IOException {
        final int length = size.deserialize(buffer);

        final byte[] bytes = new byte[length * BYTES];
        final long[] value = new long[length];

        buffer.read(bytes);

        for (int i = 0; i < value.length; i++) {
            value[i] = LongSerializer.fromBytes(bytes, i * BYTES);
        }

        return value;
    }
}
