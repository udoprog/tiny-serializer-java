package eu.toolchain.serializer.processor;

import static eu.toolchain.serializer.processor.Exceptions.brokenElement;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeName;
import eu.toolchain.serializer.processor.annotation.AutoSerializeMirror;
import eu.toolchain.serializer.processor.annotation.SubTypeMirror;
import eu.toolchain.serializer.processor.field.FieldSet;
import eu.toolchain.serializer.processor.field.FieldSetBuilder;
import eu.toolchain.serializer.processor.field.FieldTypeBuilder;
import eu.toolchain.serializer.processor.field.SubType;
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
    final Optional<AutoSerializeMirror> annotation = utils.autoSerialize(element);

    if (!annotation.isPresent()) {
      throw brokenElement("@AutoSerialize annotation not present", element);
    }

    final AutoSerializeMirror autoSerialize = annotation.get();

    if (element.getKind() == ElementKind.INTERFACE) {
      if (autoSerialize.getBuilder().isPresent()) {
        return conreteClass(element).toSerializer();
      } else {
        return abstractClass(element).toSerializer();
      }
    }

    if (element.getKind() == ElementKind.CLASS) {
      if (element.getModifiers().contains(Modifier.ABSTRACT) &&
        !autoSerialize.getBuilder().isPresent()) {
        return abstractClass(element).toSerializer();
      }

      return conreteClass(element).toSerializer();
    }

    throw brokenElement("Unsupported type, expected class or interface", element);
  }

  public AbstractClassSpec abstractClass(final TypeElement element) {
    final String packageName = elements.getPackageOf(element).getQualifiedName().toString();
    final String serializerName = utils.serializerName(element);

    final TypeName elementType = TypeName.get(element.asType());
    final TypeName superType = TypeName.get(utils.serializerFor(element.asType()));

    final List<SubType> subTypes = subTypes(element);

    return new AbstractClassSpec(utils, packageName, serializerName, elementType, superType,
      subTypes);
  }

  private ConcreteClassSpec conreteClass(final TypeElement element) {
    final Optional<AutoSerializeMirror> annotation = utils.autoSerialize(element);

    if (!annotation.isPresent()) {
      throw brokenElement("@AutoSerialize annotation not present", element);
    }

    final AutoSerializeMirror autoSerialize = annotation.get();

    final String packageName = elements.getPackageOf(element).getQualifiedName().toString();
    final Set<ElementKind> kinds = getKinds(element);

    final FieldSetBuilder fieldSetBuilder =
      new FieldSetBuilder(utils, element, kinds, autoSerialize.isUseGetter());

    for (final Element child : element.getEnclosedElements()) {
      fieldSetBuilder.add(child);
    }

    final FieldSet fields =
      fieldSetBuilder.build(autoSerialize.isOrderById(), autoSerialize.isOrderConstructorById());

    final ClassName elementType = (ClassName) TypeName.get(element.asType());
    final TypeName superType = TypeName.get(utils.serializerFor(element.asType()));
    final String serializerName = utils.serializerName(element);

    final boolean fieldBased = autoSerialize.isFieldBased();
    final boolean failOnMissing = autoSerialize.isFailOnMissing();

    final Optional<FieldTypeBuilder> fieldTypeBuilder =
      utils.builder(element).map(Optional::of).orElseGet(autoSerialize::getBuilder).map(method -> {
        return new FieldTypeBuilder(method, method.shouldUseConstructor(), method.isUseSetter(),
          method.getMethodName());
      });

    return new ConcreteClassSpec(utils, elements, statements, packageName, fields, elementType,
      superType, serializerName, fieldBased, failOnMissing, fieldTypeBuilder);
  }

  /**
   * Get the set of supported element kinds that make up the total set of fields for this type.
   *
   * @param element
   * @return
   */
  Set<ElementKind> getKinds(TypeElement element) {
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
