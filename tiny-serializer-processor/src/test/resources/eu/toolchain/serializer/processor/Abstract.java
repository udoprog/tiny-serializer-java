package eu.toolchain.serializer.processor;

import eu.toolchain.serializer.AutoSerialize;

@AutoSerialize
@AutoSerialize.SubTypes({ @AutoSerialize.SubType(value = ImplA.class), @AutoSerialize.SubType(value = ImplB.class) })
public abstract class Abstract implements Interface {
    public int compareTo(Interface o) {
        return 0;
    }
}