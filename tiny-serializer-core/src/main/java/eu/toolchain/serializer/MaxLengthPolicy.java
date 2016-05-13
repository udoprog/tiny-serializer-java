package eu.toolchain.serializer;

import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.io.IOException;

@RequiredArgsConstructor
@ToString
public class MaxLengthPolicy implements LengthPolicy {
    final long length;

    @Override
    public boolean check(long length) throws IOException {
        return length <= this.length;
    }
}
