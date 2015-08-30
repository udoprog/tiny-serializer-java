package eu.toolchain.serializer.array;

import java.io.IOException;

import eu.toolchain.serializer.SerialReader;
import eu.toolchain.serializer.SerialWriter;
import eu.toolchain.serializer.Serializer;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class BooleanArraySerializer implements Serializer<boolean[]> {
    private final Serializer<Integer> size;

    @Override
    public void serialize(SerialWriter buffer, boolean[] value) throws IOException {
        final int size = (value.length + 7) / 8;
        final byte[] bytes = new byte[size];

        for (int i = 0; i < value.length; i++) {
            if (value[i]) {
                bytes[i / 8] |= (1 << (i % 8));
            }
        }

        this.size.serialize(buffer, value.length);
        buffer.write(bytes);
    }

    @Override
    public boolean[] deserialize(SerialReader buffer) throws IOException {
        final int length = size.deserialize(buffer);

        final byte[] bytes = new byte[(length + 7) / 8];
        final boolean[] value = new boolean[length];

        buffer.read(bytes);

        for (int i = 0; i < value.length; i++) {
            value[i] = (bytes[i / 8] & (1 << (i % 8))) != 0;
        }

        return value;
    }
}