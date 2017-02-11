package eu.toolchain.serializer.processor;

import static eu.toolchain.serializer.processor.Exceptions.brokenElement;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeName;
import eu.toolchain.serializer.processor.annotation.AutoSerializeMirror;
import eu.toolchain.serializer.processor.annotation.SubTypeMirror;
import eu.toolchain.serializer.processor.field.Field;
import eu.toolchain.serializer.processor.field.FieldSet;
import eu.toolchain.serializer.processor.field.FieldBuilder;
import eu.toolchain.serializer.processor.field.SubType;
import eu.toolchain.serializer.processor.field.Value;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ClassProcessor {
  final Types types;
  final Elements elements;
  final FrameworkStatements statements;
  final AutoSerializeUtils utils;

  public JavaFile process(final TypeElement element) {
    return buildSpec(element)
      .orElseThrow(() -> brokenElement("@AutoSerialize expected on class or interface", element))
      .toSerializer();
  }

  public Optional<ClassSpec> buildSpec(final Element element) {
    final Optional<AutoSerializeMirror> annotation = utils.autoSerialize(element);

    if (!annotation.isPresent()) {
      return Optional.empty();
    }

    final AutoSerializeMirror autoSerialize = annotation.get();

    if (element.getKind() == ElementKind.INTERFACE) {
      if (autoSerialize.getBuilder().isPresent()) {
        return Optional.of(conreteClass(element));
      } else {
        return Optional.of(abstractClass(element));
      }
    }

    if (element.getKind() == ElementKind.CLASS) {
      if (element.getModifiers().contains(Modifier.ABSTRACT) &&
        !autoSerialize.getBuilder().isPresent()) {
        return Optional.of(abstractClass(element));
      }

      return Optional.of(conreteClass(element));
    }

    return Optional.empty();
  }

  public AbstractClassSpec abstractClass(final Element element) {
    final String packageName = elements.getPackageOf(element).getQualifiedName().toString();
    final String serializerName = utils.serializerName(element);

    final TypeName elementType = TypeName.get(element.asType());
    final TypeName superType = TypeName.get(utils.serializerFor(element.asType()));

    final List<SubType> subTypes = subTypes(element);

    return new AbstractClassSpec(utils, packageName, serializerName, elementType, superType,
      subTypes);
  }

  private ConcreteClassSpec conreteClass(final Element element) {
    final Optional<AutoSerializeMirror> annotation = utils.autoSerialize(element);

    if (!annotation.isPresent()) {
      throw brokenElement("@AutoSerialize annotation not present", element);
    }

    final AutoSerializeMirror autoSerialize = annotation.get();

    final String packageName = elements.getPackageOf(element).getQualifiedName().toString();
    final Set<ElementKind> kinds = getKinds(element);

    final FieldSet fieldSet =
      new FieldSet(this, utils, statements, element, kinds, autoSerialize.isUseGetter());

    for (final Element child : element.getEnclosedElements()) {
      fieldSet.add(child);
    }

    final ClassName elementType = (ClassName) TypeName.get(element.asType());
    final TypeName superType = TypeName.get(utils.serializerFor(element.asType()));
    final String serializerName = utils.serializerName(element);

    final boolean fieldBased = autoSerialize.isFieldBased();
    final boolean failOnMissing = autoSerialize.isFailOnMissing();

    final Optional<FieldBuilder> fieldTypeBuilder =
      utils.builder(element).map(Optional::of).orElseGet(autoSerialize::getBuilder).map(method -> {
        return new FieldBuilder(method, method.shouldUseConstructor(), method.isUseSetter(),
          method.getMethodName());
      });

    final List<Field> fields = ImmutableList.copyOf(fieldSet.getFields().values());
    final List<Value> values = ImmutableList.copyOf(fieldSet.getValues());

    return new ConcreteClassSpec(utils, elements, statements, packageName, fields, values,
      elementType, superType, serializerName, fieldBased, failOnMissing, fieldTypeBuilder);
  }

  /**
   * Get the set of supported element kinds that make up the total set of fields for this type.
   *
   * @param element
   * @return
   */
  Set<ElementKind> getKinds(Element element) {
    final ImmutableSet.Builder<ElementKind> kinds = ImmutableSet.builder();

    if (element.getKind() == ElementKind.INTERFACE) {
      kinds.add(ElementKind.METHOD);
    }

    if (element.getKind() == ElementKind.CLASS) {
      kinds.add(ElementKind.FIELD);

      if (element.getModifiers().contains(Modifier.ABSTRACT)) {
        kinds.add(ElementKind.METHOD);
      }
    }

    return kinds.build();
  }

  List<SubType> subTypes(Element element) {
    return utils.subTypes(element).map(subTypes -> {
      final Set<Short> seenIds = new HashSet<>();

      int offset = 0;
      final ShortIterator index = new ShortIterator();

      final ImmutableList.Builder<SubType> results = ImmutableList.builder();

      for (final SubTypeMirror s : subTypes.getSubTypes()) {
        final DeclaredType type = (DeclaredType) s.getValue().get();
        final short id = s.getId().orElseGet(index::next);

        if (!seenIds.add(id)) {
          throw brokenElement(
            String.format("Conflicting subtype id (%d) defined for definition #%d", id, offset),
            element);
        }

        results.add(new SubType(type, id));
        offset++;
      }

      return results.build();
    }).orElseGet(ImmutableList::of);
  }
}
