package eu.toolchain.examples;

import com.google.common.collect.ImmutableMap;
import eu.toolchain.serializer.AutoSerialize;
import eu.toolchain.serializer.BytesSerialWriter;
import eu.toolchain.serializer.SerialReader;
import eu.toolchain.serializer.Serializer;
import eu.toolchain.serializer.SerializerFramework;
import eu.toolchain.serializer.TinySerializer;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.Data;

public class DynamicRecords {
  public enum RecordType {
    RESOURCE_TAGS
  }

  @AutoSerialize
  @Data
  static class Record {
    private final RecordType type;
    private final ByteBuffer data;
  }

  public static void main(String argv[]) throws IOException {
    final SerializerFramework f = TinySerializer.builder().build();

    final Serializer<Map<String, String>> resourceTags = f.map(f.string(), f.string());
    final Serializer<List<Record>> records = f.list(new DynamicRecords_Record_Serializer(f));

    final ByteBuffer data;

    try (final BytesSerialWriter out = f.writeBytes()) {
      final List<Record> values = new ArrayList<>();

      values.add(new Record(RecordType.RESOURCE_TAGS,
        f.serialize(resourceTags, ImmutableMap.of("foo", "bar", "bar", "baz"))));

      records.serialize(out, values);
      data = out.toByteBuffer();
    }

    try (final SerialReader reader = f.readByteBuffer(data)) {
      final List<Record> values = records.deserialize(reader);

      for (final Record r : values) {
        switch (r.getType()) {
          case RESOURCE_TAGS:
            final Map<String, String> tags = f.deserialize(resourceTags, r.getData());
            System.out.println("resource tags = " + tags);
            break;
        }
      }
    }
  }
}
