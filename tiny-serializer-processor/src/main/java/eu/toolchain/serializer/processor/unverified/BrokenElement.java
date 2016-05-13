package eu.toolchain.serializer.processor.unverified;

import lombok.RequiredArgsConstructor;
import lombok.ToString;

import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic;

@RequiredArgsConstructor
@ToString
public class BrokenElement<T> extends AbstractVerified<T> {
    final String message;
    final Element element;

    @Override
    public T get() {
        throw new IllegalStateException("Broken reference");
    }

    @Override
    public void writeError(Messager messager) {
        messager.printMessage(Diagnostic.Kind.ERROR, message, element);
    }

    @Override
    public boolean isVerified() {
        return false;
    }
}
