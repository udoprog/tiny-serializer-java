package eu.toolchain.examples;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Optional;

import eu.toolchain.serializer.AutoSerialize;
import eu.toolchain.serializer.SerialReader;
import eu.toolchain.serializer.SerialWriter;
import eu.toolchain.serializer.Serializer;
import eu.toolchain.serializer.SerializerFramework;
import eu.toolchain.serializer.TinySerializer;
import lombok.Data;

public class SerializeToFile {
    @AutoSerialize
    @Data
    public static class Person {
        private final String name;
        private final Optional<Job> job;
    }

    @AutoSerialize
    @Data
    public static class Job {
        private final String name;
    }

    public static void main(String argv[]) throws IOException {
        final SerializerFramework f = TinySerializer.builder().build();

        final Serializer<Person> person = f.prefix(0xdeadbeef, new SerializeToFile_Person_Serializer(f));

        try (final SerialWriter w = f.writeStream(new FileOutputStream("person.bin"))) {
            person.serialize(w, new Person("John Doe", Optional.of(new Job("Designer"))));
            person.serialize(w, new Person("Jane Doe", Optional.of(new Job("Programmer"))));
            person.serialize(w, new Person("Jim Doe", Optional.empty()));
        }

        try (final SerialReader r = f.readStream(new FileInputStream("person.bin"))) {
            System.out.println(person.deserialize(r));
            System.out.println(person.deserialize(r));
            System.out.println(person.deserialize(r));
        }
    }
}