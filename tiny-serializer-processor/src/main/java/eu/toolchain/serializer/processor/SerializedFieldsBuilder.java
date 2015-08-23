package eu.toolchain.serializer.processor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;

import com.google.common.collect.ImmutableList;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;

import eu.toolchain.serializer.processor.unverified.Unverified;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class SerializedFieldsBuilder {
    final Naming fieldNaming = new Naming("s_");
    final Naming providerNaming = new Naming("p_");
    final Naming variableNaming = new Naming("v_");

    final List<SerializedFieldType> types = new ArrayList<>();

    final AutoSerializeUtils utils;
    final Element element;

    public Unverified<SerializedField> buildField(FieldInformation f) {
        final TypeName fieldType = TypeName.get(f.getFieldType());
        final TypeName serializerType = TypeName.get(utils.serializerFor(f.getFieldType()));

        final SerializedFieldTypeIdentifier identifier = new SerializedFieldTypeIdentifier(fieldType, f.isProvided(),
                f.getProviderName());

        final SerializedFieldType type;
        final Optional<SerializedFieldType> found;

        if ((found = types.stream().filter((t) -> t.getIdentifier().equals(identifier)).findFirst()).isPresent()) {
            type = found.get();
        } else {
            final String typeFieldName = fieldNaming.forType(fieldType, f.isProvided());

            final FieldSpec fieldSpec = FieldSpec
                    .builder(serializerType, typeFieldName)
                    .addModifiers(Modifier.FINAL).build();

            final Optional<ParameterSpec> providedParameterSpec;

            if (f.isProvided()) {
                final String uniqueProviderName;

                if (f.getProviderName().isPresent()) {
                    uniqueProviderName = providerNaming.forName(f.getProviderName().get());
                } else {
                    uniqueProviderName = providerNaming.forType(fieldType, false);
                }

                providedParameterSpec = Optional.of(ParameterSpec.builder(serializerType, uniqueProviderName,
                        Modifier.FINAL).build());
            } else {
                providedParameterSpec = Optional.empty();
            }

            type = new SerializedFieldType(identifier, f.getFieldType(), fieldType, fieldSpec, providedParameterSpec, f.getId());
            types.add(type);
        }

        final SerializedField field = new SerializedField(f.getElement(), type, f.getFieldName(), f.getAccessor(),
                variableNaming.forName(f.getFieldName()), f.getId(), f.getConstructorOrder());

        return Unverified.verified(field);
    }

    public List<SerializedFieldType> getTypes() {
        return ImmutableList.copyOf(types);
    }
}