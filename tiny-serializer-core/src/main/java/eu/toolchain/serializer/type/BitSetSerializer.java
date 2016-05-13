package eu.toolchain.serializer.type;

import eu.toolchain.serializer.SerialReader;
import eu.toolchain.serializer.SerialWriter;
import eu.toolchain.serializer.Serializer;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.util.BitSet;
import java.util.function.Supplier;

@RequiredArgsConstructor
public class BitSetSerializer implements Serializer<BitSet> {
    /* size in bits of a single byte */
    public static final int SIZE = 8;

    private final Serializer<Integer> size;

    @Override
    public void serialize(final SerialWriter buffer, final BitSet value) throws IOException {
        final int size = (value.length() + (SIZE - 1)) / SIZE;
        final byte[] bytes = new byte[size];

        for (int i = 0; i < value.length(); i++) {
            if (value.get(i)) {
                bytes[i / SIZE] |= (1 << (i % SIZE));
            }
        }

        this.size.serialize(buffer, value.length());
        buffer.write(bytes);
    }

    @Override
    public BitSet deserialize(final SerialReader buffer) throws IOException {
        final int length = size.deserialize(buffer);

        final byte[] bytes = new byte[(length + (SIZE - 1)) / SIZE];
        final BitSet value = new BitSet(length);

        buffer.read(bytes);

        for (int i = 0; i < value.length(); i++) {
            if ((bytes[i / SIZE] & (1 << (i % SIZE))) != 0) {
                value.set(i);
            }
        }

        return value;
    }

    public static Supplier<Serializer<BitSet>> supplier(final Serializer<Integer> size) {
        return () -> new BitSetSerializer(size);
    }
}
