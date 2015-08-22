package eu.toolchain.serializer.processor;

import java.util.List;
import java.util.function.Function;

import javax.lang.model.element.TypeElement;

import com.google.common.collect.ImmutableList;

import lombok.Data;

@Data
public class DeferredProcessing {
    private final TypeElement element;
    private final List<Throwable> errors;

    public static Function<DeferredProcessing, DeferredProcessing> refresh(AutoSerializeUtils utils) {
        return (d) -> new DeferredProcessing(utils.refetch(d.element), d.errors);
    }

    public DeferredProcessing withError(Throwable e) {
        return new DeferredProcessing(element, ImmutableList.<Throwable>builder().addAll(errors).add(e).build());
    }
}