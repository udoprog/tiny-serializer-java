package eu.toolchain.serializer.processor;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableSet;
import com.squareup.javapoet.JavaFile;

import eu.toolchain.serializer.processor.annotation.AutoSerializeMirror;
import eu.toolchain.serializer.processor.unverified.Unverified;
import lombok.Data;

@AutoService(Processor.class)
public class AutoSerializeProcessor extends AbstractProcessor {
    private Filer filer;
    private Messager messager;
    private AutoSerializeUtils utils;
    private FrameworkStatements statements;
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

        statements = new FrameworkStatements();
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
                d.getBroken().get().writeError(messager);
            }

            return false;
        }

        // failing TypeElement's from last round
        if (!deferred.isEmpty()) {
            elementsToProcess.addAll(deferred.stream().map(DeferredProcessing.refresh(utils)).iterator());
            deferred.clear();
        }

        for (final Element e : env.getElementsAnnotatedWith(utils.autoSerializeType())) {
            if (!(e instanceof TypeElement)) {
                messager.printMessage(Diagnostic.Kind.WARNING, String.format("Skipping non-type element %s", e));
                continue;
            }

            elementsToProcess.add(new DeferredProcessing((TypeElement)e, Optional.empty()));
        }

        final List<Processed> processed = processElements(elementsToProcess.build());

        for (final Processed p : processed) {
            final Unverified<JavaFile> serializer = p.getFile();

            if (!serializer.isVerified()) {
                deferred.add(p.processing.withBroken(serializer));
                continue;
            }

            try {
                serializer.get().writeTo(filer);
            } catch (final Exception e) {
                messager.printMessage(Diagnostic.Kind.ERROR, "Failed to write:\n" + Throwables.getStackTraceAsString(e), p.getProcessing().getElement());
            }
        }

        return false;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return ImmutableSet.of(AutoSerializeUtils.AUTOSERIALIZE);
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    List<Processed> processElements(Set<DeferredProcessing> elements) {
        final List<Processed> processed = new ArrayList<>();

        for (final DeferredProcessing processing : elements) {
            messager.printMessage(Diagnostic.Kind.NOTE, String.format("Processing %s", processing.getElement()));

            final Unverified<JavaFile> result = processElement(processing.getElement());
            processed.add(new Processed(result, processing));
        }

        return processed;
    }

    Unverified<JavaFile> processElement(TypeElement element) {
        final Optional<Unverified<AutoSerializeMirror>> annotation = utils.autoSerialize(element);

        if (!annotation.isPresent()) {
            return Unverified.brokenElement("@AutoSerialize annotation not present", element);
        }

        final Unverified<AutoSerializeMirror> unverifiedAutoSerialize = annotation.get();

        return unverifiedAutoSerialize.<JavaFile> transform((autoSerialize) -> {
            if (element.getKind() == ElementKind.INTERFACE) {
                if (autoSerialize.getBuilder().isPresent()) {
                    return classProcessor.process(element, autoSerialize);
                } else {
                    return abstractProcessor.process(element, autoSerialize);
                }
            }

            if (element.getKind() == ElementKind.CLASS) {
                if (element.getModifiers().contains(Modifier.ABSTRACT) && !autoSerialize.getBuilder().isPresent()) {
                    return abstractProcessor.process(element, autoSerialize);
                }

                return classProcessor.process(element, autoSerialize);
            }

            return Unverified.brokenElement("Unsupported type, expected class or interface", element);
        });
    }

    @Data
    public static class Processed {
        final Unverified<JavaFile> file;
        final DeferredProcessing processing;
    }
}