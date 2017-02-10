package eu.toolchain.serializer.processor;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic;

public final class Exceptions {
    public static BrokenException brokenElement(final String message, final Element element) {
        return new BrokenException(message, messager -> {
            messager.printMessage(Diagnostic.Kind.ERROR, message, element);
        });
    }

    public static BrokenException brokenAnnotation(
        final String message, final Element element, final AnnotationMirror annotation
    ) {
        return new BrokenException(message, messager -> {
            messager.printMessage(Diagnostic.Kind.ERROR, message, element, annotation);
        });
    }

    public static BrokenException brokenValue(
        final String message, final Element element, final AnnotationMirror annotation,
        final AnnotationValue value
    ) {
        return new BrokenException(message, messager -> {
            messager.printMessage(Diagnostic.Kind.ERROR, message, element, annotation, value);
        });
    }
}
