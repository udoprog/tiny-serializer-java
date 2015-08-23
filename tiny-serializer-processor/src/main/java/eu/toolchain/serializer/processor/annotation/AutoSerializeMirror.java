package eu.toolchain.serializer.processor.annotation;

import java.util.List;
import java.util.Optional;

import javax.lang.model.element.AnnotationMirror;

import com.google.common.collect.ImmutableList;

import eu.toolchain.serializer.processor.AnnotationValues;
import eu.toolchain.serializer.processor.AutoSerializeUtils;
import lombok.Data;

@Data
public class AutoSerializeMirror {
    private final AnnotationMirror annotation;

    private final String name;
    private final boolean useGetter;
    private final List<BuilderMirror> builder;
    private final boolean orderById;
    private final boolean orderConstructorById;

    public static AutoSerializeMirror getFor(final AutoSerializeUtils utils, final AnnotationMirror a) {
        final AnnotationValues values = utils.getElementValuesWithDefaults(a);

        final String useSetter = values.getString("name");
        final boolean useGetter = values.getBoolean("useGetter");
        final ImmutableList.Builder<BuilderMirror> builder = ImmutableList.builder();

        for (final AnnotationMirror b : values.getAnnotationValue("builder")) {
            builder.add(BuilderMirror.getFor(utils, b));
        }

        final boolean orderById = values.getBoolean("orderById");
        final boolean orderConstructorById = values.getBoolean("orderConstructorById");

        return new AutoSerializeMirror(a, useSetter, useGetter, builder.build(), orderById, orderConstructorById);
    }
}