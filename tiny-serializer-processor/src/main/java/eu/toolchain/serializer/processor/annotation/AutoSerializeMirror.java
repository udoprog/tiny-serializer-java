package eu.toolchain.serializer.processor.annotation;

import eu.toolchain.serializer.processor.AutoSerializeUtils;
import java.util.Optional;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import lombok.Data;

@Data
public class AutoSerializeMirror {
  private final AnnotationMirror annotation;

  private final String name;
  private final boolean useGetter;
  private final boolean fieldBased;
  private final boolean failOnMissing;
  private final Optional<BuilderMirror> builder;
  private final boolean orderById;
  private final boolean orderConstructorById;

  public static AutoSerializeMirror getFor(
    final AutoSerializeUtils utils, final Element element, final AnnotationMirror a
  ) {
    final AnnotationValues values = utils.annotationValues(element, a);

    final String name = values.getString("name").get();
    final boolean useGetter = values.getBoolean("useGetter").get();
    final boolean fieldBased = values.getBoolean("fieldBased").get();
    final boolean failOnMissing = values.getBoolean("failOnMissing").get();
    final boolean orderById = values.getBoolean("orderById").get();
    final boolean orderConstructorById = values.getBoolean("orderConstructorById").get();

    final Optional<BuilderMirror> builder = makeBuilder(utils, element, values);

    return new AutoSerializeMirror(a, name, useGetter, fieldBased, failOnMissing, builder,
      orderById, orderConstructorById);
  }

  private static Optional<BuilderMirror> makeBuilder(
    final AutoSerializeUtils utils, final Element element, final AnnotationValues values
  ) {
    for (final AnnotationMirror builderMirror : values.getAnnotationValue("builder").get()) {
      return Optional.of(utils.builderAnnotation(element, builderMirror));
    }

    return Optional.empty();
  }
}
