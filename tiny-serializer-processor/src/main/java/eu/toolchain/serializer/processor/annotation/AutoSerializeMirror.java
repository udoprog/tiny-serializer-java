package eu.toolchain.serializer.processor.annotation;

import java.util.Optional;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;

import eu.toolchain.serializer.processor.AutoSerializeUtils;
import eu.toolchain.serializer.processor.unverified.Unverified;
import lombok.Data;

@Data
public class AutoSerializeMirror {
    private final AnnotationMirror annotation;

    private final String name;
    private final boolean useGetter;
    private final Optional<BuilderMirror> builder;
    private final boolean orderById;
    private final boolean orderConstructorById;

    public static Unverified<AutoSerializeMirror> getFor(final AutoSerializeUtils utils, final Element element, final AnnotationMirror a) {
        final AnnotationValues values = utils.getElementValuesWithDefaults(element, a);

        final String useSetter = values.getString("name").get();
        final boolean useGetter = values.getBoolean("useGetter").get();
        final boolean orderById = values.getBoolean("orderById").get();
        final boolean orderConstructorById = values.getBoolean("orderConstructorById").get();

        final Unverified<Optional<BuilderMirror>> unverifiedBuilder = makeBuilder(utils, element, values);

        return unverifiedBuilder.map((builder) -> {
            return new AutoSerializeMirror(a, useSetter, useGetter, builder, orderById, orderConstructorById);
        });
    }

    private static Unverified<Optional<BuilderMirror>> makeBuilder(final AutoSerializeUtils utils,
            final Element element, final AnnotationValues values) {
        return utils.builder(element).map((bb) -> {
            return bb.transform((b) -> Unverified.verified(Optional.of(b)));
        }).orElseGet(() -> {
            for (final AnnotationMirror builderMirror : values.getAnnotationValue("builder").get()) {
                return BuilderMirror.getFor(utils, element, builderMirror).map((b) -> Optional.of(b));
            }

            return Unverified.verified(Optional.empty());
        });
    }
}