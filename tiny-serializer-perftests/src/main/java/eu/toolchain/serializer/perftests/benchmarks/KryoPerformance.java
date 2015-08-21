package eu.toolchain.serializer.perftests.benchmarks;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.function.Supplier;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

import eu.toolchain.serializer.perftests.MutableSerializedObject;
import eu.toolchain.serializer.perftests.ObjectHelper;
import eu.toolchain.serializer.perftests.OutputStreamHelper;

@State(Scope.Benchmark)
public class KryoPerformance {
    final MutableSerializedObject object = ObjectHelper.newMutableSerializedObject();
    final OutputStream nullStream = OutputStreamHelper.newNullStream();

    final Kryo kryo = new Kryo();
    final Output kryoOutput = new Output(nullStream);

    final Supplier<InputStream> inputObject = ObjectHelper.supplyInputStreamFrom(() -> {
        try (final ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            try (final Output out = new Output(output)) {
                kryo.writeObject(out, object);
            }

            return output.toByteArray();
        }
    });

    @Benchmark
    public void testSerializeToNull() throws Exception {
        kryo.writeObject(kryoOutput, object);
    }

    @Benchmark
    public void testSerializeToMemory(Blackhole bh) throws Exception {
        final ByteArrayOutputStream output = new ByteArrayOutputStream();

        try (final Output out = new Output(output)) {
            kryo.writeObject(out, object);
        }

        bh.consume(output.toByteArray());
    }

    @Benchmark
    public void testDeserializeFromMemory(Blackhole bh) throws Exception {
        bh.consume(kryo.readObject(new Input(inputObject.get()), MutableSerializedObject.class));
    }
}