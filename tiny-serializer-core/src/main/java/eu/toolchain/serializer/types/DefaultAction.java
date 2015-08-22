package eu.toolchain.serializer.types;

import java.io.IOException;

public interface DefaultAction<T> {
    T call() throws IOException;
}