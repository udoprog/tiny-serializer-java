package eu.toolchain.serializer.processor.annotation;

import eu.toolchain.serializer.processor.AutoSerializeUtils;
import eu.toolchain.serializer.processor.unverified.Unverified;
import lombok.Data;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import java.util.Optional;

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

    public static Unverified<AutoSerializeMirror> getFor(
        final AutoSerializeUtils utils, final Element element, final AnnotationMirror a
    ) {
        final AnnotationValues values = utils.getElementValuesWithDefaults(element, a);

        final String name = values.getString("name").get();
        final boolean useGetter = values.getBoolean("useGetter").get();
        final boolean fieldBased = values.getBoolean("fieldBased").get();
        final boolean failOnMissing = values.getBoolean("failOnMissing").get();
        final boolean orderById = values.getBoolean("orderById").get();
        final boolean orderConstructorById = values.getBoolean("orderConstructorById").get();

        final Unverified<Optional<BuilderMirror>> unverifiedBuilder =
            makeBuilder(utils, element, values);

        return unverifiedBuilder.map((builder) -> {
            return new AutoSerializeMirror(a, name, useGetter, fieldBased, failOnMissing, builder,
                orderById, orderConstructorById);
        });
    }

    private static Unverified<Optional<BuilderMirror>> makeBuilder(
        final AutoSerializeUtils utils, final Element element, final AnnotationValues values
    ) {
        return utils.builder(element).map((bb) -> {
            return bb.transform((b) -> Unverified.verified(Optional.of(b)));
        }).orElseGet(() -> {
            for (final AnnotationMirror builderMirror : values
                .getAnnotationValue("builder")
                .get()) {
                return BuilderMirror
                    .getFor(utils, element, builderMirror)
                    .map((b) -> Optional.of(b));
            }

            return Unverified.verified(Optional.empty());
        });
    }
}
