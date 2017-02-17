package eu.toolchain.serializer.processor.annotation;

import javax.lang.model.element.AnnotationMirror;
import lombok.Data;

@Data
public class IgnoreMirror {
  private final AnnotationMirror annotation;
}
