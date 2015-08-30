package eu.toolchain.examples;

import java.io.IOException;

import eu.toolchain.serializer.Serializer;
import eu.toolchain.serializer.TinySerializer;

public class SerializePrimitiveExample {
    public static void main(String[] argv) throws IOException {
        final TinySerializer s = SerializerSetup.setup().build();

        final Serializer<Integer> i = s.fixedInteger();
        final Serializer<Long> l = s.fixedLong();

        System.out.println("result: " + s.deserialize(i, s.serialize(i, Integer.MIN_VALUE)));
        System.out.println("result: " + s.deserialize(i, s.serialize(i, Integer.MAX_VALUE)));
        System.out.println("result: " + s.deserialize(l, s.serialize(l, Long.MIN_VALUE)));
        System.out.println("result: " + s.deserialize(l, s.serialize(l, Long.MAX_VALUE)));
    }
}
