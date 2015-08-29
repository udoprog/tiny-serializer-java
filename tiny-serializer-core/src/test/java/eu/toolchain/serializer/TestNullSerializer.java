package eu.toolchain.serializer;

import java.io.IOException;

import org.junit.Test;
import org.mockito.Mockito;

import eu.toolchain.serializer.types.NullSerializer;

public class TestNullSerializer {
    final Object reference = new Object();

    @Test
    public void testSerializeNonNull() throws IOException {
        final SerialWriter out = Mockito.mock(SerialWriter.class);
        @SuppressWarnings("unchecked")
        final Serializer<Object> inner = Mockito.mock(Serializer.class);
        final Serializer<Object> serializer = new NullSerializer<>(inner);

        serializer.serialize(out, reference);
        Mockito.verify(out).write(NullSerializer.NOT_NULL);
        Mockito.verify(inner).serialize(out, reference);
    }

    @Test
    public void testSerializeNull() throws IOException {
        final SerialWriter out = Mockito.mock(SerialWriter.class);
        @SuppressWarnings("unchecked")
        final Serializer<Object> inner = Mockito.mock(Serializer.class);
        final Serializer<Object> serializer = new NullSerializer<>(inner);

        serializer.serialize(out, null);
        Mockito.verify(out).write(NullSerializer.NULL);
        Mockito.verify(inner, Mockito.never()).serialize(out, reference);
    }
}