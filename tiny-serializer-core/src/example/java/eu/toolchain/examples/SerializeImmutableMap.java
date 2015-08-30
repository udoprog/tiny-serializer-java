package eu.toolchain.examples;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Map;

import com.google.common.collect.ImmutableMap;

import eu.toolchain.serializer.BytesSerialWriter;
import eu.toolchain.serializer.SerialReader;
import eu.toolchain.serializer.Serializer;
import eu.toolchain.serializer.TinySerializer;

public class SerializeImmutableMap {
    public static void main(String argv[]) throws IOException {
        final TinySerializer s = SerializerSetup.setup().useImmutableCollections(true).build();
        final Serializer<Map<String, String>> map = s.map(s.string(), s.nullable(s.string()));

        final Map<String, String> serialized = ImmutableMap.of("hello", "world", "foo", "bar");

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
