package eu.toolchain.serializer.processor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
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
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableSet;
import com.squareup.javapoet.JavaFile;

import eu.toolchain.serializer.AutoSerialize;

@AutoService(Processor.class)
public class AutoSerializeProcessor extends AbstractProcessor {
    private Filer filer;
    private Messager messager;
    private AutoSerializeUtils utils;
    private AutoSerializeAbstractProcessor abstractProcessor;
    private AutoSerializeClassProcessor classProcessor;
    private int round = 0;

    @Override
    public void init(final ProcessingEnvironment env) {
        super.init(env);

        filer = env.getFiler();
        messager = env.getMessager();

        final Elements elements = env.getElementUtils();
        final Types types = env.getTypeUtils();
        final FrameworkStatements statements = new FrameworkStatements();

        utils = new AutoSerializeUtils(types, elements);
        abstractProcessor = new AutoSerializeAbstractProcessor(elements, statements, utils);
        classProcessor = new AutoSerializeClassProcessor(types, elements, statements, utils);

        /**
         * Eclipse JDT does not preserve the original order of type fields, causing some Processor assumptions to fail.
         */
        if (env.getClass().getPackage().getName().startsWith("org.eclipse.jdt.")) {
            messager.printMessage(Diagnostic.Kind.WARNING,
                    "@AutoSerialize processor might not work properly in Eclipse");
        }
    }

    @Override
    public boolean process(Set<? extends TypeElement> typeElements, RoundEnvironment env) {
        messager.printMessage(Diagnostic.Kind.NOTE, String.format("processing round %d", round++));

        final List<SerializedType> processed = processElements(env.getElementsAnnotatedWith(AutoSerialize.class));

        for (final SerializedType p : processed) {
            if (!p.isValid(messager)) {
                messager.printMessage(Diagnostic.Kind.WARNING,
                        String.format("Might not be valid: %s", p.getElementType()));
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

        return false;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return ImmutableSet.of(AutoSerialize.class.getName());
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    List<SerializedType> processElements(Set<? extends Element> elements) {
        final List<SerializedType> processed = new ArrayList<>();

        for (final Element element : elements) {
            if (!(element instanceof TypeElement)) {
                throw new IllegalArgumentException(String.format("Element unsupported (%s), expected Type (interface or class)", element));
            }

            final TypeElement typeElement = (TypeElement) element;
            messager.printMessage(Diagnostic.Kind.NOTE, String.format("Processing %s", typeElement));
            processed.add(processElement(typeElement));
        }

        return processed;
    }

    SerializedType processElement(TypeElement element) {
        if (element.getKind() == ElementKind.INTERFACE) {
            if (utils.useBuilder(element)) {
                return classProcessor.process(element);
            } else {
                return abstractProcessor.process(element);
            }
        }

        if (element.getKind() == ElementKind.CLASS) {
            if (element.getModifiers().contains(Modifier.ABSTRACT) && !utils.useBuilder(element)) {
                return abstractProcessor.process(element);
            }

            return classProcessor.process(element);
        }

        throw new IllegalArgumentException("Unexpected type " + element);
    }
}