package eu.toolchain.serializer.processor.annotation;

import javax.lang.model.element.AnnotationMirror;

import eu.toolchain.serializer.processor.AnnotationValues;
import eu.toolchain.serializer.processor.AutoSerializeUtils;
import lombok.Data;

@Data
public class FieldMirror {
    private final AnnotationMirror annotation;

    private final String accessor;
    private final int id;
    private final int constructorOrder;
    private final boolean useGetter;
    private final boolean provided;
    private final String providerName;

    public static FieldMirror getFor(final AutoSerializeUtils utils, final AnnotationMirror a) {
        final AnnotationValues values = utils.getElementValuesWithDefaults(a);

        final String accessor = values.getString("accessor");
        final int id = values.getInteger("id");
        final int constructorOrder = values.getInteger("constructorOrder");
        final boolean useGetter = values.getBoolean("useGetter");
        final boolean provided = values.getBoolean("provided");
        final String providerName = values.getString("providerName");

        return new FieldMirror(a, accessor, id, constructorOrder, useGetter, provided, providerName);
    }
}