package eu.toolchain.serializer.io;

import eu.toolchain.serializer.SerialReader;
import java.nio.ByteBuffer;

public class CoreByteBufferSerialReaderTest extends AbstractSerialReaderTest {
    @Override
    protected SerialReader setupSerialReader(final byte[] bytes) {
        return new CoreByteBufferSerialReader(ByteBuffer.wrap(bytes));
    }
}
