package eu.toolchain.examples;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import eu.toolchain.serializer.HexUtils;
import eu.toolchain.serializer.SerialWriter;
import eu.toolchain.serializer.TinySerializer;
import eu.toolchain.serializer.io.AbstractSerialWriter;

public class SerializeCustomWriterExample {
    public static void main(String argv[]) throws IOException {
        final TinySerializer s = SerializerSetup.setup().build();

        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        final SerialWriter writer = new AbstractSerialWriter() {
            @Override
            public void write(byte[] bytes, int offset, int length) throws IOException {
                outputStream.write(bytes, offset, length);
            }

            @Override
            public void write(int b) throws IOException {
                outputStream.write(b);
            }

            @Override
            public void flush() throws IOException {
                outputStream.flush();
            }

            @Override
            public void close() throws IOException {
                outputStream.close();
            }
        };

        s.integer().serialize(writer, 0xf07f0000);
        final byte[] result = outputStream.toByteArray();

        System.out.println("result: " + HexUtils.toHex(result));
    }
}
