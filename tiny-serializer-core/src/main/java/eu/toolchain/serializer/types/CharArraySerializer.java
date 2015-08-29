package eu.toolchain.serializer.types;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;

import lombok.RequiredArgsConstructor;
import eu.toolchain.serializer.SerialReader;
import eu.toolchain.serializer.SerialWriter;
import eu.toolchain.serializer.Serializer;

@RequiredArgsConstructor
public class CharArraySerializer implements Serializer<char[]> {
    final Charset utf8 = Charset.forName("UTF-8");

    private final Serializer<Integer> size;

    @Override
    public void serialize(SerialWriter buffer, char[] value) throws IOException {
        final ByteBuffer data = utf8.encode(CharBuffer.wrap(value));
        size.serialize(buffer, data.limit());
        final byte[] bytes = new byte[data.limit()];
        data.get(bytes);
        buffer.write(bytes);
    }

    @Override
    public char[] deserialize(SerialReader buffer) throws IOException {
        final int length = size.deserialize(buffer);

        final byte[] bytes = new byte[length];
        buffer.read(bytes);

        final CharBuffer v = utf8.decode(ByteBuffer.wrap(bytes));

        char[] value = new char[v.limit()];
        v.get(value);

        return value;
    }
}