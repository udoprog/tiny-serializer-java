package eu.toolchain.serializer.type;

import eu.toolchain.serializer.SerialReader;
import eu.toolchain.serializer.SerialWriter;
import eu.toolchain.serializer.Serializer;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class StringEnumSerializer<T extends Enum<T>> implements Serializer<T> {
  private final Serializer<String> string;
  private final DefaultAction<T> defaultAction;
  private final Map<String, T> mapping;

  public StringEnumSerializer(
    Serializer<String> string, T[] values, DefaultAction<T> defaultAction
  ) {
    this.string = string;
    this.defaultAction = defaultAction;
    this.mapping = buildNameMapping(values);
  }

  private Map<String, T> buildNameMapping(T[] values) {
    final Map<String, T> mapping = new HashMap<>();

    for (final T value : values) {
      mapping.put(value.name(), value);
    }

    return mapping;
  }

  @Override
  public void serialize(SerialWriter buffer, T value) throws IOException {
    this.string.serialize(buffer, value.name());
  }

  @Override
  public T deserialize(SerialReader buffer) throws IOException {
    final T result = mapping.get(this.string.deserialize(buffer));

    if (result == null) {
      return defaultAction.call();
    }

    return result;
  }
}
