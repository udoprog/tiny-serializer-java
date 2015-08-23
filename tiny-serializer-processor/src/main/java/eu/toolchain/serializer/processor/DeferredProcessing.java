package eu.toolchain.serializer.processor;

import java.util.Optional;
import java.util.function.Function;

import javax.lang.model.element.TypeElement;

import eu.toolchain.serializer.processor.unverified.Unverified;
import lombok.Data;

@Data
public class DeferredProcessing {
    private final TypeElement element;
    private final Optional<Unverified<?>> broken;

    public static Function<DeferredProcessing, DeferredProcessing> refresh(final AutoSerializeUtils utils) {
        return (d) -> new DeferredProcessing(utils.refetch(d.element), Optional.empty());
    }

    public DeferredProcessing withBroken(final Unverified<?> broken) {
        return new DeferredProcessing(element, Optional.of(broken));
    }
}