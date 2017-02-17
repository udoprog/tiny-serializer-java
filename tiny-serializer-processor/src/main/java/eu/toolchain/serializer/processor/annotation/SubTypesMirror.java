package eu.toolchain.serializer.processor.annotation;

import java.util.List;
import javax.lang.model.element.AnnotationMirror;
import lombok.Data;

@Data
public class SubTypesMirror {
  private final AnnotationMirror annotation;
  private final List<SubTypeMirror> subTypes;
}
