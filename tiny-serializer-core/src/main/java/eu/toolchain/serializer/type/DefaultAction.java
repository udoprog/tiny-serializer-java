package eu.toolchain.serializer.type;

import java.io.IOException;

public interface DefaultAction<T> {
  T call() throws IOException;
}
