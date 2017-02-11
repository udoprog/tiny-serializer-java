package eu.toolchain.serializer;

import java.io.IOException;

public interface Serializer<T> {
  void serialize(SerialWriter buffer, T value) throws IOException;

  T deserialize(SerialReader buffer) throws IOException;

  /**
   * @return the constant size of a serializer
   */
  default int size() {
    return -1;
  }
}
