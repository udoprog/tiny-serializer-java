package eu.toolchain.serializer.processor.annotation;

import com.google.common.collect.ImmutableList;
import eu.toolchain.serializer.processor.AutoSerializeUtils;
import java.util.List;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import lombok.Data;

@Data
public class SubTypesMirror {
    private final AnnotationMirror annotation;
    private final List<SubTypeMirror> subTypes;

    public static SubTypesMirror getFor(
        final AutoSerializeUtils utils, final Element element, final AnnotationMirror a
    ) {
        final AnnotationValues values = utils.getElementValuesWithDefaults(element, a);

        final ImmutableList.Builder<SubTypeMirror> subTypes = ImmutableList.builder();

        for (final AnnotationMirror subType : values.getAnnotationValue("value").get()) {
            subTypes.add(SubTypeMirror.getFor(utils, element, subType));
        }

        return new SubTypesMirror(a, subTypes.build());
    }
}
