package eu.toolchain.serializer.io;

import eu.toolchain.serializer.SerialWriter;
import java.nio.ByteBuffer;

public class CoreByteBufferSerialWriterTest extends AbstractSerialWriterTest {
    @Override
    protected SerialWriter setupSerialWriter() {
        return new CoreByteBufferSerialWriter(ByteBuffer.allocate(SIZE));
    }
}
