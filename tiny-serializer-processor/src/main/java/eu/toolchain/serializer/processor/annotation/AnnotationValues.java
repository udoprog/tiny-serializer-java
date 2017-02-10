package eu.toolchain.serializer.processor.annotation;

import static eu.toolchain.serializer.processor.Exceptions.brokenValue;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.Map;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.type.ErrorType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.SimpleAnnotationValueVisitor8;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AnnotationValues {
  private final Element element;
  private final AnnotationMirror annotation;
  private final Map<String, AnnotationValue> values;

  public AnnotationValue get(final String key) {
    final AnnotationValue annotationValue = values.get(key);

    if (annotationValue == null) {
      throw new IllegalArgumentException(key);
    }

    return annotationValue;
  }

  public Value<Boolean> getBoolean(final String key) {
    final AnnotationValue annotationValue = values.get(key);

    if (annotationValue == null) {
      throw new IllegalArgumentException(key);
    }

    return new Value<>(annotationValue, toBoolean(annotationValue));
  }

  public Value<TypeMirror> getTypeMirror(final String key) {
    final AnnotationValue annotationValue = values.get(key);

    if (annotationValue == null) {
      throw new IllegalArgumentException(key);
    }

    final TypeMirror typeMirror = toTypeMirror(annotationValue);

    if (typeMirror == null) {
      throw brokenValue("Could not resolve type", element, annotation, annotationValue);
    }

    if (typeMirror instanceof ErrorType) {
      throw brokenValue("Could not resolve type", element, annotation, annotationValue);
    }

    return new Value<>(annotationValue, typeMirror);
  }

  public Value<String> getString(final String key) {
    final AnnotationValue annotationValue = values.get(key);

    if (annotationValue == null) {
      throw new IllegalArgumentException(key);
    }

    return new Value<>(annotationValue, toString(annotationValue));
  }

  public Value<Short> getShort(String key) {
    final AnnotationValue annotationValue = values.get(key);

    if (annotationValue == null) {
      throw new IllegalArgumentException(key);
    }

    return new Value<>(annotationValue, toShort(annotationValue));
  }

  public Value<Integer> getInteger(String key) {
    final AnnotationValue annotationValue = values.get(key);

    if (annotationValue == null) {
      throw new IllegalArgumentException(key);
    }

    return new Value<>(annotationValue, toInteger(annotationValue));
  }

  public Value<List<AnnotationMirror>> getAnnotationValue(String key) {
    final AnnotationValue annotationValue = values.get(key);

    if (annotationValue == null) {
      throw new IllegalArgumentException(key);
    }

    return new Value<>(annotationValue, toAnnotationMirror(annotationValue));
  }

  public TypeMirror toTypeMirror(final AnnotationValue annotationValue) {
    return annotationValue.accept(new SimpleAnnotationValueVisitor8<TypeMirror, Void>() {
      @Override
      public TypeMirror visitType(TypeMirror t, Void p) {
        return t;
      }

      @Override
      protected TypeMirror defaultAction(Object o, Void p) {
        return null;
      }
    }, null);
  }

  public String toString(final AnnotationValue annotationValue) {
    return annotationValue.accept(new SimpleAnnotationValueVisitor8<String, Void>() {
      @Override
      public String visitString(String s, Void p) {
        return s;
      }

      @Override
      protected String defaultAction(Object o, Void p) {
        throw new IllegalArgumentException();
      }
    }, null);
  }

  public short toShort(final AnnotationValue annotationValue) {
    return annotationValue.accept(new SimpleAnnotationValueVisitor8<Short, Void>() {
      @Override
      public Short visitInt(int i, Void p) {
        return Integer.valueOf(i).shortValue();
      }

      @Override
      public Short visitShort(short s, Void p) {
        return s;
      }

      @Override
      protected Short defaultAction(Object o, Void p) {
        throw new IllegalArgumentException(
          String.format("Could not convert %s to Short", annotationValue));
      }
    }, null);
  }

  public int toInteger(final AnnotationValue annotationValue) {
    return annotationValue.accept(new SimpleAnnotationValueVisitor8<Integer, Void>() {
      @Override
      public Integer visitInt(int i, Void p) {
        return i;
      }

      @Override
      public Integer visitShort(short s, Void p) {
        return Short.valueOf(s).intValue();
      }

      @Override
      protected Integer defaultAction(Object o, Void p) {
        throw new IllegalArgumentException(
          String.format("Could not convert %s to Integer", annotationValue));
      }
    }, null);
  }

  public List<AnnotationMirror> toAnnotationMirror(final AnnotationValue annotationValue) {
    return annotationValue.accept(
      new SimpleAnnotationValueVisitor8<List<AnnotationMirror>, Void>() {
        @Override
        public List<AnnotationMirror> visitAnnotation(AnnotationMirror a, Void p) {
          return ImmutableList.of(a);
        }

        @Override
        public List<AnnotationMirror> visitArray(
          List<? extends AnnotationValue> vals, Void p
        ) {
          final ImmutableList.Builder<AnnotationMirror> mirrors = ImmutableList.builder();

          for (final AnnotationValue val : vals) {
            mirrors.add(val.accept(new SimpleAnnotationValueVisitor8<AnnotationMirror, Void>() {
              public AnnotationMirror visitAnnotation(
                AnnotationMirror a, Void p
              ) {
                return a;
              }

              ;

              @Override
              protected AnnotationMirror defaultAction(Object o, Void p) {
                throw new IllegalArgumentException(
                  String.format("Could not convert %s to AnnotationMirror", annotationValue));
              }
            }, null));
          }

          return mirrors.build();
        }

        @Override
        protected List<AnnotationMirror> defaultAction(Object o, Void p) {
          throw new IllegalArgumentException(
            String.format("Could not convert %s to AnnotationMirror", annotationValue));
        }
      }, null);
  }

  public boolean toBoolean(AnnotationValue annotationValue) {
    return annotationValue.accept(new SimpleAnnotationValueVisitor8<Boolean, Void>() {
      @Override
      public Boolean visitBoolean(boolean b, Void p) {
        return b;
      }

      @Override
      protected Boolean defaultAction(Object o, Void p) {
        throw new IllegalArgumentException(
          String.format("Could not convert %s to Boolean", annotationValue));
      }
    }, null);
  }

  @Data
  public static class Value<T> {
    private final AnnotationValue annotationValue;
    private final T value;

    public T get() {
      return value;
    }
  }
}
