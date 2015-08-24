package eu.toolchain.serializer.processor;

import eu.toolchain.serializer.AutoSerialize;

@AutoSerialize
public class IgnoreFinalAssigned {
    final String assigned = "hello";
}