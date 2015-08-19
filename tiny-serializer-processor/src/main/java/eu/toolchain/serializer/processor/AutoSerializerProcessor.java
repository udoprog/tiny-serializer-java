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
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;

import com.google.auto.service.AutoService;
import com.google.common.base.CaseFormat;
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
import com.sun.tools.javac.util.Pair;

import eu.toolchain.serializer.AbsentProperty;
import eu.toolchain.serializer.AutoSerialize;
import eu.toolchain.serializer.OptionalProperty;
import eu.toolchain.serializer.PresentProperty;
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

    static final ClassName optionalPropertyType = ClassName.get(OptionalProperty.class);
    static final ClassName presentPropertytType = ClassName.get(PresentProperty.class);
    static final ClassName absentPropertyType = ClassName.get(AbsentProperty.class);

    static final Joiner parameterJoiner = Joiner.on(", ");

    private Filer filer;
    private Elements elements;
    private Messager messager;

    @Override
    public void init(final ProcessingEnvironment env) {
        super.init(env);

        filer = env.getFiler();
        elements = env.getElementUtils();
        messager = env.getMessager();
    }

    @Override
    public boolean process(Set<? extends TypeElement> typeElements, RoundEnvironment env) {
        final Collection<ProcessedSerializer> processed;

        try {
            processed = processElements(env.getElementsAnnotatedWith(AutoSerialize.class));
        } catch (IOException e) {
            messager.printMessage(Diagnostic.Kind.ERROR, e.getMessage());
            return false;
        }

        final Set<JavaFile> serialization = processSerialization(processed);

        for (final ProcessedSerializer p : processed) {
            try {
                p.asJavaFile().writeTo(filer);
            } catch (IOException e) {
                messager.printMessage(Diagnostic.Kind.ERROR, e.getMessage());
                return false;
            }
        }

        for (final JavaFile file : serialization) {
            try {
                file.writeTo(filer);
            } catch (IOException e) {
                messager.printMessage(Diagnostic.Kind.ERROR, e.getMessage());
                return false;
            }
        }

        return false;
    }

    Set<JavaFile> processSerialization(Collection<ProcessedSerializer> processed) {
        final Set<JavaFile> serialization = new HashSet<>();

        for (final Map.Entry<String, Collection<ProcessedSerializer>> e : byPackage(processed).entrySet()) {
            serialization.add(processPackageSerialization(e.getKey(), e.getValue()));
        }

        return serialization;
    }

    JavaFile processPackageSerialization(final String packageName, final Collection<ProcessedSerializer> processed) {
        final TypeSpec.Builder serialization = TypeSpec.classBuilder(SERIALIZATION_NAME);

        serialization.addModifiers(Modifier.PUBLIC, Modifier.FINAL);

        final FieldSpec frameworkField = FieldSpec.builder(SerializerFramework.class, FRAMEWORK_NAME)
                .addModifiers(Modifier.PRIVATE, Modifier.FINAL).build();

        serialization.addField(frameworkField);

        serialization.addMethod(serializationConstructor(frameworkField));

        for (final ProcessedSerializer p : processed) {
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

    MethodSpec builderMethod(final ProcessedSerializer p, final FieldSpec framework) {
        final MethodSpec.Builder b = MethodSpec.methodBuilder(String.format("get%s", p.name));

        final List<String> statements = new ArrayList<>();
        final List<Object> parameters = new ArrayList<>();

        int index = 0;

        parameters.add(p.type);

        parameters.add(framework);
        statements.add("$N");

        for (final SerializedField f : p.fields) {
            if (f.type.provided) {
                final ParameterSpec spec = ParameterSpec.builder(serializerFor(f.type.serializedFieldType), String.format("p%d", index++), Modifier.FINAL).build();
                b.addParameter(spec);
                parameters.add(spec);
                statements.add("$N");
            }
        }

        b.returns(p.supertype);
        b.addModifiers(Modifier.PUBLIC);
        b.addStatement(String.format("return new $N(%s)", parameterJoiner.join(statements)), parameters.toArray());

        return b.build();
    }

    Map<String, Collection<ProcessedSerializer>> byPackage(Collection<ProcessedSerializer> processed) {
        final Map<String, Collection<ProcessedSerializer>> byPackage = new HashMap<>();

        for (final ProcessedSerializer p : processed) {
            Collection<ProcessedSerializer> group = byPackage.get(p.packageName);

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

    Collection<ProcessedSerializer> processElements(Set<? extends Element> elements) throws IOException {
        final List<ProcessedSerializer> processed = new ArrayList<>();

        for (final Element element : elements) {
            processed.add(processElement(element));
        }

        return processed;
    }

    ProcessedSerializer processElement(Element element) throws IOException {
        if (element.getKind() == ElementKind.INTERFACE) {
            return processInterface(element);
        }

        return processClass(element);
    }

    ProcessedSerializer processInterface(Element element) {
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
        generated.addMethod(interfaceSerializeMethod(element, serializer));
        generated.addMethod(interfaceDeserializeMethod(element, serializer));

        return new ProcessedSerializer(packageName, name, generated.build(), elementType, supertype,
                ImmutableList.<SerializedField> of());
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

    static MethodSpec interfaceSerializeMethod(Element element, FieldSpec serializer) {
        final ParameterSpec buffer = ParameterSpec.builder(SerialWriter.class, "buffer").addModifiers(Modifier.FINAL)
                .build();

        final ParameterSpec value = ParameterSpec.builder(TypeName.get(element.asType()), "value")
                .addModifiers(Modifier.FINAL).build();

        final MethodSpec.Builder b = buildSerializeMethod(buffer, value);

        b.addStatement("$N.serialize($N, $N)", serializer, buffer, value);

        return b.build();
    }

    static MethodSpec interfaceDeserializeMethod(Element element, FieldSpec serializer) {
        final ParameterSpec buffer = ParameterSpec.builder(SerialReader.class, "buffer").addModifiers(Modifier.FINAL)
                .build();

        final MethodSpec.Builder b = buildDeserializeMethod(element, buffer);

        b.addStatement("return $N.deserialize($N)", serializer, buffer);

        return b.build();
    }

    ProcessedSerializer processClass(Element element) {
        final AutoSerialize annotation = element.getAnnotation(AutoSerialize.class);

        final String packageName = elements.getPackageOf(element).getQualifiedName().toString();
        final String serializerName = serializerName(element, annotation);
        final String name = name(element, annotation);

        final SerializedType creator = findCreatorConstructor(element, annotation);

        final TypeName elementType = TypeName.get(element.asType());
        final TypeName supertype = serializerFor(elementType);

        final FieldSpec optional = FieldSpec.builder(serializerFor(Boolean.class), "optional")
                .addModifiers(Modifier.FINAL).build();

        final TypeSpec.Builder generated = TypeSpec.classBuilder(serializerName);

        if (creator.anyOptional) {
            generated.addField(optional);
        }

        for (final SerializedField serializedType : creator.fields) {
            generated.addField(serializedType.fieldSpec);
        }

        generated.addModifiers(Modifier.PUBLIC, Modifier.FINAL);
        generated.addSuperinterface(supertype);

        generated.addMethod(classSerializeConstructor(creator, optional));
        generated.addMethod(classSerializeMethod(element, creator, optional));
        generated.addMethod(classDeserializeMethod(element, creator, optional));

        return new ProcessedSerializer(packageName, name, generated.build(), elementType, supertype,
                creator.fields);
    }

    static MethodSpec classSerializeConstructor(final SerializedType creator, final FieldSpec optional) {
        final ParameterSpec framework = ParameterSpec.builder(SerializerFramework.class, FRAMEWORK_NAME)
                .addModifiers(Modifier.FINAL).build();

        final MethodSpec.Builder b = MethodSpec.constructorBuilder();
        b.addModifiers(Modifier.PUBLIC);
        b.addParameter(framework);

        if (creator.anyOptional) {
            b.addStatement("$N = $N.bool()", optional, framework);
        }

        int providedIndex = 0;

        for (final SerializedField s : creator.fields) {
            if (s.type.provided) {
                final ParameterSpec spec = ParameterSpec.builder(serializerFor(s.type.serializedFieldType),
                        String.format("p%d", providedIndex++), Modifier.FINAL).build();

                b.addParameter(spec);
                b.addStatement("$N = $N", s.fieldSpec, spec);
                continue;
            }

            final FrameworkMethodBuilder builder = new FrameworkMethodBuilder() {
                @Override
                public void assign(final String statement, final List<Object> arguments) {
                    b.addStatement(String.format("$N = %s", statement),
                            ImmutableList.builder().add(s.fieldSpec).addAll(arguments).build().toArray());
                }
            };

            FrameworkStatements.resolveStatement(boxedType(s.type.serializedFieldType), framework).writeTo(builder);
        }

        return b.build();
    }

    static MethodSpec classSerializeMethod(final Element element, final SerializedType creator,
            final FieldSpec optional) {
        final ParameterSpec buffer = ParameterSpec.builder(SerialWriter.class, "buffer").addModifiers(Modifier.FINAL)
                .build();

        final ParameterSpec value = ParameterSpec.builder(TypeName.get(element.asType()), "value")
                .addModifiers(Modifier.FINAL).build();

        final MethodSpec.Builder b = buildSerializeMethod(buffer, value);

        for (final SerializedField field : creator.fields) {
            if (field.type.optional) {
                serializeOptionalParameter(b, field, buffer, value, optional);
                continue;
            }

            b.addStatement("$N.serialize($N, $N.$L())", field.fieldSpec, buffer, value, field.type.accessor);
        }

        return b.build();
    }

    static MethodSpec classDeserializeMethod(Element element, SerializedType creator, FieldSpec optional) {
        final ParameterSpec buffer = ParameterSpec.builder(SerialReader.class, "buffer").addModifiers(Modifier.FINAL)
                .build();

        final MethodSpec.Builder b = buildDeserializeMethod(element, buffer);

        final List<String> variables = new ArrayList<>();

        int index = 0;

        for (final SerializedField field : creator.fields) {
            final String name = String.format("v%d", index++);

            variables.add(name);

            if (field.type.optional) {
                deserializeOptionalParameter(b, field, buffer, optional, name);
                continue;
            }

            b.addStatement("final $T $L = $N.deserialize($N)", field.type.fieldType, name, field.fieldSpec, buffer);
        }

        b.addStatement("return new $T($L)", creator.type, parameterJoiner.join(variables));
        return b.build();
    }

    static MethodSpec.Builder buildDeserializeMethod(final Element element, final ParameterSpec buffer) {
        final MethodSpec.Builder b = MethodSpec.methodBuilder("deserialize");

        b.addModifiers(Modifier.PUBLIC);
        b.returns(TypeName.get(element.asType()));
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
        return serializerFor(TypeName.get(type));
    }

    static TypeName boxedType(TypeName type) {
        if (type.isPrimitive()) {
            return primtiveType(type);
        }

        return type;
    }

    static TypeName serializerFor(TypeName type) {
        if (type.isPrimitive()) {
            return serializerFor(primtiveType(type));
        }

        return ParameterizedTypeName.get(ClassName.get(Serializer.class), type);
    }

    static ClassName primtiveType(TypeName type) {
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

    static SerializedType findCreatorConstructor(final Element root, final AutoSerialize autoSerialize) {
        final TypeName elementType = TypeName.get(root.asType());

        final List<SerializedFieldType> fieldTypes = new ArrayList<>();

        for (final Element e : root.getEnclosedElements()) {
            if (e.getKind() != ElementKind.FIELD) {
                continue;
            }
            // skip static fields.

            if (e.getModifiers().contains(Modifier.STATIC)) {
                continue;
            }

            if (e.getAnnotation(AutoSerialize.Ignore.class) != null) {
                continue;
            }

            final TypeName fieldType = TypeName.get(e.asType());
            final TypeName serializedFieldType = getSerializedType(fieldType);
            final boolean optional = isParameterOptional(fieldType);
            final boolean useGetter = isParameterUsingGetter(e, autoSerialize);
            final boolean provided = isParameterProvided(e);
            final String accessor = accessorForField(e, useGetter);

            fieldTypes.add(new SerializedFieldType(accessor, fieldType, serializedFieldType, optional, provided));
        }

        return new SerializedType(root, elementType, fieldTypes);
    }

    static boolean isParameterProvided(Element e) {
        final AutoSerialize.Field field;

        if ((field = e.getAnnotation(AutoSerialize.Field.class)) != null) {
            return field.provided();
        }

        return false;
    }

    static boolean isParameterUsingGetter(Element e, AutoSerialize autoSerialize) {
        final AutoSerialize.Field field;

        if ((field = e.getAnnotation(AutoSerialize.Field.class)) != null) {
            return field.useGetter();
        }

        return autoSerialize.useGetter();
    }

    static boolean accessorMethodExists(final Element root, final String accessor, final TypeName serializedType) {
        for (final Element enclosed : root.getEnclosedElements()) {
            if (enclosed.getKind() != ElementKind.METHOD) {
                continue;
            }

            final ExecutableElement executable = (ExecutableElement) enclosed;

            if (!executable.getSimpleName().toString().equals(accessor)) {
                continue;
            }

            if (!executable.getParameters().isEmpty()) {
                continue;
            }

            final TypeName returnType = TypeName.get(executable.getReturnType());

            return serializedType.equals(returnType);
        }

        return false;
    }

    static String accessorForField(Element e, final boolean useGetter) {
        final AutoSerialize.Field field;

        if ((field = e.getAnnotation(AutoSerialize.Field.class)) != null && !"".equals(field.accessor())) {
            return field.accessor();
        }

        final String accessor = e.getSimpleName().toString();

        if (!useGetter) {
            return accessor;
        }

        return "get" + CaseFormat.LOWER_CAMEL.to(CaseFormat.UPPER_CAMEL, accessor);
    }

    static TypeName getSerializedType(TypeName type) {
        if (type instanceof ParameterizedTypeName) {
            final ParameterizedTypeName parameterized = (ParameterizedTypeName) type;

            if (parameterized.rawType.equals(optionalPropertyType)) {
                return parameterized.typeArguments.iterator().next();
            }
        }

        return type;
    }

    static boolean isParameterOptional(final TypeName type) {
        if (type instanceof ParameterizedTypeName) {
            final ParameterizedTypeName parameterized = (ParameterizedTypeName) type;
            return parameterized.rawType.equals(optionalPropertyType);
        }

        return false;
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

    static void serializeOptionalParameter(final MethodSpec.Builder b, final SerializedField field,
            final ParameterSpec buffer, final ParameterSpec value, FieldSpec optional) {
        b.beginControlFlow("");
        {
            final TypeName optionalType = ParameterizedTypeName.get(optionalPropertyType,
                    field.type.serializedFieldType);
            b.addStatement("final $T o = $N.$L()", optionalType, value, field.type.accessor);

            b.beginControlFlow("if (o.isPresent())");
            {
                b.addStatement("$N.serialize($N, $L)", optional, buffer, true);
                b.addStatement("$N.serialize($N, o.get())", field.fieldSpec, buffer);
            }
            b.endControlFlow();

            b.beginControlFlow("else");
            {
                b.addStatement("$N.serialize($N, $L)", optional, buffer, false);
            }
            b.endControlFlow();
        }
        b.endControlFlow();
    }

    static void deserializeOptionalParameter(final MethodSpec.Builder b, final SerializedField field,
            final ParameterSpec buffer, final FieldSpec optional, final String name) {
        final TypeName optionalType = ParameterizedTypeName.get(optionalPropertyType, field.type.serializedFieldType);
        b.addStatement("final $T $L", optionalType, name);

        b.beginControlFlow("if ($N.deserialize($N))", optional, buffer);
        {
            final TypeName presentType = ParameterizedTypeName
                    .get(presentPropertytType, field.type.serializedFieldType);
            b.addStatement("$L = new $T($N.deserialize($N))", name, presentType, field.fieldSpec, buffer);
        }
        b.endControlFlow();

        b.beginControlFlow("else");
        {
            b.addStatement("$L = $T.absent()", name, absentPropertyType);
        }
        b.endControlFlow();
    }

    static class ProcessedSerializer {
        final String packageName;
        final String name;
        final TypeSpec type;
        final TypeName elementType;
        final TypeName supertype;
        final List<SerializedField> fields;

        public ProcessedSerializer(String packageName, String name, TypeSpec type, TypeName elementType,
                TypeName supertype, List<SerializedField> fields) {
            this.packageName = packageName;
            this.name = name;
            this.type = type;
            this.elementType = elementType;
            this.supertype = supertype;
            this.fields = fields;
        }

        public JavaFile asJavaFile() {
            return JavaFile.builder(packageName, type).skipJavaLangImports(true).indent("    ").build();
        }
    }

    static class SerializedType {
        static final List<SerializedFieldType> EMPTY_FIELDS = ImmutableList.of();

        final Element root;
        final TypeName type;
        final List<SerializedFieldType> fieldTypes;
        final List<SerializedField> fields;
        final boolean anyOptional;

        public SerializedType(final Element root, final TypeName type) {
            this(root, type, EMPTY_FIELDS);
        }

        public SerializedType(Element root, TypeName type, List<SerializedFieldType> fieldTypes) {
            this.root = root;
            this.type = type;
            this.fieldTypes = fieldTypes;
            this.fields = fields(fieldTypes);
            this.anyOptional = hasAnyOptional(fieldTypes);
        }

        SerializedType verify() {
            for (final SerializedFieldType field : fieldTypes) {
                final TypeName expected = field.optional ? field.fieldType : field.serializedFieldType;

                if (!accessorMethodExists(root, field.accessor, expected)) {
                    throw new IllegalStateException(String.format("Accessor #%s() does not exist on type %s",
                            field.accessor,
                            root.asType()));
                }
            }

            return this;
        }

        static List<SerializedField> fields(List<SerializedFieldType> parameters) {
            final Set<Pair<TypeName, Boolean>> defined = new HashSet<>();
            final Collection<SerializedFieldType> types = new ArrayList<>();

            for (final SerializedFieldType p : parameters) {
                final Pair<TypeName, Boolean> key = Pair.of(p.serializedFieldType, p.provided);

                if (defined.contains(key)) {
                    continue;
                }

                defined.add(key);
                types.add(p);
            }

            int i = 0;

            final List<SerializedField> serializedTypes = new ArrayList<>();

            for (final SerializedFieldType p : types) {
                final FieldSpec field = FieldSpec.builder(serializerFor(p.serializedFieldType), String.format("s%d", i++))
                        .addModifiers(Modifier.FINAL).build();

                serializedTypes.add(new SerializedField(field, p));
            }

            return ImmutableList.copyOf(serializedTypes);
        }

        static boolean hasAnyOptional(final Iterable<SerializedFieldType> fields) {
            for (final SerializedFieldType p : fields) {
                if (p.optional) {
                    return true;
                }
            }

            return false;
        }
    }

    static class SerializedFieldType {
        final String accessor;
        final TypeName fieldType;
        final TypeName serializedFieldType;
        final boolean optional;
        final boolean provided;

        public SerializedFieldType(final String accessor, final TypeName fieldType, final TypeName serializedFieldType,
                final boolean optional, final boolean provided) {
            this.accessor = accessor;
            this.fieldType = fieldType;
            this.serializedFieldType = serializedFieldType;
            this.optional = optional;
            this.provided = provided;
        }
    }

    static class SerializedField {
        final FieldSpec fieldSpec;
        final SerializedFieldType type;

        public SerializedField(FieldSpec field, SerializedFieldType type) {
            this.fieldSpec = field;
            this.type = type;
        }
    }

    static class SubType {
        final ClassName type;
        final short id;

        public SubType(ClassName type, short id) {
            this.type = type;
            this.id = id;
        }
    }
}