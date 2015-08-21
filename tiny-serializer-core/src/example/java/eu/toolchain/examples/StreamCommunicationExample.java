package eu.toolchain.examples;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Optional;

import eu.toolchain.serializer.SerialReader;
import eu.toolchain.serializer.SerialWriter;
import eu.toolchain.serializer.Serializer;
import eu.toolchain.serializer.TinySerializer;
import eu.toolchain.serializer.io.InputStreamSerialReader;
import eu.toolchain.serializer.io.OutputStreamSerialWriter;

public class StreamCommunicationExample {
    public static void main(String[] argv) throws IOException, InterruptedException {
        final TinySerializer s = SerializerSetup.setup();

        // Sends each message like the following:
        // 0xde 0xad 0xbe 0xef
        // 0x01 (if absent = 0x00)
        // *string*
        final Serializer<Optional<String>> m = s.prefix(0xdeadbeef, s.optional(s.string()));

        final PipedOutputStream output = new PipedOutputStream();
        final PipedInputStream input = new PipedInputStream(output);

        final Thread t = new Thread() {
            @Override
            public void run() {
                try (final SerialReader in = new InputStreamSerialReader(input)) {
                    Optional<String> message;

                    while ((message = m.deserialize(in)).isPresent()) {
                        System.out.println("Thread Got: " + message.get());
                    }
                } catch (IOException e) {
                    e.printStackTrace(System.err);
                }
            }
        };

        t.start();

        try (final SerialWriter out = new OutputStreamSerialWriter(output)) {
            for (int i = 0; i < 100; i++) {
                m.serialize(out, Optional.of("message #" + i));
                out.flush();
                Thread.sleep(100);
            }

            m.serialize(out, Optional.empty());
        } catch (Exception e) {
            e.printStackTrace(System.err);
        }

        t.join();
    }
}