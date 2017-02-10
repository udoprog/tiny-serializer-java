package eu.toolchain.serializer.processor;

import java.util.function.Consumer;
import java.util.function.Function;
import javax.annotation.processing.Messager;
import javax.lang.model.element.TypeElement;
import lombok.Data;

@Data
public class DeferredProcessing {
  private final TypeElement element;
  private final Consumer<Messager> broken;

  public static Function<DeferredProcessing, DeferredProcessing> refresh(
    final AutoSerializeUtils utils
  ) {
    return (d) -> new DeferredProcessing(utils.refetch(d.element), messager -> {
    });
  }

  public DeferredProcessing withBroken(final Consumer<Messager> broken) {
    return new DeferredProcessing(element, broken);
  }
}
