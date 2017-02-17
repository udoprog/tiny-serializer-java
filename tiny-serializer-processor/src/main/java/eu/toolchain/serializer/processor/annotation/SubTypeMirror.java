package eu.toolchain.serializer.processor.annotation;

import java.util.Optional;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.type.TypeMirror;
import lombok.Data;

@Data
public class SubTypeMirror {
  private final AnnotationMirror annotation;
  private final AnnotationValues.Value<TypeMirror> value;
  private final Optional<Short> id;
}
