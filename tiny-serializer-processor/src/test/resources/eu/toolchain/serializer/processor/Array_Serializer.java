package eu.toolchain.serializer.processor;

import eu.toolchain.serializer.SerialReader;
import eu.toolchain.serializer.SerialWriter;
import eu.toolchain.serializer.Serializer;
import eu.toolchain.serializer.SerializerFramework;
import java.io.IOException;
import javax.annotation.Generated;

@Generated("eu.toolchain.serializer.processor.AutoSerializeProcessor")
public final class Array_Serializer implements Serializer<Array> {
    final Serializer<boolean[]> s_BooleanArray;
    final Serializer<short[]> s_ShortArray;
    final Serializer<int[]> s_IntegerArray;
    final Serializer<long[]> s_LongArray;
    final Serializer<float[]> s_FloatArray;
    final Serializer<double[]> s_DoubleArray;
    final Serializer<Interface[]> s_InterfaceArray;
    final Serializer<int[][][]> s_IntegerArrayArrayArray;

    public Array_Serializer(final SerializerFramework framework) {
        s_BooleanArray = framework.booleanArray();
        s_ShortArray = framework.shortArray();
        s_IntegerArray = framework.intArray();
        s_LongArray = framework.longArray();
        s_FloatArray = framework.floatArray();
        s_DoubleArray = framework.doubleArray();
        s_InterfaceArray = framework.<Interface> array(new Interface_Serializer(framework), (s) -> new Interface[s]);
        s_IntegerArrayArrayArray = framework.<int[][]> array(framework.<int[]> array(framework.intArray(), (s) -> new int[s][]), (s) -> new int[s][][]);
    }

    @Override
    public void serialize(final SerialWriter buffer, final Array value) throws IOException {
        s_BooleanArray.serialize(buffer, value.getBooleans());
        s_ShortArray.serialize(buffer, value.getShorts());
        s_IntegerArray.serialize(buffer, value.getInts());
        s_LongArray.serialize(buffer, value.getLongs());
        s_FloatArray.serialize(buffer, value.getFloats());
        s_DoubleArray.serialize(buffer, value.getDoubles());
        s_InterfaceArray.serialize(buffer, value.getInterfaces());
        s_IntegerArrayArrayArray.serialize(buffer, value.getNested());
    }

    @Override
    public Array deserialize(final SerialReader buffer) throws IOException {
        final boolean[] v_booleans = s_BooleanArray.deserialize(buffer);
        final short[] v_shorts = s_ShortArray.deserialize(buffer);
        final int[] v_ints = s_IntegerArray.deserialize(buffer);
        final long[] v_longs = s_LongArray.deserialize(buffer);
        final float[] v_floats = s_FloatArray.deserialize(buffer);
        final double[] v_doubles = s_DoubleArray.deserialize(buffer);
        final Interface[] v_interfaces = s_InterfaceArray.deserialize(buffer);
        final int[][][] v_nested = s_IntegerArrayArrayArray.deserialize(buffer);
        return new Array(v_booleans, v_shorts, v_ints, v_longs, v_floats, v_doubles, v_interfaces, v_nested);
    }
}
