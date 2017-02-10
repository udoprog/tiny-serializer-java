package eu.toolchain.serializer;

import java.io.IOException;

public interface Serializer<T> {
  public void serialize(SerialWriter buffer, T value) throws IOException;

  public T deserialize(SerialReader buffer) throws IOException;
}
