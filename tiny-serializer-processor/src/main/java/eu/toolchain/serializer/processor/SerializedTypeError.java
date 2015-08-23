package eu.toolchain.serializer.processor;

import java.util.Optional;

import javax.lang.model.element.Element;
import javax.tools.Diagnostic;

import com.google.common.base.Throwables;

import lombok.Data;

@Data
public class SerializedTypeError {
    private final Diagnostic.Kind kind;
    private final String message;
    private final Optional<Element> element;

    public static SerializedTypeError fromException(Throwable t) {
        return new SerializedTypeError(Diagnostic.Kind.ERROR, Throwables.getStackTraceAsString(t), Optional.empty());
    }
}