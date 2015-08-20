package eu.toolchain.serializer.processor;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;

import eu.toolchain.serializer.AutoSerialize;

@AutoSerialize
public class Ordering {
    @AutoSerialize.Field(id = 3, constructorOrder = 3)
    final String a;
    @AutoSerialize.Field(id = 2, constructorOrder = 1)
    final String b;
    @AutoSerialize.Field(id = 1, constructorOrder = 2)
    final String c;

    public Ordering(String b, String c, String a) {
        this.b = b;
        this.c = c;
        this.a = a;
    }

    public String getA() {
        return a;
    }

    public String getB() {
        return b;
    }

    public String getC() {
        return c;
    }
}