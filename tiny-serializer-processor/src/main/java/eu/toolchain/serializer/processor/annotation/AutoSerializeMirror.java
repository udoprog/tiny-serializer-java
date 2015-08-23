package eu.toolchain.serializer.processor.annotation;

import java.util.List;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;

import com.google.common.collect.ImmutableList;

import eu.toolchain.serializer.processor.AutoSerializeUtils;
import eu.toolchain.serializer.processor.unverified.Unverified;
import lombok.Data;

@Data
public class AutoSerializeMirror {
    private final AnnotationMirror annotation;

    private final String name;
    private final boolean useGetter;
    private final List<BuilderMirror> builder;
    private final boolean orderById;
    private final boolean orderConstructorById;

    public static Unverified<AutoSerializeMirror> getFor(final AutoSerializeUtils utils, final Element element, final AnnotationMirror a) {
        final AnnotationValues values = utils.getElementValuesWithDefaults(element, a);

        final String useSetter = values.getString("name").get();
        final boolean useGetter = values.getBoolean("useGetter").get();
        final ImmutableList.Builder<Unverified<BuilderMirror>> builder = ImmutableList.builder();

        for (final AnnotationMirror b : values.getAnnotationValue("builder").get()) {
            builder.add(BuilderMirror.getFor(utils, element, b));
        }

        final boolean orderById = values.getBoolean("orderById").get();
        final boolean orderConstructorById = values.getBoolean("orderConstructorById").get();

        final Unverified<List<BuilderMirror>> unverifiedCombined = Unverified.combine(builder.build());

        return unverifiedCombined.map((combined) -> new AutoSerializeMirror(a, useSetter, useGetter, combined,
                orderById, orderConstructorById));
    }
}