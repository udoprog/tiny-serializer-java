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
import com.google.common.base.Joiner;
import com.google.common.base.Optional;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.squareup.javapoet.JavaFile;

import eu.toolchain.serializer.AutoSerialize;

@AutoService(Processor.class)
public class AutoSerializeProcessor extends AbstractProcessor {
    private static final Joiner lineJoiner = Joiner.on('\n');

    private Filer filer;
    private Messager messager;
    private AutoSerializeUtils utils;
    private AutoSerializeAbstractProcessor abstractProcessor;
    private AutoSerializeClassProcessor classProcessor;

    private final List<DeferredProcessing> deferred = new ArrayList<>();

    @Override
    public void init(final ProcessingEnvironment env) {
        super.init(env);

        filer = env.getFiler();
        messager = new PrefixingMessager("@AutoSerialize", env.getMessager());

        final Elements elements = env.getElementUtils();
        final Types types = env.getTypeUtils();
        final FrameworkStatements statements = new FrameworkStatements();

        utils = new AutoSerializeUtils(types, elements);
        abstractProcessor = new AutoSerializeAbstractProcessor(elements, statements, utils);
        classProcessor = new AutoSerializeClassProcessor(types, elements, statements, utils);

        if (env.getClass().getPackage().getName().startsWith("org.eclipse.jdt.")) {
            warnAboutBugEclipse300408();
        }
    }

    /**
     * Eclipse JDT does not preserve the original order of type fields, causing some Processor assumptions to fail.
     */
    void warnAboutBugEclipse300408() {
        messager.printMessage(Diagnostic.Kind.WARNING,
                "@AutoSerialize processor might not work properly in Eclipse < 3.5, see https://bugs.eclipse.org/bugs/show_bug.cgi?id=300408");
    }

    @Override
    public boolean process(Set<? extends TypeElement> typeElements, RoundEnvironment env) {
        final ImmutableSet.Builder<DeferredProcessing> elementsToProcess = ImmutableSet.builder();

        if (env.processingOver()) {
            for (final DeferredProcessing d : deferred) {
                for (final SerializedTypeError t : d.getLastError()) {
                    messager.printMessage(Diagnostic.Kind.ERROR, t.getMessage(), t.getElement().orElse(d.getElement()));
                }
            }

            return false;
        }

        // failing TypeElement's from last round
        if (!deferred.isEmpty()) {
            elementsToProcess.addAll(deferred.stream().map(DeferredProcessing.refresh(utils)).iterator());
            deferred.clear();
        }

        for (final Element e : env.getElementsAnnotatedWith(AutoSerialize.class)) {
            if (!(e instanceof TypeElement)) {
                messager.printMessage(Diagnostic.Kind.WARNING, String.format("Skipping non-type element %s", e));
                continue;
            }

            elementsToProcess.add(new DeferredProcessing((TypeElement)e, ImmutableList.of()));
        }

        final List<SerializedType> processed = processElements(elementsToProcess.build());

        for (final SerializedType p : processed) {
            final JavaFile output = p.asJavaFile();

            messager.printMessage(Diagnostic.Kind.NOTE,
                    String.format("Writing %s.%s", output.packageName, output.typeSpec.name), p.getRoot());

            try {
                output.writeTo(filer);
            } catch (IOException e) {
                messager.printMessage(Diagnostic.Kind.ERROR, "Failed to write:\n" + Throwables.getStackTraceAsString(e), p.getRoot());
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

    List<SerializedType> processElements(Set<DeferredProcessing> elements) {
        final List<SerializedType> processed = new ArrayList<>();

        for (final DeferredProcessing processing : elements) {
            messager.printMessage(Diagnostic.Kind.NOTE, String.format("Processing %s", processing.getElement()));

            final Optional<SerializedType> result;

            try {
                result = processElement(processing.getElement());
            } catch(Exception e) {
                deferred.add(processing.withError(SerializedTypeError.fromException(e)));
                continue;
            }

            if (result.isPresent()) {
                final SerializedType s = result.get();

                final List<SerializedTypeError> errors = s.validate();

                if (!errors.isEmpty()) {
                    deferred.add(processing.withErrors(errors));
                    continue;
                }

                processed.add(s);
            }
        }

        return processed;
    }

    Optional<SerializedType> processElement(TypeElement element) {
        if (element.getKind() == ElementKind.INTERFACE) {
            if (utils.useBuilder(element)) {
                return Optional.of(classProcessor.process(element));
            } else {
                return Optional.of(abstractProcessor.process(element));
            }
        }

        if (element.getKind() == ElementKind.CLASS) {
            if (element.getModifiers().contains(Modifier.ABSTRACT) && !utils.useBuilder(element)) {
                return Optional.of(abstractProcessor.process(element));
            }

            return Optional.of(classProcessor.process(element));
        }

        messager.printMessage(Diagnostic.Kind.WARNING,
                String.format("Cannot process element (%s) of kind %s", element.toString(), element.getKind()));
        return Optional.absent();
    }
}