package eu.toolchain.serializer.type;

import java.io.IOException;
import java.util.Arrays;

import eu.toolchain.serializer.HexUtils;
import eu.toolchain.serializer.SerialReader;
import eu.toolchain.serializer.SerialWriter;
import eu.toolchain.serializer.Serializer;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class PrefixSerializer<T> implements Serializer<T> {
    private final byte[] prefix;
    private final Serializer<T> serializer;

    @Override
    public void serialize(SerialWriter buffer, T value) throws IOException {
        buffer.write(prefix);
        serializer.serialize(buffer, value);
    }

    @Override
    public T deserialize(SerialReader buffer) throws IOException {
        final byte[] actual = new byte[prefix.length];
        buffer.read(actual);

        if (!Arrays.equals(prefix, actual))
            throw new IllegalArgumentException("Invalid prefix, expected " + HexUtils.toHex(prefix) + " but got "
                    + HexUtils.toHex(actual));

        return serializer.deserialize(buffer);
    }
}