package eu.toolchain.serializer.io;

import eu.toolchain.serializer.SerialReader;

public class CoreByteChannelSerialReaderTest extends AbstractSerialReaderTest {
    @Override
    protected SerialReader setupSerialReader(final byte[] bytes) throws Exception {
        return new CoreByteChannelSerialReader(new FakeReadableByteChannel(bytes));
    }
}
