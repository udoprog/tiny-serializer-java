package eu.toolchain.serializer.processor.annotation;

import eu.toolchain.serializer.processor.AutoSerializeUtils;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import lombok.Data;

@Data
public class FieldMirror {
  private final AnnotationMirror annotation;

  private final String name;
  private final String fieldName;
  private final String accessor;
  private final boolean useGetter;
  private final boolean provided;
  private final boolean external;
  private final String providerName;

  public static FieldMirror getFor(
    final AutoSerializeUtils utils, final Element element, final AnnotationMirror a
  ) {
    final AnnotationValues values = utils.getElementValuesWithDefaults(element, a);

    final String name = values.getString("name").get();
    final String fieldName = values.getString("fieldName").get();
    final String accessor = values.getString("accessor").get();
    final boolean useGetter = values.getBoolean("useGetter").get();
    final boolean provided = values.getBoolean("provided").get();
    final boolean external = values.getBoolean("external").get();
    final String providerName = values.getString("providerName").get();

    return new FieldMirror(a, name, fieldName, accessor, useGetter, provided, external,
      providerName);
  }
}
