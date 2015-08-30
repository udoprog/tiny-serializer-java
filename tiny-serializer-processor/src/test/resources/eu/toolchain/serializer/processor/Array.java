package eu.toolchain.serializer.processor;

import eu.toolchain.serializer.AutoSerialize;

@AutoSerialize
public class Array {
    final boolean[] booleans;
    final short[] shorts;
    final int[] ints;
    final long[] longs;
    final float[] floats;
    final double[] doubles;
    final Interface[] interfaces;
    final int[][][] nested;

    public Array(boolean[] booleans, short[] shorts, int[] ints, long[] longs, float[] floats, double[] doubles,
            Interface[] interfaces, int[][][] nested) {
        this.booleans = booleans;
        this.shorts = shorts;
        this.ints = ints;
        this.longs = longs;
        this.floats = floats;
        this.doubles = doubles;
        this.interfaces = interfaces;
        this.nested = nested;
    }

    boolean[] getBooleans() {
        return booleans;
    }

    short[] getShorts() {
        return shorts;
    }

    int[] getInts() {
        return ints;
    }

    long[] getLongs() {
        return longs;
    }

    float[] getFloats() {
        return floats;
    }

    double[] getDoubles() {
        return doubles;
    }

    Interface[] getInterfaces() {
        return interfaces;
    }

    int[][][] getNested() {
        return nested;
    }
}