package eu.toolchain.serializer;

import java.io.IOException;

public interface LengthPolicy {
  /**
   * Check the given length.
   *
   * @param length The length to check.
   * @return {@code true} if the length is OK, {@code false} otherwise.
   * @throws IOException If an IOException occurs during policy checking.
   */
  boolean check(long length) throws IOException;
}
