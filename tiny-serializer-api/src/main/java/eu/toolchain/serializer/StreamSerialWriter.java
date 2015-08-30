package eu.toolchain.serializer;

import java.io.IOException;

public interface StreamSerialWriter extends SerialWriter {
    /**
     * Flush the underlying stream.
     *
     * @throws IOException When a flush could not be fully performed.
     */
    public void flush() throws IOException;
}