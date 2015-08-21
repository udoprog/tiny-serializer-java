package eu.toolchain.serializer.perftests;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;

public class ObjectHelper {
    public static SerializedObject newSerializedObject() {
        return new SerializedObject(42, "hello world", ImmutableMap.of("hello", "world", "this", "sucks"),
                ImmutableList.of("fee", "fii", "foo", "fum"));
    }

    public static SerializableSerializedObject newSerializableSerializedObject() {
        return new SerializableSerializedObject(42, "hello world", ImmutableMap.of("hello", "world", "this", "sucks"),
                ImmutableList.of("fee", "fii", "foo", "fum"));
    }
}