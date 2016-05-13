package eu.toolchain.serializer.processor.unverified;

import com.google.common.collect.ImmutableList;

import javax.annotation.processing.Messager;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import java.util.List;
import java.util.function.Function;

public interface Unverified<T> {
    T get();

    boolean isVerified();

    void writeError(Messager messager);

    <O> Unverified<O> map(Function<? super T, ? extends O> result);

    <O> Unverified<O> transform(Function<? super T, ? extends Unverified<O>> result);

    T orElse(T defaultValue);

    static <T> Unverified<T> verified(T reference) {
        return new Verified<>(reference);
    }

    static <T> Unverified<T> brokenElement(String message, Element element) {
        return new BrokenElement<>(message, element);
    }

    static <T> Unverified<T> brokenAnnotation(
        String message, Element element, AnnotationMirror annotation
    ) {
        return new BrokenAnnotation<>(message, element, annotation);
    }

    static <T> Unverified<T> brokenAnnotationValue(
        String message, Element element, AnnotationMirror annotation, AnnotationValue value
    ) {
        return new BrokenAnnotationValue<>(message, element, annotation, value);
    }

    static <T> Unverified<List<T>> combine(Iterable<? extends Unverified<T>> maybes) {
        return new AbstractVerified<List<T>>() {
            @Override
            public List<T> get() {
                final ImmutableList.Builder<T> result = ImmutableList.builder();

                for (final Unverified<T> m : maybes) {
                    result.add(m.get());
                }

                return result.build();
            }

            @Override
            public boolean isVerified() {
                boolean verified = true;

                for (final Unverified<?> m : maybes) {
                    verified = verified && m.isVerified();
                }

                return verified;
            }

            @Override
            public void writeError(Messager messager) {
                for (final Unverified<?> m : maybes) {
                    m.writeError(messager);
                }
            }
        };
    }

    static Unverified<?> combineDifferent(Unverified<?>... maybes) {
        return new AbstractVerified<Object>() {
            @Override
            public Object get() {
                return null;
            }

            @Override
            public boolean isVerified() {
                boolean verified = true;

                for (final Unverified<?> m : maybes) {
                    verified = verified && m.isVerified();
                }

                return verified;
            }

            @Override
            public void writeError(Messager messager) {
                for (final Unverified<?> m : maybes) {
                    m.writeError(messager);
                }
            }
        };
    }
}
