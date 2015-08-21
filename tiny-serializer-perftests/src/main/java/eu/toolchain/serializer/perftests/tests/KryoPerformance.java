package eu.toolchain.serializer.perftests.tests;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Output;

import eu.toolchain.serializer.perftests.ObjectHelper;
import eu.toolchain.serializer.perftests.OutputStreamHelper;
import eu.toolchain.serializer.perftests.SerializedObject;

@State(Scope.Benchmark)
public class KryoPerformance {
    final SerializedObject object = ObjectHelper.newSerializedObject();
    final OutputStream nullStream = OutputStreamHelper.newNullStream();

    final Kryo kryo = new Kryo();
    final Output kryoOutput = new Output(nullStream);

    @Benchmark
    public void testSerializeToNull() throws Exception {
        kryo.writeObject(kryoOutput, object);
    }

    @Benchmark
    public void testSerializeToMemory(Blackhole bh) throws Exception {
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        kryo.writeObject(new Output(output), object);
        bh.consume(output.toByteArray());
    }
}