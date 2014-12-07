package eu.toolchain.serializer.io;

import java.nio.ByteBuffer;

import org.junit.Assert;
import org.junit.Test;

public class ByteBufferOutputStreamTest {
    @Test
    public void testWriteFF() {
        final ByteBufferOutputStream output = new ByteBufferOutputStream();
        simpleInteraction(output);
    }

    @Test
    public void testWriteResumse() {
        final ByteBufferOutputStream output = new ByteBufferOutputStream();
        complicatedInteraction(output);
    }

    /**
     * Tests that the use of Math.ceil asserts that it at least grows with one element at a time.
     */
    @Test
    public void testTinyGrowthFactor() {
        final ByteBufferOutputStream output = new ByteBufferOutputStream(1, 1.000001f);
        simpleInteraction(output);
    }

    private void simpleInteraction(final ByteBufferOutputStream output) {
        for (int i = 0; i < 0xff; i++)
            output.write(i % 0xff);

        final ByteBuffer result = output.buffer();

        for (int i = 0; i < 0xff; i++)
            Assert.assertEquals((byte) (i % 0xff), result.get());
    }

    private void complicatedInteraction(final ByteBufferOutputStream output) {
        for (int i = 0; i < 0xff; i++)
            output.write(i % 0xff);

        {
            final ByteBuffer result = output.buffer();

            for (int i = 0; i < 0xff; i++)
                Assert.assertEquals((byte) (i % 0xff), result.get());
        }

        for (int i = 0; i < 0xff; i++)
            output.write((i + 10) % 0xff);

        {
            final ByteBuffer result = output.buffer();

            for (int i = 0; i < 0xff; i++)
                Assert.assertEquals((byte) (i % 0xff), result.get());

            for (int i = 0; i < 0xff; i++)
                Assert.assertEquals((byte) ((i + 10) % 0xff), result.get());
        }
    }
}
