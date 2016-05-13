package eu.toolchain.serializer.processor.unverified;

import lombok.RequiredArgsConstructor;
import lombok.ToString;

import javax.annotation.processing.Messager;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic;

@RequiredArgsConstructor
@ToString
public class BrokenAnnotation<T> extends AbstractVerified<T> {
    final String message;
    final Element element;
    final AnnotationMirror annotation;

    @Override
    public T get() {
        throw new IllegalStateException("Broken reference");
    }

    @Override
    public void writeError(Messager messager) {
        messager.printMessage(Diagnostic.Kind.ERROR, message, element, annotation);
    }

    @Override
    public boolean isVerified() {
        return false;
    }
}
