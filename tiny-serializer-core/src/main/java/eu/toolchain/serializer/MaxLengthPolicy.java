package eu.toolchain.serializer;

import java.io.IOException;

import lombok.RequiredArgsConstructor;
import lombok.ToString;

@RequiredArgsConstructor
@ToString
public class MaxLengthPolicy implements LengthPolicy {
    final long length;

    @Override
    public boolean check(long length) throws IOException {
        return length <= this.length;
    }
}