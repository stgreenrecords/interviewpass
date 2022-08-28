package app.core.api.utils;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;
import org.osgi.framework.Bundle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

public final class ReflectionUtil {

    private static final Logger LOG = LoggerFactory.getLogger(ReflectionUtil.class);

    private ReflectionUtil() {
    }

    public static <A extends Annotation> String getName(Field field, Class<A> annotationClass, Function<A, String> function) {
        if (isAnnotatedWith(annotationClass).test(field) && !StringUtils.EMPTY.equals(field.getAnnotation(ValueMapValue.class).name())) {
            return function.apply(field.getAnnotation(annotationClass));
        }
        return field.getName();
    }

    public static String getNamedValue(Field field) {
        return getName(field, ValueMapValue.class, ValueMapValue::name);
    }

    public static Function<Field, Object> getFiledValue(Object object) {
        return field -> {
            try {
                boolean accessible = field.isAccessible();
                field.setAccessible(true);
                Object value = field.get(object);
                field.setAccessible(accessible);
                return value;
            } catch (IllegalAccessException e) {
                return null;
            }
        };
    }

    public static Predicate<Field> isNotAnnotatedWith(Class<? extends Annotation> annotationClass) {
        return checkAnnotations(annotationClass, ArrayUtils::isEmpty);
    }

    public static Predicate<Field> isAnnotatedWith(Class<? extends Annotation> annotationClass) {
        return checkAnnotations(annotationClass, ArrayUtils::isNotEmpty);
    }

    public static Predicate<Field> checkAnnotations(Class<? extends Annotation> annotationClass,
                                                    Predicate<Annotation[]> function) {
        return field -> function.test(field.getDeclaredAnnotationsByType(annotationClass));
    }

    public static Class<?>[] getMethodGenericInfo(String methodName, Class<?> objectClass, Class<?> args) {
        try {
            Method method = objectClass.getMethod(methodName, args);
            return Stream.of(method.getGenericParameterTypes())
                    .filter(ParameterizedType.class::isInstance)
                    .map(ParameterizedType.class::cast)
                    .map(ParameterizedType::getActualTypeArguments)
                    .flatMap(Stream::of)
                    .map(Type.class::cast)
                    .toArray(Class<?>[]::new);
        } catch (NoSuchMethodException e) {
            LOG.error(e.getMessage(), e);
        }
        return new Class<?>[0];
    }


    public static boolean isFieldPresent(String fieldName, Object classObject) {
        return isFieldPresent(fieldName, classObject.getClass());
    }

    public static boolean isFieldPresent(String fieldName, Class<?> modelClass) {
        return Arrays.stream(modelClass.getDeclaredFields())
                .anyMatch(field -> field.getName().equals(fieldName));

    }

    public static void setFieldValue(String fieldName, String[] fieldValues, Object classObject, Class<?> modelClass) {
        try {
            Field field = modelClass.getDeclaredField(fieldName);
            Object value = fieldValues;
            if (!field.getType().isArray()) {
                value = fieldValues[0];
            }
            boolean accessible = field.isAccessible();
            field.setAccessible(true);
            field.set(classObject, value);
            field.setAccessible(accessible);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            LOG.error(e.getMessage());
        }
    }

    public static void setFieldValueDeep(String fieldName, String[] fieldValues, Object classObject) {
        final Class<?> realModelClass = getClassWithField(classObject.getClass(), fieldName);
        if (realModelClass != null) {
            setFieldValue(fieldName, fieldValues, classObject, realModelClass);
        }
    }

    private static Class<?> getClassWithField(Class<?> modelClass, String fieldName) {
        final boolean present = isFieldPresent(fieldName, modelClass);
        if (present) {
            return modelClass;
        }
        final Class<?> superClass = modelClass.getSuperclass();
        if (superClass != null) {
            return getClassWithField(superClass, fieldName);
        }
        return null;
    }

    public static String[] getAllClassesFromPackage(Bundle bundle, String packageName) {
        Enumeration classUrls = bundle.findEntries("/" + packageName.replace('.', '/'), "*.class", true);
        List<String> classes = new ArrayList<>();
        while (classUrls.hasMoreElements()) {
            URL url = (URL) classUrls.nextElement();
            classes.add(toClassName(url));
        }

        return classes.toArray(new String[]{});
    }

    private static String toClassName(URL url) {
        String f = url.getFile();
        String cn = f.substring(1, f.length() - ".class".length());
        return cn.replace('/', '.');
    }
}
