package eu.toolchain.serializer.processor.annotation;

import java.util.Optional;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.type.TypeMirror;
import lombok.Data;

@Data
public class BuilderMirror {
  private final AnnotationMirror annotation;

  private final boolean useSetter;
  private final boolean useMethod;
  private final boolean useConstructor;
  private final Optional<AnnotationValues.Value<TypeMirror>> type;
  private final String methodName;

  public boolean shouldUseConstructor() {
    // use explicitly ask to use method.
    if (useMethod) {
      return false;
    }

    // use explicitly ask to use constructor.
    if (useConstructor) {
      return true;
    }

    // by policy, if a type is specified, the constructor should be used.
    if (type.isPresent()) {
      return true;
    }

    return false;
  }
}
