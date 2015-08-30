package eu.toolchain.serializer.array;

import java.io.IOException;

import eu.toolchain.serializer.SerialReader;
import eu.toolchain.serializer.SerialWriter;
import eu.toolchain.serializer.Serializer;
import eu.toolchain.serializer.types.LongSerializer;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class DoubleArraySerializer implements Serializer<double[]> {
    private static final int BYTES = 8;

    private final Serializer<Integer> size;

    @Override
    public void serialize(SerialWriter buffer, double[] value) throws IOException {
        final int size = value.length * BYTES;
        final byte[] bytes = new byte[size];

        for (int i = 0; i < value.length; i++) {
            LongSerializer.toBytes(Double.doubleToLongBits(value[i]), bytes, i * BYTES);
        }

        this.size.serialize(buffer, value.length);
        buffer.write(bytes);
    }

    @Override
    public double[] deserialize(SerialReader buffer) throws IOException {
        final int length = size.deserialize(buffer);

        final byte[] bytes = new byte[length * BYTES];
        final double[] value = new double[length];

        buffer.read(bytes);

        for (int i = 0; i < value.length; i++) {
            value[i] = Double.longBitsToDouble(LongSerializer.fromBytes(bytes, i * BYTES));
        }

        return value;
    }
}