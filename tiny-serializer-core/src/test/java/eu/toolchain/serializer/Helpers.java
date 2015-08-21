package eu.toolchain.serializer;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.mockito.stubbing.OngoingStubbing;

import eu.toolchain.serializer.io.OutputStreamSerialWriter;

public final class Helpers {
    public static <T> void roundtrip(Serializer<T> serializer, T value) throws IOException {
        final List<Integer> captured = new ArrayList<>();

        // test serialize
        {
            final SerialWriter out = new CapturingSerialWriter(captured);

            serializer.serialize(out, value);
        }

        // test deserialize
        {
            final SerialReader in = Mockito.mock(SerialReader.class);

            OngoingStubbing<Byte> stubbing = Mockito.when(in.read());

            for (int p : captured) {
                stubbing = stubbing.thenReturn((byte) (p & 0xff));
            }

            Assert.assertEquals(value, serializer.deserialize(in));
            Mockito.verify(in, Mockito.times(captured.size())).read();
        }
    }

    public static <T> void roundtripArray(Serializer<T> serializer, T value, final int[] pattern) throws IOException {
        // test serialize
        {
            final SerialWriter out = Mockito.mock(SerialWriter.class);
            serializer.serialize(out, value);
            final ArgumentCaptor<byte[]> captor = ArgumentCaptor.forClass(byte[].class);
            Mockito.verify(out).write(captor.capture());

            final byte[] captured = captor.getValue();

            for (int i = 0; i < pattern.length; ++i)
                Assert.assertEquals(String.format("pattern position#%d should match", i), (byte) pattern[i],
                        captured[i]);
        }

        // test deserialize
        {
            final SerialReader in = Mockito.mock(SerialReader.class);

            Mockito.doAnswer(new Answer<Void>() {
                @Override
                public Void answer(InvocationOnMock invocation) {
                    byte[] buffer = (byte[]) invocation.getArguments()[0];

                    for (int i = 0; i < pattern.length; i++)
                        buffer[i] = (byte) pattern[i];

                    return null;
                }
            }).when(in).read(Mockito.any(byte[].class));

            Assert.assertEquals(value, serializer.deserialize(in));
            Mockito.verify(in).read(Mockito.any(byte[].class));
        }
    }

    public static <T> byte[] serialize(Serializer<T> s, T value) throws IOException {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();
        s.serialize(new OutputStreamSerialWriter(out), value);
        return out.toByteArray();
    }
}
