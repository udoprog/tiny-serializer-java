package eu.toolchain.serializer.perftests.benchmarks;

import eu.toolchain.serializer.perftests.MutableSerializedObject;
import eu.toolchain.serializer.perftests.ObjectHelper;
import eu.toolchain.serializer.perftests.OutputStreamHelper;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.function.Supplier;
import org.msgpack.MessagePack;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;

@State(Scope.Benchmark)
public class MessagePackPerformance {
  final MutableSerializedObject object = ObjectHelper.newMutableSerializedObject();
  ;
  final OutputStream nullStream = OutputStreamHelper.newNullStream();

  final MessagePack msgpack = new MessagePack();

  final Supplier<InputStream> inputObject = ObjectHelper.supplyInputStreamFrom(() -> {
    return msgpack.write(object);
  });

  @Benchmark
  public void testSerializeToNull() throws Exception {
    msgpack.write(nullStream, object);
  }

  @Benchmark
  public void testSerializeToMemory(Blackhole bh) throws Exception {
    final ByteArrayOutputStream output = new ByteArrayOutputStream();
    msgpack.write(output, object);
  }

  @Benchmark
  public void testDeserializeFromMemory(Blackhole bh) throws Exception {
    bh.consume(msgpack.read(inputObject.get(), new MutableSerializedObject()));
  }
}
