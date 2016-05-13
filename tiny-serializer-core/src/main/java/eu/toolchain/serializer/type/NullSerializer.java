package eu.toolchain.serializer.type;

import eu.toolchain.serializer.SerialReader;
import eu.toolchain.serializer.SerialWriter;
import eu.toolchain.serializer.Serializer;

import java.io.IOException;

/**
 * {@code null} is serialized as follows.
 * <p>
 * <pre>
 * | 0    |
 * | 0x7e |
 * </pre>
 * <p>
 * Non-{@code null} is serialized as follows.
 * <p>
 * <pre>
 * | 0    | 1..n  |
 * | 0x7f | value |
 * </pre>
 */
public class NullSerializer<T> implements Serializer<T> {
    public static final byte[] NULL = new byte[]{0x7e};
    public static final byte[] NOT_NULL = new byte[]{0x7f};

    private final Serializer<T> serializer;

    public NullSerializer(Serializer<T> serializer) {
        this.serializer = serializer;
    }

    @Override
    public void serialize(SerialWriter buffer, T value) throws IOException {
        if (value == null) {
            buffer.write(NULL);
            return;
        }

        buffer.write(NOT_NULL);
        serializer.serialize(buffer, value);
    }

    @Override
    public T deserialize(SerialReader buffer) throws IOException {
        final byte[] bytes = new byte[1];

        buffer.read(bytes);

        if (bytes[0] == 0x7e) {
            return null;
        }

        return serializer.deserialize(buffer);
    }
}
