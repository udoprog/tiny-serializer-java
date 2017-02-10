package eu.toolchain.serializer.processor.annotation;

import eu.toolchain.serializer.processor.AutoSerializeUtils;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import lombok.Data;

@Data
public class IgnoreMirror {
  private final AnnotationMirror annotation;

  public static IgnoreMirror getFor(
    final AutoSerializeUtils utils, final Element element, final AnnotationMirror a
  ) {
    return new IgnoreMirror(a);
  }
}
