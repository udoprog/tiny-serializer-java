package eu.toolchain.serializer.processor.value;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import javax.lang.model.element.Modifier;

import com.google.common.collect.ImmutableList;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeName;

import eu.toolchain.serializer.processor.AutoSerializeUtils;
import eu.toolchain.serializer.processor.Naming;
import eu.toolchain.serializer.processor.unverified.Unverified;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ValueSetBuilder {
    final Naming fieldNaming = new Naming("s_");
    final Naming providerNaming = new Naming("p_");
    final Naming variableNaming = new Naming("v_");

    final List<ValueType> types = new ArrayList<>();
    final List<Unverified<Value>> unverifiedValues = new ArrayList<>();

    final AutoSerializeUtils utils;

    public void add(Unverified<ValueSpecification> unverifiedValueSpecification) {
        unverifiedValues.add(unverifiedValueSpecification.map((valueSpecification) -> {
            final TypeName valueType = TypeName.get(valueSpecification.getValueType());
            final TypeName serializerType = TypeName.get(utils.serializerFor(valueSpecification.getValueType()));

            final ValueTypeIdentifier identifier = new ValueTypeIdentifier(valueType, valueSpecification.isProvided(),
                    valueSpecification.getProviderName());

            final ValueType type;
            final Optional<ValueType> found;

            if ((found = types.stream().filter((t) -> t.getIdentifier().equals(identifier)).findFirst()).isPresent()) {
                type = found.get();
            } else {
                final String typeFieldName = fieldNaming.forType(valueType, valueSpecification.isProvided());

                final FieldSpec fieldSpec = FieldSpec.builder(serializerType, typeFieldName)
                        .addModifiers(Modifier.FINAL).build();

                final Optional<ParameterSpec> providedParameterSpec;

                if (valueSpecification.isProvided()) {
                    final String uniqueProviderName;

                    if (valueSpecification.getProviderName().isPresent()) {
                        uniqueProviderName = providerNaming.forName(valueSpecification.getProviderName().get());
                    } else {
                        uniqueProviderName = providerNaming.forType(valueType, false);
                    }

                    providedParameterSpec = Optional
                            .of(ParameterSpec.builder(serializerType, uniqueProviderName, Modifier.FINAL).build());
                } else {
                    providedParameterSpec = Optional.empty();
                }

                type = new ValueType(identifier, valueSpecification.getValueType(), valueType, fieldSpec,
                        providedParameterSpec, valueSpecification.getId());
                types.add(type);
            }

            return new Value(type, valueSpecification.getValueName(), valueSpecification.getAccessor(),
                    variableNaming.forName(valueSpecification.getValueName()), valueSpecification.getId(),
                    valueSpecification.getConstructorOrder());
        }));
    }

    public Unverified<ValueSet> build(boolean isOrdereById, boolean isOrderConstructorById) {
        return Unverified.combine(unverifiedValues).map((values) -> {
            return new ValueSet(isOrdereById, isOrderConstructorById, ImmutableList.copyOf(types),
                    ImmutableList.copyOf(values));
        });
    }
}