package eu.toolchain.serializer.processor;

import com.google.auto.service.AutoService;
import com.google.common.base.Throwables;
import com.google.common.collect.ImmutableSet;
import com.squareup.javapoet.JavaFile;
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
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import lombok.Data;

@AutoService(Processor.class)
public class AutoSerializeProcessor extends AbstractProcessor {
  private Filer filer;
  private Messager messager;
  private AutoSerializeUtils utils;
  private ClassProcessor classProcessor;

  private final List<DeferredProcessing> deferred = new ArrayList<>();

  @Override
  public void init(final ProcessingEnvironment env) {
    super.init(env);

    filer = env.getFiler();
    messager = new PrefixingMessager("@AutoSerialize", env.getMessager());

    final Elements elements = env.getElementUtils();
    final Types types = env.getTypeUtils();

    utils = new AutoSerializeUtils(types, elements);
    classProcessor = new ClassProcessor(types, elements, utils);

    if (env.getClass().getPackage().getName().startsWith("org.eclipse.jdt.")) {
      warnAboutBugEclipse300408();
    }
  }

  /**
   * Eclipse JDT does not preserve the original order of type fields, causing some Processor
   * assumptions to fail.
   */
  void warnAboutBugEclipse300408() {
    messager.printMessage(Diagnostic.Kind.WARNING,
      "@AutoSerialize processor might not work properly in Eclipse < 3.5, see https://bugs" +
        ".eclipse.org/bugs/show_bug.cgi?id=300408");
  }

  @Override
  public boolean process(Set<? extends TypeElement> typeElements, RoundEnvironment env) {
    final ImmutableSet.Builder<DeferredProcessing> elementsToProcess = ImmutableSet.builder();

    if (env.processingOver()) {
      for (final DeferredProcessing d : deferred) {
        d.getBroken().accept(messager);
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
        messager.printMessage(Diagnostic.Kind.WARNING,
          String.format("Skipping non-type element %s", e));
        continue;
      }

      elementsToProcess.add(new DeferredProcessing((TypeElement) e, messager -> {
      }));
    }

    final List<Processed> results = new ArrayList<>();

    for (final DeferredProcessing p : elementsToProcess.build()) {
      try {
        final JavaFile file = processElement(p.getElement());
        results.add(new Processed(file, p.getElement()));
      } catch (final BrokenException broken) {
        deferred.add(p.withBroken(broken.getWriter()));
      } catch (final Exception e) {
        messager.printMessage(Diagnostic.Kind.ERROR,
          "Failed to process:\n" + Throwables.getStackTraceAsString(e), p.getElement());
      }
    }

    for (final Processed p : results) {
      final JavaFile file = p.getFile();

      try {
        file.writeTo(filer);
      } catch (final Exception e) {
        messager.printMessage(Diagnostic.Kind.ERROR,
          "Failed to write:\n" + Throwables.getStackTraceAsString(e), p.getElement());
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

  JavaFile processElement(TypeElement element) {
    return classProcessor.process(element);
  }

  @Data
  public static class Processed {
    private final JavaFile file;
    private final Element element;
  }
}
