package eu.toolchain.serializer.array;

import static org.junit.Assert.assertEquals;

import java.io.IOException;

import org.junit.Before;
import org.junit.Test;

import eu.toolchain.serializer.Helpers;
import eu.toolchain.serializer.Serializer;
import eu.toolchain.serializer.primitive.CompactVarIntSerializer;

public class ArraySerializerTest {
    private Serializer<boolean[]> booleanArray;
    private Serializer<short[]> shortArray;
    private Serializer<int[]> integerArray;
    private Serializer<long[]> longArray;
    private Serializer<float[]> floatArray;
    private Serializer<double[]> doubleArray;

    @Before
    public void setup() {
        booleanArray = new BooleanArraySerializer(new CompactVarIntSerializer());
        shortArray = new ShortArraySerializer(new CompactVarIntSerializer());
        integerArray = new IntegerArraySerializer(new CompactVarIntSerializer());
        longArray = new LongArraySerializer(new CompactVarIntSerializer());

        floatArray = new FloatArraySerializer(new CompactVarIntSerializer());
        doubleArray = new DoubleArraySerializer(new CompactVarIntSerializer());
    }

    @Test
    public void testBoolean() throws IOException {
        assertEquals(1, Helpers.roundtripArray(booleanArray, new boolean[] {}).getCaptured().size());

        assertEquals(2, Helpers.roundtripArray(booleanArray, new boolean[] { true, false, true }).getCaptured().size());

        assertEquals(3,
                Helpers.roundtripArray(booleanArray,
                        new boolean[] { true, false, true, false, true, false, true, false, true, true, false, false })
                .getCaptured().size());
    }

    @Test
    public void testShort() throws IOException {
        assertEquals(1, Helpers.roundtripArray(shortArray, new short[] {}).getCaptured().size());
        assertEquals(3, Helpers.roundtripArray(shortArray, new short[] { -1 }).getCaptured().size());
        assertEquals(3, Helpers.roundtripArray(shortArray, new short[] { Short.MIN_VALUE }).getCaptured().size());
        assertEquals(3, Helpers.roundtripArray(shortArray, new short[] { Short.MAX_VALUE }).getCaptured().size());
    }

    @Test
    public void testInteger() throws IOException {
        assertEquals(1, Helpers.roundtripArray(integerArray, new int[] {}).getCaptured().size());
        assertEquals(5, Helpers.roundtripArray(integerArray, new int[] { -1 }).getCaptured().size());
        assertEquals(5, Helpers.roundtripArray(integerArray, new int[] { Integer.MIN_VALUE }).getCaptured().size());
        assertEquals(5, Helpers.roundtripArray(integerArray, new int[] { Integer.MAX_VALUE }).getCaptured().size());
    }

    @Test
    public void testLong() throws IOException {
        assertEquals(1, Helpers.roundtripArray(longArray, new long[] {}).getCaptured().size());
        assertEquals(9, Helpers.roundtripArray(longArray, new long[] { -1 }).getCaptured().size());
        assertEquals(9, Helpers.roundtripArray(longArray, new long[] { Long.MIN_VALUE }).getCaptured().size());
        assertEquals(9, Helpers.roundtripArray(longArray, new long[] { Long.MAX_VALUE }).getCaptured().size());
    }


    @Test
    public void testFloat() throws IOException {
        assertEquals(1, Helpers.roundtripArray(floatArray, new float[] {}).getCaptured().size());
        assertEquals(5, Helpers.roundtripArray(floatArray, new float[] { -1f }).getCaptured().size());
        assertEquals(5, Helpers.roundtripArray(floatArray, new float[] { Float.MIN_VALUE }).getCaptured().size());
        assertEquals(5, Helpers.roundtripArray(floatArray, new float[] { Float.MAX_VALUE }).getCaptured().size());
    }

    @Test
    public void testDouble() throws IOException {
        assertEquals(1, Helpers.roundtripArray(doubleArray, new double[] {}).getCaptured().size());
        assertEquals(9, Helpers.roundtripArray(doubleArray, new double[] { -1.0d }).getCaptured().size());
        assertEquals(9, Helpers.roundtripArray(doubleArray, new double[] { Double.MIN_VALUE }).getCaptured().size());
        assertEquals(9, Helpers.roundtripArray(doubleArray, new double[] { Double.MAX_VALUE }).getCaptured().size());
    }
}