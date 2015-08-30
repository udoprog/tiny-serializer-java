package eu.toolchain.serializer.types;

import java.io.IOException;

import eu.toolchain.serializer.SerialReader;
import eu.toolchain.serializer.SerialWriter;
import eu.toolchain.serializer.Serializer;

public class DoubleSerializer implements Serializer<Double> {
    @Override
    public void serialize(SerialWriter buffer, Double value) throws IOException {
        final byte[] bytes = new byte[8];
        LongSerializer.toBytes(Double.doubleToLongBits(value), bytes, 0);
        buffer.write(bytes);
    }

    @Override
    public Double deserialize(SerialReader buffer) throws IOException {
        final byte[] b = new byte[8];
        buffer.read(b);
        return Double.longBitsToDouble(LongSerializer.fromBytes(b, 0));
    }
}