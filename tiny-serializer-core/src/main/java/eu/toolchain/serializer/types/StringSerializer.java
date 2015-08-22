package eu.toolchain.serializer.types;

import java.io.IOException;
import java.nio.charset.Charset;

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
        byte[] bytes = value.getBytes(UTF_8);
        size.serialize(buffer, bytes.length);
        buffer.write(bytes);
    }

    @Override
    public String deserialize(SerialReader buffer) throws IOException {
        final int length = size.deserialize(buffer);
        byte[] bytes = new byte[length];
        buffer.read(bytes);
        return new String(bytes, UTF_8);
    }
}
