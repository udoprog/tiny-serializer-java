package eu.toolchain.serializer.perftests.tests;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;

import org.nustaq.serialization.FSTConfiguration;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;

import eu.toolchain.serializer.perftests.ObjectHelper;
import eu.toolchain.serializer.perftests.OutputStreamHelper;
import eu.toolchain.serializer.perftests.SerializableSerializedObject;

@State(Scope.Benchmark)
public class FSTPerformance {
    final SerializableSerializedObject object = ObjectHelper.newSerializableSerializedObject();
    final OutputStream nullStream = OutputStreamHelper.newNullStream();

    final FSTConfiguration conf = FSTConfiguration.createDefaultConfiguration();

    @Benchmark
    public void testSerializeToNull() throws Exception {
        conf.encodeToStream(nullStream, object);
    }

    @Benchmark
    public void testSerializeToMemory(Blackhole bh) throws Exception {
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        conf.encodeToStream(output, object);
        bh.consume(output.toByteArray());
    }
}