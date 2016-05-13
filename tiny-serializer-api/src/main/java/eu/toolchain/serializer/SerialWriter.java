package eu.toolchain.serializer;

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;

public interface SerialWriter extends Closeable {
    /**
     * Write a single byte.
     */
    public void write(byte b) throws IOException;

    /**
     * Write an array of bytes from the given buffer.
     * <p>
     * Will write all {@link ByteBuffer#remaining()} bytes.
     */
    public void write(ByteBuffer buffer) throws IOException;

    /**
     * Write an array of bytes.
     *
     * @param bytes Array of bytes to write.
     * @throws IOException When bytes cannot be written.
     */
    public void write(byte[] bytes) throws IOException;

    /**
     * Write an array of bytes with the given offset and length.
     *
     * @param bytes Bytes to write.
     * @param offset Offset to start writing from.
     * @param length Length to write.
     * @throws IOException If underlying stream could not be written to.
     */
    public void write(byte[] bytes, int offset, int length) throws IOException;

    /**
     * Create a scoped writer.
     * <p>
     * Scopes sections are sections that can be skipped. They are typically prefixed with the length
     * of the section, but the exact details are implementation specific.
     */
    public SerialWriter.Scope scope();

    public SharedPool pool();

    public interface Scope extends SerialWriter {
        /**
         * Close the current scoped section.
         * <p>
         * After this, the section should be guaranteed to have been written to its parent {@code
         * SerialWriter}.
         */
        @Override
        public void close() throws IOException;
    }
}
