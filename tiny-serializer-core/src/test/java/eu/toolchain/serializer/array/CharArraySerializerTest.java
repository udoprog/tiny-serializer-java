package eu.toolchain.serializer.array;

import static org.junit.Assert.assertArrayEquals;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.junit.Test;

import eu.toolchain.serializer.Serializer;
import eu.toolchain.serializer.SerializerFramework;
import eu.toolchain.serializer.TinySerializer;
import eu.toolchain.serializer.array.CharArraySerializer;
import eu.toolchain.serializer.primitive.CompactVarIntSerializer;

public class CharArraySerializerTest {
    final SerializerFramework s = TinySerializer.builder().build();

    final Serializer<char[]> serializer = new CharArraySerializer(new CompactVarIntSerializer());

    private void roundtrip(char[] chars) throws IOException {
        final ByteBuffer buffer = s.serialize(serializer, chars);
        final char[] result = s.deserialize(serializer, buffer);
        assertArrayEquals(chars, result);
    }

    @Test
    public void testBasic() throws IOException {
        roundtrip(new char[] { 'a', 'b', 'c' });
        roundtrip(new char[] { 'å', 'ä', 'ö' });
    }
}