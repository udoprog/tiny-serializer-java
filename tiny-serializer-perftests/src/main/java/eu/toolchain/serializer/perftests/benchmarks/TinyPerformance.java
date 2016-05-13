package eu.toolchain.serializer.perftests.benchmarks;

import eu.toolchain.serializer.BytesSerialWriter;
import eu.toolchain.serializer.SerialReader;
import eu.toolchain.serializer.SerialWriter;
import eu.toolchain.serializer.Serializer;
import eu.toolchain.serializer.TinySerializer;
import eu.toolchain.serializer.io.CoreOutputStreamSerialWriter;
import eu.toolchain.serializer.perftests.ImmutableSerializedObject;
import eu.toolchain.serializer.perftests.ImmutableSerializedObject_Serializer;
import eu.toolchain.serializer.perftests.ObjectHelper;
import eu.toolchain.serializer.perftests.OutputStreamHelper;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.function.Supplier;

@State(Scope.Benchmark)
public class TinyPerformance {
    final ImmutableSerializedObject object = ObjectHelper.newSerializedObject();
    ;
    final OutputStream nullStream = OutputStreamHelper.newNullStream();

    final TinySerializer tiny = TinySerializer.builder().build();
    final Serializer<ImmutableSerializedObject> serializer =
        new ImmutableSerializedObject_Serializer(tiny);
    final SerialWriter writer = new CoreOutputStreamSerialWriter(nullStream);

    final Supplier<InputStream> inputObject = ObjectHelper.supplyInputStreamFrom(() -> {
        try (final BytesSerialWriter writer = tiny.writeBytes()) {
            serializer.serialize(writer, object);
            return writer.toByteArray();
        }
    });

    @Benchmark
    public void testSerializeToNull() throws Exception {
        serializer.serialize(writer, object);
    }

    @Benchmark
    public void testSerializeToMemory(Blackhole bh) throws Exception {
        try (final BytesSerialWriter writer = tiny.writeBytes()) {
            serializer.serialize(writer, object);
            bh.consume(writer.toByteArray());
        }
    }

    @Benchmark
    public void testDeserializeFromMemory(Blackhole bh) throws Exception {
        try (final SerialReader reader = tiny.readStream(inputObject.get())) {
            bh.consume(serializer.deserialize(reader));
        }
    }
}
