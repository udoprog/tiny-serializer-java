package eu.toolchain.serializer.type;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;

import eu.toolchain.serializer.SerialReader;
import eu.toolchain.serializer.SerialWriter;
import eu.toolchain.serializer.Serializer;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class StringSerializer implements Serializer<String> {
    private final Serializer<Integer> size;

    private static final Charset UTF_8 = Charset.forName("utf8");

    @Override
    public void serialize(SerialWriter buffer, String value) throws IOException {
        final CharsetEncoder encoder = UTF_8.newEncoder();
        /* allocate a worst-case buffer */
        final int worst = (int) (value.length() * encoder.maxBytesPerChar());
        final ByteBuffer bytes = buffer.pool().allocate(worst);

        try {
            encoder.encode(CharBuffer.wrap(value), bytes, true);
            bytes.flip();

            this.size.serialize(buffer, bytes.remaining());

            buffer.write(bytes);
        } finally {
            buffer.pool().release(worst);
        }
    }

    @Override
    public String deserialize(SerialReader buffer) throws IOException {
        final CharsetDecoder decoder = UTF_8.newDecoder();

        final int length = this.size.deserialize(buffer);

        final ByteBuffer bytes = buffer.pool().allocate(length);

        try {
            buffer.read(bytes);
            bytes.flip();
            return decoder.decode(bytes).toString();
        } finally {
            buffer.pool().release(length);
        }
    }
}
