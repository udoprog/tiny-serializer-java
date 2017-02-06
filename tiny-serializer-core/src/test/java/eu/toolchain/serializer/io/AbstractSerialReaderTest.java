package eu.toolchain.serializer.io;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import eu.toolchain.serializer.SerialReader;
import java.nio.ByteBuffer;
import org.junit.Test;

public abstract class AbstractSerialReaderTest {
    protected abstract SerialReader setupSerialReader(final byte[] bytes) throws Exception;

    @Test
    public void testReadPosition() throws Exception {
        final byte[] bytes = new byte[128];

        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) i;
        }

        final SerialReader reader = setupSerialReader(bytes);
        assertEquals(0L, reader.position());

        assertEquals(0, reader.read());
        assertEquals(1L, reader.position());

        byte[] three = new byte[3];

        reader.read(three);
        assertArrayEquals(new byte[]{1, 2, 3}, three);
        assertEquals(4L, reader.position());

        three[0] = -1;
        reader.read(three, 1, 2);
        assertArrayEquals(new byte[]{-1, 4, 5}, three);
        assertEquals(6L, reader.position());

        reader.read(ByteBuffer.wrap(three));
        assertArrayEquals(new byte[]{6, 7, 8}, three);
        assertEquals(9L, reader.position());
    }
}
