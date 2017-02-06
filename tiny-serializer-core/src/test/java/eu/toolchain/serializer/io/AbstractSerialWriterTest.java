package eu.toolchain.serializer.io;

import static org.junit.Assert.assertEquals;

import eu.toolchain.serializer.SerialWriter;
import java.nio.ByteBuffer;
import org.junit.Test;

public abstract class AbstractSerialWriterTest {
    protected static final int SIZE = 1024;

    protected abstract SerialWriter setupSerialWriter();

    @Test
    public void testWritePosition() throws Exception {
        final SerialWriter writer = setupSerialWriter();
        assertEquals(0L, writer.position());

        writer.write((byte) 0x10);
        assertEquals(1L, writer.position());

        writer.write(new byte[]{0x10, 0x20, 0x30});
        assertEquals(4L, writer.position());

        writer.write(new byte[]{0x10, 0x20, 0x30}, 1, 2);
        assertEquals(6L, writer.position());

        writer.write(ByteBuffer.wrap(new byte[]{0x10, 0x20, 0x30}, 1, 2));
        assertEquals(8L, writer.position());
    }
}
