package eu.toolchain.serializer.perftests.benchmarks;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.function.Supplier;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;

import eu.toolchain.serializer.SerialWriter;
import eu.toolchain.serializer.Serializer;
import eu.toolchain.serializer.TinySerializer;
import eu.toolchain.serializer.io.InputStreamSerialReader;
import eu.toolchain.serializer.io.OutputStreamSerialWriter;
import eu.toolchain.serializer.perftests.ImmutableSerializedObject;
import eu.toolchain.serializer.perftests.ImmutableSerializedObject_Serializer;
import eu.toolchain.serializer.perftests.ObjectHelper;
import eu.toolchain.serializer.perftests.OutputStreamHelper;

@State(Scope.Benchmark)
public class TinyPerformance {
    final ImmutableSerializedObject object = ObjectHelper.newSerializedObject();;
    final OutputStream nullStream = OutputStreamHelper.newNullStream();

    final TinySerializer tiny = TinySerializer.builder().build();
    final Serializer<ImmutableSerializedObject> serializer = new ImmutableSerializedObject_Serializer(tiny);
    final SerialWriter writer = new OutputStreamSerialWriter(nullStream);

    final Supplier<InputStream> inputObject = ObjectHelper.supplyInputStreamFrom(() -> {
        try (final ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            final SerialWriter out = new OutputStreamSerialWriter(output);
            serializer.serialize(out, object);
            out.flush();
            return output.toByteArray();
        }
    });

    @Benchmark
    public void testSerializeToNull() throws Exception {
        serializer.serialize(writer, object);
    }

    @Benchmark
    public void testSerializeToMemory(Blackhole bh) throws Exception {
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        final SerialWriter out = new OutputStreamSerialWriter(output);
        serializer.serialize(out, object);
        out.flush();
        bh.consume(output.toByteArray());
    }

    @Benchmark
    public void testDeserializeFromMemory(Blackhole bh) throws Exception {
        bh.consume(serializer.deserialize(new InputStreamSerialReader(inputObject.get())));
    }
}