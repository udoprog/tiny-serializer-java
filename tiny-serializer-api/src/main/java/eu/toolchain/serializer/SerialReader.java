package eu.toolchain.serializer;

import java.io.Closeable;
import java.io.IOException;

public interface SerialReader extends Closeable {
    /**
     * Read a single byte.
     *
     * @return The read byte.
     * @throws IOException When byte cannot be read.
     */
    byte read() throws IOException;

    /**
     * Read an array of bytes.
     *
     * @param b Array to add read bytes to.
     * @throws IOException When bytes cannot be read.
     */
    void read(byte[] b) throws IOException;

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
