package eu.toolchain.serializer.processor;

import java.util.List;
import java.util.function.Function;

import javax.lang.model.element.TypeElement;

import com.google.common.collect.ImmutableList;

import lombok.Data;

@Data
public class DeferredProcessing {
    private final TypeElement element;
    private final List<SerializedTypeError> errors;

    public static Function<DeferredProcessing, DeferredProcessing> refresh(AutoSerializeUtils utils) {
        return (d) -> new DeferredProcessing(utils.refetch(d.element), d.errors);
    }

    public DeferredProcessing withError(SerializedTypeError e) {
        return withErrors(ImmutableList.of(e));
    }

    public DeferredProcessing withErrors(List<SerializedTypeError> errors) {
        return new DeferredProcessing(element, errors);
    }
}