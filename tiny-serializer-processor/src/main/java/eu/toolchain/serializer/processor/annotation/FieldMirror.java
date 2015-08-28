package eu.toolchain.serializer.processor.annotation;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;

import eu.toolchain.serializer.processor.AutoSerializeUtils;
import lombok.Data;

@Data
public class FieldMirror {
    private final AnnotationMirror annotation;

    private final String name;
    private final String fieldName;
    private final String accessor;
    private final int id;
    private final int constructorOrder;
    private final boolean useGetter;
    private final boolean provided;
    private final String providerName;

    public static FieldMirror getFor(final AutoSerializeUtils utils, final Element element, final AnnotationMirror a) {
        final AnnotationValues values = utils.getElementValuesWithDefaults(element, a);

        final String name = values.getString("name").get();
        final String fieldName = values.getString("fieldName").get();
        final String accessor = values.getString("accessor").get();
        final int id = values.getInteger("id").get();
        final int constructorOrder = values.getInteger("constructorOrder").get();
        final boolean useGetter = values.getBoolean("useGetter").get();
        final boolean provided = values.getBoolean("provided").get();
        final String providerName = values.getString("providerName").get();

        return new FieldMirror(a, name, fieldName, accessor, id, constructorOrder, useGetter, provided, providerName);
    }
}