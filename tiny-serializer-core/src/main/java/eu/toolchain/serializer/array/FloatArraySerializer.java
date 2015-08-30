package eu.toolchain.serializer.array;

import java.io.IOException;

import eu.toolchain.serializer.SerialReader;
import eu.toolchain.serializer.SerialWriter;
import eu.toolchain.serializer.Serializer;
import eu.toolchain.serializer.types.IntegerSerializer;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class FloatArraySerializer implements Serializer<float[]> {
    private static final int BYTES = 4;

    private final Serializer<Integer> size;

    @Override
    public void serialize(SerialWriter buffer, float[] value) throws IOException {
        final int size = value.length * BYTES;
        final byte[] bytes = new byte[size];

        for (int i = 0; i < value.length; i++) {
            IntegerSerializer.toBytes(Float.floatToIntBits(value[i]), bytes, i * BYTES);
        }

        this.size.serialize(buffer, value.length);
        buffer.write(bytes);
    }

    @Override
    public float[] deserialize(SerialReader buffer) throws IOException {
        final int length = size.deserialize(buffer);

        final byte[] bytes = new byte[length * BYTES];
        final float[] value = new float[length];

        buffer.read(bytes);

        for (int i = 0; i < value.length; i++) {
            value[i] = Float.intBitsToFloat(IntegerSerializer.fromBytes(bytes, i * BYTES));
        }

        return value;
    }
}