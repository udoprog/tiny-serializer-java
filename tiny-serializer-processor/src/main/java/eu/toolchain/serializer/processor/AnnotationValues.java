package eu.toolchain.serializer.processor;

import java.util.List;
import java.util.Map;

import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.SimpleAnnotationValueVisitor8;

import com.google.common.collect.ImmutableList;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class AnnotationValues {
    private final Map<String, AnnotationValue> values;

    public boolean getBoolean(final String key) {
        final AnnotationValue annotationValue = values.get(key);

        if (annotationValue == null) {
            throw new IllegalArgumentException(key);
        }

        return annotationValue.accept(new SimpleAnnotationValueVisitor8<Boolean, Void>() {
            @Override
            public Boolean visitBoolean(boolean b, Void p) {
                return b;
            }

            @Override
            protected Boolean defaultAction(Object o, Void p) {
                throw new IllegalArgumentException();
            }
        }, null);
    }

    public TypeMirror getTypeMirror(final String key) {
        final AnnotationValue annotationValue = values.get(key);

        if (annotationValue == null) {
            throw new IllegalArgumentException(key);
        }

        return annotationValue.accept(new SimpleAnnotationValueVisitor8<TypeMirror, Void>() {
            @Override
            public TypeMirror visitType(TypeMirror t, Void p) {
                return t;
            }

            @Override
            protected TypeMirror defaultAction(Object o, Void p) {
                throw new IllegalArgumentException();
            }
        }, null);
    }

    public String getString(final String key) {
        final AnnotationValue annotationValue = values.get(key);

        if (annotationValue == null) {
            throw new IllegalArgumentException(key);
        }

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

    public int getInteger(String key) {
        final AnnotationValue annotationValue = values.get(key);

        if (annotationValue == null) {
            throw new IllegalArgumentException(key);
        }

        return annotationValue.accept(new SimpleAnnotationValueVisitor8<Integer, Void>() {
            @Override
            public Integer visitInt(int i, Void p) {
                return i;
            }

            @Override
            protected Integer defaultAction(Object o, Void p) {
                throw new IllegalArgumentException();
            }
        }, null);
    }

    public List<AnnotationMirror> getAnnotationValue(String key) {
        final AnnotationValue annotationValue = values.get(key);

        if (annotationValue == null) {
            throw new IllegalArgumentException(key);
        }

        return annotationValue.accept(new SimpleAnnotationValueVisitor8<List<AnnotationMirror>, Void>() {
            @Override
            public List<AnnotationMirror> visitAnnotation(AnnotationMirror a, Void p) {
                return ImmutableList.of(a);
            }

            @Override
            public List<AnnotationMirror> visitArray(List<? extends AnnotationValue> vals, Void p) {
                final ImmutableList.Builder<AnnotationMirror> mirrors = ImmutableList.builder();

                for (final AnnotationValue val : vals) {
                    mirrors.add(val.accept(new SimpleAnnotationValueVisitor8<AnnotationMirror, Void>() {
                        public AnnotationMirror visitAnnotation(AnnotationMirror a, Void p) {
                            return a;
                        };

                        @Override
                        protected AnnotationMirror defaultAction(Object o, Void p) {
                            throw new IllegalArgumentException();
                        }
                    }, null));
                }

                return mirrors.build();
            }

            @Override
            protected List<AnnotationMirror> defaultAction(Object o, Void p) {
                throw new IllegalArgumentException();
            }
        }, null);
    }
}
