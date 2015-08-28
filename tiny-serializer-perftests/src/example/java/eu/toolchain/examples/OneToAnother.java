package eu.toolchain.examples;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.Set;

import com.google.common.collect.Sets;

import eu.toolchain.serializer.AutoSerialize;
import eu.toolchain.serializer.SerialReader;
import eu.toolchain.serializer.SerialWriter;
import eu.toolchain.serializer.Serializer;
import eu.toolchain.serializer.SerializerFramework;
import eu.toolchain.serializer.TinySerializer;
import eu.toolchain.serializer.io.ByteArraySerialReader;
import eu.toolchain.serializer.io.OutputStreamSerialWriter;
import lombok.Data;

public class OneToAnother {
    @AutoSerialize(fieldBased = true)
    @Data
    public static class One {
        private final String first;
        private final String caught;
        private final String last;

        @AutoSerialize.Field(name = "values")
        private final Optional<Set<Integer>> stuff;
    }

    @AutoSerialize(fieldBased = true)
    @Data
    public static class Another {
        @AutoSerialize.Field(name = "last")
        private final String caught;

        private final Optional<Set<Integer>> values;
    }

    public static void main(String argv[]) throws IOException {
        final ByteArrayOutputStream out = new ByteArrayOutputStream();

        final SerializerFramework f = TinySerializer.builder().build();

        final Serializer<One> one = new OneToAnother_One_Serializer(f);
        final Serializer<Another> another = new OneToAnother_Another_Serializer(f);

        final One o = new One("foo", "bar", "baz", Optional.of(Sets.newHashSet(12, 14, 15)));

        try (final SerialWriter writer = new OutputStreamSerialWriter(out)) {
            one.serialize(writer, o);
        }

        final Another a;

        try (final SerialReader reader = new ByteArraySerialReader(out.toByteArray())) {
            a = another.deserialize(reader);
        }

        System.out.println(a);
    }
}