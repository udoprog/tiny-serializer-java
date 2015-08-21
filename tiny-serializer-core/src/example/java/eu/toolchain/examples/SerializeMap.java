package eu.toolchain.examples;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import eu.toolchain.serializer.Serializer;
import eu.toolchain.serializer.TinySerializer;
import eu.toolchain.serializer.io.ByteBufferSerialReader;
import eu.toolchain.serializer.io.ByteBufferSerialWriter;

public class SerializeMap {
    public static void main(String argv[]) throws IOException {
        final TinySerializer s = SerializerSetup.setup();
        final Serializer<Map<String, String>> map = s.map(s.string(), s.nullable(s.string()));

        final Map<String, String> serialized = new HashMap<>();
        serialized.put("hello", "world");
        serialized.put("foo", "bar");

        final ByteBuffer bytes;

        try (final ByteBufferSerialWriter buffer = new ByteBufferSerialWriter()) {
            map.serialize(buffer, serialized);
            bytes = buffer.buffer();
        }

        final Map<String, String> result;

        try (final ByteBufferSerialReader buffer = new ByteBufferSerialReader(bytes)) {
            result = map.deserialize(buffer);
        }

        System.out.println(String.format("Serialized size: %d", bytes.limit()));
        System.out.println(String.format("Equals?: %s (%s = %s)", serialized.equals(result), serialized, result));
    }
}
