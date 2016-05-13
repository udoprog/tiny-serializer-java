package eu.toolchain.serializer.processor;

import lombok.RequiredArgsConstructor;

import javax.annotation.processing.Messager;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic.Kind;

@RequiredArgsConstructor
public class PrefixingMessager implements Messager {
    private final String prefix;
    private final Messager delegate;

    @Override
    public void printMessage(Kind kind, CharSequence msg) {
        delegate.printMessage(kind, prefix + ": " + msg);
    }

    @Override
    public void printMessage(Kind kind, CharSequence msg, Element e) {
        delegate.printMessage(kind, prefix + ": " + msg, e);
    }

    @Override
    public void printMessage(Kind kind, CharSequence msg, Element e, AnnotationMirror a) {
        delegate.printMessage(kind, prefix + ": " + msg, e, a);
    }

    @Override
    public void printMessage(
        Kind kind, CharSequence msg, Element e, AnnotationMirror a, AnnotationValue v
    ) {
        delegate.printMessage(kind, prefix + ": " + msg, e, a, v);
    }
}
