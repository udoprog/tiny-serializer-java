package eu.toolchain.serializer;

import java.io.IOException;

import org.junit.Assert;
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

    @Test
    public void testDeserializeNonNull() throws IOException {
        final SerialReader in = Mockito.mock(SerialReader.class);
        @SuppressWarnings("unchecked")
        final Serializer<Object> inner = Mockito.mock(Serializer.class);
        final Serializer<Object> serializer = new NullSerializer<>(inner);

        Mockito.when(inner.deserialize(in)).thenReturn(reference);
        Mockito.when(in.read()).thenReturn((byte) NullSerializer.NOT_NULL);
        Assert.assertEquals(reference, serializer.deserialize(in));
        Mockito.verify(inner).deserialize(in);
    }

    @Test
    public void testDeserializeNull() throws IOException {
        final SerialReader in = Mockito.mock(SerialReader.class);
        @SuppressWarnings("unchecked")
        final Serializer<Object> inner = Mockito.mock(Serializer.class);
        final Serializer<Object> serializer = new NullSerializer<>(inner);

        Mockito.when(in.read()).thenReturn((byte) NullSerializer.NULL);
        Assert.assertEquals(null, serializer.deserialize(in));
        Mockito.verify(inner, Mockito.never()).deserialize(in);
    }
}
