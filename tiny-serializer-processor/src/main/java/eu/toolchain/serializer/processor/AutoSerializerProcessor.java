package eu.toolchain.serializer.processor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

import com.google.auto.service.AutoService;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;

import eu.toolchain.serializer.AutoSerialize;
import eu.toolchain.serializer.SerialReader;
import eu.toolchain.serializer.SerialWriter;
import eu.toolchain.serializer.Serializer;
import eu.toolchain.serializer.SerializerFramework;
import eu.toolchain.serializer.SerializerFramework.TypeMapping;

@AutoService(Processor.class)
public class AutoSerializerProcessor extends AbstractProcessor {
    public static final String FRAMEWORK_NAME = "framework";
    public static final String SERIALIZER_NAME_FORMAT = "%s_Serializer";
    public static final String SERIALIZATION_NAME = "Serialization";

    static final Joiner parameterJoiner = Joiner.on(", ");

    private boolean disabled = false;
    private Filer filer;
    private Elements elements;
    private Messager messager;
    private int round = 0;

    @Override
    public void init(final ProcessingEnvironment env) {
        super.init(env);

        filer = env.getFiler();
        elements = env.getElementUtils();
        messager = env.getMessager();

        /**
         * Eclipse JDT does not preserve the original order of type fields, causing some Processor assumptions to fail.
         */
        if (env.getClass().getPackage().getName().startsWith("org.eclipse.jdt.")) {
            messager.printMessage(Diagnostic.Kind.WARNING, "@AutoSerialize processor cannot run in the given environment");
            disabled = true;
        }
    }

    @Override
    public boolean process(Set<? extends TypeElement> typeElements, RoundEnvironment env) {
        messager.printMessage(Diagnostic.Kind.NOTE,
                String.format("processing round %d", round++));

        if (disabled) {
            return false;
        }
        
        final Collection<SerializedType> processed = processElements(env.getElementsAnnotatedWith(AutoSerialize.class));

        for (final SerializedType p : processed) {
            if (!p.isValid(messager)) {
                messager.printMessage(Diagnostic.Kind.WARNING, String.format("Might not be valid: %s", p.getElementType()));
            }
            
            final JavaFile output = p.asJavaFile();

            messager.printMessage(Diagnostic.Kind.NOTE,
                    String.format("Writing %s.%s", output.packageName, output.typeSpec.name));

            try {
                output.writeTo(filer);
            } catch (IOException e) {
                messager.printMessage(Diagnostic.Kind.ERROR, e.getMessage());
                return false;
            }
        }

        final Set<JavaFile> serialization = processSerialization(processed);

        for (final JavaFile file : serialization) {
            messager.printMessage(Diagnostic.Kind.NOTE, String.format(
                    "Writing %s.%s (%d serializer(s))", file.packageName, file.typeSpec.name,
                    processed.size()));

            try {
                file.writeTo(filer);
            } catch (IOException e) {
                messager.printMessage(Diagnostic.Kind.ERROR, e.getMessage());
                return false;
            }
        }

        return false;
    }

    Set<JavaFile> processSerialization(Collection<SerializedType> processed) {
        final Set<JavaFile> serialization = new HashSet<>();

        for (final Map.Entry<String, Collection<SerializedType>> e : byPackage(processed).entrySet()) {
            serialization.add(processPackageSerialization(e.getKey(), e.getValue()));
        }

        return serialization;
    }

    JavaFile processPackageSerialization(final String packageName, final Collection<SerializedType> processed) {
        final TypeSpec.Builder serialization = TypeSpec.classBuilder(SERIALIZATION_NAME);

        serialization.addModifiers(Modifier.PUBLIC, Modifier.FINAL);

        final FieldSpec frameworkField = FieldSpec.builder(SerializerFramework.class, FRAMEWORK_NAME)
                .addModifiers(Modifier.PRIVATE, Modifier.FINAL).build();

        serialization.addField(frameworkField);

        serialization.addMethod(serializationConstructor(frameworkField));

        for (final SerializedType p : processed) {
            serialization.addMethod(builderMethod(p, frameworkField));
        }

        final JavaFile.Builder b = JavaFile.builder(packageName, serialization.build());
        return b.build();
    }

    MethodSpec serializationConstructor(final FieldSpec frameworkField) {
        final ParameterSpec framework = ParameterSpec.builder(SerializerFramework.class, "f")
                .addModifiers(Modifier.FINAL).build();

        final MethodSpec.Builder b = MethodSpec.constructorBuilder();

        b.addModifiers(Modifier.PUBLIC);
        b.addParameter(framework);
        b.addStatement("$N = $N", frameworkField, framework);
        return b.build();
    }

    MethodSpec builderMethod(final SerializedType p, final FieldSpec framework) {
        final MethodSpec.Builder b = MethodSpec.methodBuilder(String.format("get%s", p.name));

        final List<String> statements = new ArrayList<>();
        final List<Object> parameters = new ArrayList<>();

        int index = 0;

        parameters.add(p.type);

        parameters.add(framework);
        statements.add("$N");

        for (final SerializedFieldType t : p.getFields().getFieldTypes()) {
            if (!t.getProvidedParameterSpec().isPresent()) {
                continue;
            }

            final ParameterSpec spec = ParameterSpec.builder(serializerFor(t.getFieldType()),
                    String.format("p%d", index++), Modifier.FINAL).build();
            b.addParameter(spec);
            parameters.add(spec);
            statements.add("$N");
        }

        b.returns(p.supertype);
        b.addModifiers(Modifier.PUBLIC);
        b.addStatement(String.format("return new $N(%s)", parameterJoiner.join(statements)), parameters.toArray());

        return b.build();
    }

    Map<String, Collection<SerializedType>> byPackage(Collection<SerializedType> processed) {
        final Map<String, Collection<SerializedType>> byPackage = new HashMap<>();

        for (final SerializedType p : processed) {
            Collection<SerializedType> group = byPackage.get(p.packageName);

            if (group == null) {
                group = new ArrayList<>();
                byPackage.put(p.packageName, group);
            }

            group.add(p);
        }

        return byPackage;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return ImmutableSet.of(AutoSerialize.class.getName());
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    Collection<SerializedType> processElements(Set<? extends Element> elements) {
        final List<SerializedType> processed = new ArrayList<>();

        for (final Element element : elements) {
            messager.printMessage(Diagnostic.Kind.NOTE, String.format("Processing %s", element));
            processed.add(processElement(element));
        }

        return processed;
    }

    SerializedType processElement(Element element) {
        if (element.getKind() == ElementKind.INTERFACE) {
            return processInterface(element);
        }

        return processClass(element);
    }

    SerializedType processInterface(Element element) {
        final AutoSerialize annotation = element.getAnnotation(AutoSerialize.class);

        final String packageName = elements.getPackageOf(element).getQualifiedName().toString();
        final String serializerName = serializerName(element, annotation);
        final String name = name(element, annotation);

        final TypeName elementType = TypeName.get(element.asType());
        final TypeName supertype = serializerFor(elementType);

        final List<SubType> subtypes = getSubtypes(element);

        final FieldSpec serializer = FieldSpec.builder(supertype, "serializer", Modifier.FINAL).build();

        final TypeSpec.Builder generated = TypeSpec.classBuilder(serializerName);

        generated.addModifiers(Modifier.PUBLIC, Modifier.FINAL);
        generated.addSuperinterface(supertype);

        generated.addField(serializer);

        generated.addMethod(interfaceSerializeConstructor(elementType, serializer, subtypes));
        generated.addMethod(interfaceSerializeMethod(elementType, serializer));
        generated.addMethod(interfaceDeserializeMethod(elementType, serializer));

        return new SerializedType(element, packageName, name, generated.build(), elementType, supertype, new SerializedTypeFields());
    }

    static List<SubType> getSubtypes(Element element) {
        final AutoSerialize.SubTypes annotation = element.getAnnotation(AutoSerialize.SubTypes.class);

        if (annotation == null) {
            return ImmutableList.of();
        }

        final Set<Short> seenIds = new HashSet<>();
        final ImmutableList.Builder<SubType> subtypes = ImmutableList.builder();

        int offset = 0;
        int index = 0;

        for (final AutoSerialize.SubType s : annotation.value()) {
            final ClassName type = hackilyPullClassName(s);
            final short id = s.id() < 0 ? nextShort(index++) : s.id();

            if (!seenIds.add(id)) {
                throw new IllegalStateException(String.format("Conflicting subtype id (%d) defined for definition #%d",
                        id, offset));
            }

            subtypes.add(new SubType(type, id));
            offset++;
        }

        return subtypes.build();
    }

    private static short nextShort(int i) {
        if (i > Short.MAX_VALUE) {
            throw new IllegalStateException("Too many subtypes defined");
        }

        return (short) i;
    }

    private static ClassName hackilyPullClassName(AutoSerialize.SubType annotation) {
        try {
            return ClassName.get(annotation.value());
        } catch (final MirroredTypeException e) {
            return (ClassName) TypeName.get(e.getTypeMirror());
        }
    }

    static final ClassName list = ClassName.get(List.class);
    static final ClassName typeMapping = ClassName.get(TypeMapping.class);
    static final ClassName arrayList = ClassName.get(ArrayList.class);

    static MethodSpec interfaceSerializeConstructor(TypeName elementType, FieldSpec serializer,
            final List<SubType> subtypes) {
        final ParameterSpec framework = ParameterSpec.builder(SerializerFramework.class, FRAMEWORK_NAME)
                .addModifiers(Modifier.FINAL).build();

        final MethodSpec.Builder b = MethodSpec.constructorBuilder();
        b.addModifiers(Modifier.PUBLIC);

        b.addParameter(framework);

        b.addStatement("final $T<$T<? extends $T, $T>> mappings = new $T<>()", list, typeMapping, elementType,
                elementType,
                arrayList);

        for (final SubType subtype : subtypes) {
            final ClassName serializerType = serializerClassFor(subtype.type);

            b.addStatement("mappings.add($N.<$T, $T>type($L, $T.class, new $T($N)))", framework, subtype.type,
                    elementType, subtype.id, subtype.type, serializerType, framework);
        }

        b.addStatement("$N = $N.subtypes(mappings)", serializer, framework);
        return b.build();
    }

    static MethodSpec interfaceSerializeMethod(TypeName valueType, FieldSpec serializer) {
        final ParameterSpec buffer = ParameterSpec.builder(SerialWriter.class, "buffer").addModifiers(Modifier.FINAL)
                .build();

        final ParameterSpec value = ParameterSpec.builder(valueType, "value")
                .addModifiers(Modifier.FINAL).build();

        final MethodSpec.Builder b = buildSerializeMethod(buffer, value);

        b.addStatement("$N.serialize($N, $N)", serializer, buffer, value);

        return b.build();
    }

    static MethodSpec interfaceDeserializeMethod(TypeName returnType, FieldSpec serializer) {
        final ParameterSpec buffer = ParameterSpec.builder(SerialReader.class, "buffer").addModifiers(Modifier.FINAL)
                .build();

        final MethodSpec.Builder b = buildDeserializeMethod(returnType, buffer);

        b.addStatement("return $N.deserialize($N)", serializer, buffer);

        return b.build();
    }

    SerializedType processClass(final Element element) {
        final AutoSerialize annotation = element.getAnnotation(AutoSerialize.class);

        final String packageName = elements.getPackageOf(element).getQualifiedName().toString();
        final String name = name(element, annotation);

        final SerializedTypeFields serializedType = SerializedTypeFields.build(element, annotation);

        final TypeName elementType = TypeName.get(element.asType());
        final TypeName supertype = serializerFor(elementType);

        final TypeSpec.Builder generated = TypeSpec.classBuilder(serializerName(element, annotation));

        for (final SerializedFieldType t : serializedType.getFieldTypes()) {
            generated.addField(t.getFieldSpec());
        }

        generated.addModifiers(Modifier.PUBLIC, Modifier.FINAL);
        generated.addSuperinterface(supertype);

        generated.addMethod(classSerializeConstructor(serializedType));
        generated.addMethod(classSerializeMethod(elementType, serializedType));
        generated.addMethod(classDeserializeMethod(elementType, serializedType));

        return new SerializedType(element, packageName, name, generated.build(), elementType, supertype, serializedType);
    }

    static MethodSpec classSerializeConstructor(final SerializedTypeFields serialized) {
        final ParameterSpec framework = ParameterSpec.builder(SerializerFramework.class, FRAMEWORK_NAME)
                .addModifiers(Modifier.FINAL).build();

        final MethodSpec.Builder b = MethodSpec.constructorBuilder();
        b.addModifiers(Modifier.PUBLIC);
        b.addParameter(framework);

        for (final SerializedFieldType t : serialized.getFieldTypes()) {
            if (t.getProvidedParameterSpec().isPresent()) {
                b.addParameter(t.getProvidedParameterSpec().get());
            }
        }

        for (final SerializedFieldType fieldType : serialized.getFieldTypes()) {
            if (fieldType.getProvidedParameterSpec().isPresent()) {
                b.addStatement("$N = $N", fieldType.getFieldSpec(), fieldType.getProvidedParameterSpec().get());
                continue;
            }

            final FrameworkMethodBuilder builder = new FrameworkMethodBuilder() {
                @Override
                public void assign(final String statement, final List<Object> arguments) {
                    b.addStatement(String.format("$N = %s", statement), ImmutableList.builder().add(fieldType.getFieldSpec())
                            .addAll(arguments).build().toArray());
                }
            };

            FrameworkStatements.resolveStatement(boxedType(fieldType.getFieldType()), framework).writeTo(builder);
        }

        return b.build();
    }

    static MethodSpec classSerializeMethod(final TypeName valueType, final SerializedTypeFields serialized) {
        final ParameterSpec buffer = ParameterSpec.builder(SerialWriter.class, "buffer").addModifiers(Modifier.FINAL)
                .build();

        final ParameterSpec value = ParameterSpec.builder(valueType, "value")
                .addModifiers(Modifier.FINAL).build();

        final MethodSpec.Builder b = buildSerializeMethod(buffer, value);

        for (final SerializedField field : serialized.getFields()) {
            b.addStatement("$N.serialize($N, $N.$L())", field.getFieldType().getFieldSpec(), buffer, value,
                    field.getAccessor());
        }

        return b.build();
    }

    static MethodSpec classDeserializeMethod(TypeName returnType, SerializedTypeFields serializedType) {
        final ParameterSpec buffer = ParameterSpec.builder(SerialReader.class, "buffer").addModifiers(Modifier.FINAL)
                .build();

        final MethodSpec.Builder b = buildDeserializeMethod(returnType, buffer);

        final List<String> variables = new ArrayList<>();

        for (final SerializedField field : serializedType.getFields()) {
            final String name = String.format("v_%s", field.getFieldName());
            variables.add(name);
            final TypeName fieldType = field.getFieldType().getFieldType();
            final FieldSpec fieldSpec = field.getFieldType().getFieldSpec();
            b.addStatement("final $T $L = $N.deserialize($N)", fieldType, name, fieldSpec, buffer);
        }

        b.addStatement("return new $T($L)", returnType, parameterJoiner.join(variables));
        return b.build();
    }

    static MethodSpec.Builder buildDeserializeMethod(final TypeName elementType, final ParameterSpec buffer) {
        final MethodSpec.Builder b = MethodSpec.methodBuilder("deserialize");

        b.addModifiers(Modifier.PUBLIC);
        b.returns(elementType);
        b.addAnnotation(Override.class);
        b.addParameter(buffer);
        b.addException(IOException.class);

        return b;
    }

    static MethodSpec.Builder buildSerializeMethod(final ParameterSpec buffer, final ParameterSpec value) {
        final MethodSpec.Builder b = MethodSpec.methodBuilder("serialize");

        b.addModifiers(Modifier.PUBLIC);
        b.returns(TypeName.VOID);
        b.addAnnotation(Override.class);
        b.addParameter(buffer);
        b.addParameter(value);
        b.addException(IOException.class);

        return b;
    }

    static TypeName serializerFor(Class<?> type) {
        return serializerFor(ClassName.get(type));
    }

    static TypeName boxedType(TypeName type) {
        if (type.isPrimitive()) {
            return primitiveType(type);
        }

        return type;
    }

    static TypeName serializerFor(TypeName type) {
        if (type.isPrimitive()) {
            return serializerFor(primitiveType(type));
        }

        return ParameterizedTypeName.get(ClassName.get(Serializer.class), type);
    }

    static ClassName primitiveType(TypeName type) {
        if (type == TypeName.BOOLEAN) {
            return ClassName.get(Boolean.class);
        }

        if (type == TypeName.BYTE) {
            return ClassName.get(Byte.class);
        }

        if (type == TypeName.SHORT) {
            return ClassName.get(Short.class);
        }

        if (type == TypeName.INT) {
            return ClassName.get(Integer.class);
        }

        if (type == TypeName.LONG) {
            return ClassName.get(Long.class);
        }

        if (type == TypeName.CHAR) {
            return ClassName.get(Character.class);
        }

        if (type == TypeName.FLOAT) {
            return ClassName.get(Float.class);
        }

        if (type == TypeName.DOUBLE) {
            return ClassName.get(Double.class);
        }

        throw new IllegalArgumentException("Invalid type: " + type.toString());
    }

    static ClassName serializerClassFor(ClassName type) {
        return ClassName.get(type.packageName(), String.format(SERIALIZER_NAME_FORMAT, type.simpleName()));
    }

    static String serializerName(Element element, AutoSerialize annotation) {
        return String.format(SERIALIZER_NAME_FORMAT, element.getSimpleName());
    }

    static String name(Element element, AutoSerialize annotation) {
        if (!"".equals(annotation.name())) {
            return annotation.name();
        }

        return element.getSimpleName().toString();
    }
}
