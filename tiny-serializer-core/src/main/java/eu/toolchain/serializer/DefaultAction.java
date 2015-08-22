package eu.toolchain.serializer;

import java.io.IOException;

public interface DefaultAction<T> {
    T call() throws IOException;
}