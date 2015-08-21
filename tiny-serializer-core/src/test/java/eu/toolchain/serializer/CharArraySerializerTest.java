package eu.toolchain.serializer;

import static org.junit.Assert.assertArrayEquals;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.junit.Test;

import eu.toolchain.serializer.io.CharArraySerializer;
import eu.toolchain.serializer.var.CompactVarIntSerializer;

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