package eu.toolchain.examples;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import eu.toolchain.serializer.SerialReader;
import eu.toolchain.serializer.TinySerializer;
import eu.toolchain.serializer.io.AbstractSerialReader;

public class SerializeCustomReaderExample {
    public static void main(String argv[]) throws IOException {
        final TinySerializer s = SerializerSetup.setup().build();

        final byte[] source = new byte[] { (byte) 0xf0, 0x7f, 0x00, 0x00 };

        final ByteArrayInputStream inputStream = new ByteArrayInputStream(source);

        final SerialReader reader = new AbstractSerialReader() {
            @Override
            public byte read() throws IOException {
                return (byte) inputStream.read();
            }

            @Override
            public void read(byte[] b) throws IOException {
                inputStream.read(b);
            }

            @Override
            public void skip(int length) throws IOException {
                inputStream.skip(length);
            }
        };

        int value = s.integer().deserialize(reader);

        System.out.println("result: " + value);
        System.out.println("equals?: " + (value == 0xf07f0000));
    }
}
