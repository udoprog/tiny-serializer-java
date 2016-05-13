package eu.toolchain.serializer.array;

import eu.toolchain.serializer.SerialReader;
import eu.toolchain.serializer.SerialWriter;
import eu.toolchain.serializer.Serializer;
import lombok.RequiredArgsConstructor;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;

@RequiredArgsConstructor
public class CharacterArraySerializer implements Serializer<char[]> {
    final Charset UTF8 = Charset.forName("UTF-8");

    private final Serializer<Integer> size;

    @Override
    public void serialize(SerialWriter buffer, char[] value) throws IOException {
        final CharsetEncoder encoder = UTF8.newEncoder();
        final int expected = value.length * (int) encoder.maxBytesPerChar();
        final ByteBuffer target = buffer.pool().allocate(expected);

        try {
            encoder.encode(CharBuffer.wrap(value), target, true);
            target.flip();

            this.size.serialize(buffer, target.remaining());
            buffer.write(target);
        } finally {
            buffer.pool().release(expected);
        }
    }

    @Override
    public char[] deserialize(SerialReader buffer) throws IOException {
        final CharsetDecoder decoder = UTF8.newDecoder();

        final int size = this.size.deserialize(buffer);

        final ByteBuffer target = buffer.pool().allocate(size);

        try {
            buffer.read(target);
            target.flip();

            final CharBuffer chars = decoder.decode(target);

            final char output[] = new char[chars.remaining()];
            chars.get(output);
            return output;
        } finally {
            buffer.pool().release(size);
        }
    }
}
