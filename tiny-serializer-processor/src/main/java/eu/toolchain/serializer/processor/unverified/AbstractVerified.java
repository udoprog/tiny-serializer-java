package eu.toolchain.serializer.processor.unverified;

import javax.annotation.processing.Messager;
import java.util.function.Function;

public abstract class AbstractVerified<T> implements Unverified<T> {
    @Override
    public <O> Unverified<O> map(Function<? super T, ? extends O> result) {
        if (isVerified()) {
            return Unverified.verified(result.apply(get()));
        }

        return new AbstractVerified<O>() {
            @Override
            public O get() {
                throw new IllegalStateException("broken");
            }

            @Override
            public boolean isVerified() {
                return false;
            }

            @Override
            public void writeError(Messager messager) {
                AbstractVerified.this.writeError(messager);
            }
        };
    }

    @Override
    public <O> Unverified<O> transform(Function<? super T, ? extends Unverified<O>> result) {
        if (isVerified()) {
            return result.apply(get());
        }

        return new AbstractVerified<O>() {
            @Override
            public O get() {
                throw new IllegalStateException("broken");
            }

            @Override
            public boolean isVerified() {
                return false;
            }

            @Override
            public void writeError(Messager messager) {
                AbstractVerified.this.writeError(messager);
            }
        };
    }

    @Override
    public T orElse(T defaultValue) {
        if (isVerified()) {
            return get();
        }

        return defaultValue;
    }
}
