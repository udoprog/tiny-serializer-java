package eu.toolchain.serializer.processor.unverified;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import javax.annotation.processing.Messager;

@RequiredArgsConstructor
@ToString
@EqualsAndHashCode(of = {"reference"}, callSuper = false)
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
