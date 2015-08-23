package eu.toolchain.serializer.processor.unverified;

import javax.annotation.processing.Messager;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic;

import lombok.RequiredArgsConstructor;
import lombok.ToString;

@RequiredArgsConstructor
@ToString
public class BrokenAnnotationValue<T> extends AbstractVerified<T> {
    final String message;
    final Element element;
    final AnnotationMirror annotation;
    final AnnotationValue value;

    @Override
    public T get() {
        throw new IllegalStateException("Broken reference");
    }

    @Override
    public void writeError(Messager messager) {
        messager.printMessage(Diagnostic.Kind.ERROR, message, element, annotation, value);
    }

    @Override
    public boolean isVerified() {
        return false;
    }
}