package eu.toolchain.serializer;

import java.io.IOException;

public interface LengthPolicy {
    boolean check(long length) throws IOException;
}