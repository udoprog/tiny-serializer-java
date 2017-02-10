package eu.toolchain.serializer.processor.annotation;

import eu.toolchain.serializer.processor.AutoSerializeUtils;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import lombok.Data;

@Data
public class ProvidedMirror {
  private final AnnotationMirror annotation;

  public static ProvidedMirror getFor(
    final AutoSerializeUtils utils, final Element element, final AnnotationMirror a
  ) {
    return new ProvidedMirror(a);
  }
}
