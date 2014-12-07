package eu.toolchain.serializer.io;

import java.io.IOException;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ByteArraySerialReader extends AbstractSerialReader {
    private final byte[] source;

    private int p = 0;

    @Override
    public byte read() throws IOException {
        if (p + 1 > source.length)
            throw new IOException("end of buffer");

        return source[p++];
    }

    @Override
    public void read(byte[] b) throws IOException {
        if (p + b.length > source.length)
            throw new IOException("end of buffer");

        System.arraycopy(source, p, b, 0, b.length);
        p += b.length;
    }

    @Override
    public void skip(int size) throws IOException {
        if (p + size > source.length)
            throw new IOException("end of buffer");

        p += size;
    }
}