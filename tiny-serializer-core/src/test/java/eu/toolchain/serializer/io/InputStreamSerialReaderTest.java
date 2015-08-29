package eu.toolchain.serializer.io;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import eu.toolchain.serializer.Serializer;

@RunWith(MockitoJUnitRunner.class)
public class InputStreamSerialReaderTest {
    @Mock
    InputStream input;

    @Mock
    Serializer<Integer> scopeSize;

    CoreInputStreamSerialReader reader;

    @Before
    public void setup() {
        reader = new CoreInputStreamSerialReader(input);
    }

    @Test
    public void testReadBytes() throws IOException {
        final byte[] bytes = new byte[0];

        doReturn(15).when(input).read(bytes, 0, 20);
        doReturn(5).when(input).read(bytes, 15, 5);

        reader.read(bytes, 0, 20);

        final InOrder order = inOrder(input);
        order.verify(input).read(bytes, 0, 20);
        order.verify(input).read(bytes, 15, 5);
    }

    @Test(expected = EOFException.class)
    public void testReadBytesThrows() throws IOException {
        final byte[] bytes = new byte[0];

        doReturn(-1).when(input).read(bytes, 0, 10);
        reader.read(bytes, 0, 10);
    }

    @Test
    public void testSkip() throws IOException {
        doReturn(5L).when(input).skip(10);
        doReturn(5L).when(input).skip(5);

        reader.skip(10);

        final InOrder order = inOrder(input);
        order.verify(input).skip(10);
        order.verify(input).skip(5);
    }

    @Test
    public void testClose() throws IOException {
        doNothing().when(input).close();
        reader.close();
        verify(input).close();
    }
}