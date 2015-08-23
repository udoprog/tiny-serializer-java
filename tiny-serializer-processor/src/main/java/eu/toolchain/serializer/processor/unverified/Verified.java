package eu.toolchain.serializer.processor.unverified;

import javax.annotation.processing.Messager;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@RequiredArgsConstructor
@ToString
@EqualsAndHashCode(of={"reference"}, callSuper = false)
public class Verified<T> extends AbstractVerified<T> {
    final T reference;

    public T get() {
        return reference;
    }

    @Override
    public boolean isVerified() {
        return true;
    }

    @Override
    public void writeError(Messager messager) {
    }
}