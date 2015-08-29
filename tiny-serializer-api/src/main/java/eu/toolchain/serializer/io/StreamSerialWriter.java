package eu.toolchain.serializer.io;

import java.io.IOException;

import eu.toolchain.serializer.SerialWriter;

public interface StreamSerialWriter extends SerialWriter {
    /**
     * Flush the underlying stream.
     *
     * @throws IOException When a flush could not be fully performed.
     */
    public void flush() throws IOException;
}