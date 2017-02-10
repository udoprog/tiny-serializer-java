package eu.toolchain.serializer.perftests.benchmarks;

import eu.toolchain.serializer.perftests.MutableSerializedObject;
import eu.toolchain.serializer.perftests.ObjectHelper;
import eu.toolchain.serializer.perftests.OutputStreamHelper;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.function.Supplier;
import org.nustaq.serialization.FSTConfiguration;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;

@State(Scope.Benchmark)
public class FSTPerformance {
  final MutableSerializedObject object = ObjectHelper.newMutableSerializedObject();
  final OutputStream nullStream = OutputStreamHelper.newNullStream();

  final FSTConfiguration conf = FSTConfiguration.createDefaultConfiguration();

  final Supplier<InputStream> inputObject = ObjectHelper.supplyInputStreamFrom(() -> {
    try (final ByteArrayOutputStream output = new ByteArrayOutputStream()) {
      conf.encodeToStream(output, object);
      return output.toByteArray();
    }
  });

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

  @Benchmark
  public void testDeserializeFromMemory(Blackhole bh) throws Exception {
    bh.consume(conf.decodeFromStream(inputObject.get()));
  }
}
