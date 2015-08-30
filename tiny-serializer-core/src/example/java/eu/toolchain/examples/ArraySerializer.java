package eu.toolchain.examples;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;

import eu.toolchain.serializer.SerialReader;
import eu.toolchain.serializer.Serializer;
import eu.toolchain.serializer.TinySerializer;
import eu.toolchain.serializer.io.BytesSerialWriter;

public class ArraySerializer {
    public static void main(String argv[]) throws IOException {
        final TinySerializer s = SerializerSetup.setup().build();
        Serializer<int[][][]> aa = s.array(s.<int[]>array(s.intArray(), (size) -> new int[size][]), (size) -> new int[size][][]);

        final Serializer<Map<String, String>> map = s.map(s.string(), s.nullable(s.string()));

        final Map<String, String> serialized = new HashMap<>();
        serialized.put("hello", "world");
        serialized.put("foo", "bar");

        final ByteBuffer bytes;

        try (final BytesSerialWriter buffer = s.writeBytes()) {
            map.serialize(buffer, serialized);
            bytes = buffer.toByteBuffer();
        }

        final Map<String, String> result;

        try (final SerialReader buffer = s.readByteBuffer(bytes)) {
            result = map.deserialize(buffer);
        }

        System.out.println(String.format("Serialized size: %d", bytes.limit()));
        System.out.println(String.format("Equals?: %s (%s = %s)", serialized.equals(result), serialized, result));
    }
}
