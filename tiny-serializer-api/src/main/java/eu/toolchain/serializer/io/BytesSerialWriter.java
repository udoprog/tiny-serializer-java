package eu.toolchain.serializer.io;

import java.nio.ByteBuffer;

import eu.toolchain.serializer.SerialWriter;

public interface BytesSerialWriter extends SerialWriter {
    /**
     * Copy the underlying buffer, and provide a byte array.
     *
     * @return A byte array copy of the underlying buffer.
     */
    byte[] toByteArray();

    /**
     * Copy the underlying buffer, and provide a {@link ByteBuffer}.
     *
     * @return A {@link ByteBuffer} copy of the underlying buffer.
     */
    ByteBuffer toByteBuffer();
}