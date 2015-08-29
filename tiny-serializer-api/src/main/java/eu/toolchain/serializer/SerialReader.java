package eu.toolchain.serializer;

import java.io.Closeable;
import java.io.IOException;

public interface SerialReader extends Closeable {
    /**
     * Read an array of bytes.
     *
     * @param b Array to add read bytes to.
     * @throws IOException When bytes cannot be read.
     */
    void read(byte[] b) throws IOException;

    /**
     * Read an array of bytes with the given offset and length.
     *
     * @param bytes The array to read into.
     * @param offset The offset to read to.
     * @param length The number of bytes to read.
     */
    void read(byte[] bytes, int offset, int length) throws IOException;

    /**
     * Skip the given amount of bytes.
     *
     * @param length Number of bytes to skip.
     * @throws IOException When data cannot be skipped.
     */
    void skip(int length) throws IOException;

    /**
     * Skip a scoped section.
     *
     * @throws IOException If section could not be skipped.
     */
    void skip() throws IOException;

    /**
     * Retrieve the reader for a scoped section.
     *
     * Scopes sections are sections that can be skipped. They are typically prefixed with the length of the section, but
     * the exact details are implementation specific.
     *
     * @see #skip() for how to skip scoped sections.
     */
    SerialReader scope() throws IOException;
}
