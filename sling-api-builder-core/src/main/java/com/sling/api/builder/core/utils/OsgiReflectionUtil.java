package com.sling.api.builder.core.utils;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.tuple.Pair;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.stream.Stream;

public final class OsgiReflectionUtil {

    private OsgiReflectionUtil() {
    }

    /**
     * Get service with this class using provided class loader
     *
     * @param loader           {@link ClassLoader}
     * @param serviceName      service name which is looking for
     * @param logger           logging action
     * @param generator        generator for new service from existing class
     * @param injectedServices injected services into current one
     * @return pair of service class and service instance (null if not service found)
     */
    public static Pair<Class<?>, Object> getServiceInfo(ClassLoader loader, String serviceName, Consumer<Exception> logger,
                                                        Function<Class<?>, Object> generator, Pair<String, Object>... injectedServices) {
        try {
            final Class<?> serviceClass = loader.loadClass(serviceName);
            if (ArrayUtils.isEmpty(injectedServices)) {
                return Pair.of(serviceClass, generator.apply(serviceClass));
            }
            final Function<Class<?>, Object> injectedServicesGenerator = createServiceWithInitializedServices(logger, generator, injectedServices);
            return Pair.of(serviceClass, injectedServicesGenerator.apply(serviceClass));
        } catch (ClassNotFoundException e) {
            logger.accept(e);
        }
        return Pair.of(Object.class, new Object());
    }

    /**
     * Get service with this class using provided class loader
     *
     * @param loader           {@link ClassLoader}
     * @param serviceName      service name which is looking for
     * @param logger           logging action
     * @param injectedServices injected services into current one
     * @return pair of service class and service instance (null if not service found)
     */
    public static Pair<Class<?>, Object> getServiceInfo(ClassLoader loader, String serviceName, Consumer<Exception> logger,
                                                        Pair<String, Object>... injectedServices) {
        return getServiceInfo(loader, serviceName, logger, createNewService(logger), injectedServices);
    }

    /**
     * Get service with this class using provided class loader
     *
     * @param loader      {@link ClassLoader}
     * @param serviceName service name which is looking for
     * @param logger      logging action
     * @param generator   generator for new service from existing class
     * @return pair of service class and service instance (null if not service found)
     */
    public static Pair<Class<?>, Object> getServiceInfo(ClassLoader loader, String serviceName, Consumer<Exception> logger,
                                                        Function<Class<?>, Object> generator) {
        return getServiceInfo(loader, serviceName, logger, generator, null);
    }

    /**
     * Get service with this class using provided class loader
     *
     * @param loader      {@link ClassLoader}
     * @param serviceName service name which is looking for
     * @param logger      logging action
     * @return pair of service class and service instance (null if not service found)
     */
    public static Pair<Class<?>, Object> getServiceInfo(ClassLoader loader, String serviceName, Consumer<Exception> logger) {
        return getServiceInfo(loader, serviceName, logger, createNewService(logger));
    }

    /**
     * Generator for service. Create new instance with initialized services
     *
     * @param logger           logging action
     * @param generator        generator for new service from existing class
     * @param injectedServices pairs of injected services (field name and service object)
     * @return generation function
     */
    private static Function<Class<?>, Object> createServiceWithInitializedServices(Consumer<Exception> logger, Function<Class<?>, Object> generator,
                                                                                   Pair<String, Object>... injectedServices) {
        return serviceClass -> {
            Object service = generator.apply(serviceClass);
            try {
                initFields(service, serviceClass, injectedServices);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                logger.accept(e);
            }
            return service;
        };
    }

    /**
     * Generator for service. Create new instance with initialized services
     *
     * @param logger           logging action
     * @param injectedServices pairs of injected services (field name and service object)
     * @return generation function
     */
    private static Function<Class<?>, Object> createServiceWithInitializedServices(Consumer<Exception> logger, Pair<String, Object>... injectedServices) {
        return createServiceWithInitializedServices(logger, createNewService(logger), injectedServices);
    }

    private static void initFields(Object service, Class<?> serviceClass, Pair<String, Object>[] injectedServices)
            throws NoSuchFieldException, IllegalAccessException {
        for (Pair<String, Object> injectedService : injectedServices) {
            Field field = serviceClass.getDeclaredField(injectedService.getKey());
            final boolean access = field.isAccessible();
            field.setAccessible(true);
            field.set(service, injectedService.getValue());
            field.setAccessible(access);
        }
    }

    private static Function<Class<?>, Object> createNewService(Consumer<Exception> logger) {
        return serviceClass -> {
            try {
                return serviceClass.newInstance();
            } catch (InstantiationException | IllegalAccessException e) {
                logger.accept(e);
            }
            return null;
        };
    }

    /**
     * Invoke method using reflection
     *
     * @param serviceInfo      class and service instance
     * @param methodName       method name
     * @param logger           logger function
     * @param methodParameters method parameters
     * @return result of method invocation
     */
    public static Object invokeMethod(Pair<Class<?>, Object> serviceInfo, String methodName, Consumer<Exception> logger,
                                      MethodParameter... methodParameters) {
        final Class<?>[] classes = parametersToClasses(methodParameters);
        final Object[] args = parametersToArgs(methodParameters);
        try {
            final Method method = serviceInfo.getKey().getMethod(methodName, classes);
            return invokeMethodWithAccessor(serviceInfo.getValue(), method, logger, args);
        } catch (NoSuchMethodException e) {
            return invokeMethodFromDeclaredMethods(serviceInfo, methodName, logger, classes, args);
        }
    }

    /**
     * Invoke method using reflection
     *
     * @param serviceInfo      class and service instance
     * @param methodName       method name
     * @param logger           logger function
     * @param methodParameters method parameters
     * @return result of method invocation
     */
    public static <T> T invokeMethod(Pair<Class<?>, Object> serviceInfo, String methodName, Consumer<Exception> logger,
                                     Class<T> resultClass, MethodParameter... methodParameters) {
        return resultClass.cast(invokeMethod(serviceInfo, methodName, logger, methodParameters));
    }

    private static Object invokeMethodFromDeclaredMethods(Pair<Class<?>, Object> serviceInfo, String methodName,
                                                          Consumer<Exception> logger, Class<?>[] classes, Object[] args) {
        return Stream.of(serviceInfo.getKey().getDeclaredMethods())
                .filter(method -> method.getName().equals(methodName))
                .filter(method -> Arrays.deepEquals(method.getParameterTypes(), classes))
                .findFirst()
                .map(method -> invokeMethodWithAccessor(serviceInfo.getValue(), method, logger, args))
                .orElse(null);
    }

    private static Object invokeMethodWithAccessor(Object service, Method method, Consumer<Exception> logger, Object... args) {
        final boolean accessible = method.isAccessible();
        method.setAccessible(true);
        try {
            return method.invoke(service, args);
        } catch (IllegalAccessException | InvocationTargetException e) {
            logger.accept(e);
        } finally {
            method.setAccessible(accessible);
        }
        return null;
    }

    private static Object[] parametersToArgs(MethodParameter[] parameters) {
        return toParameters(parameters, MethodParameter::getParameter, Object[]::new);
    }

    private static Class[] parametersToClasses(MethodParameter[] parameters) {
        return toParameters(parameters, MethodParameter::getParameterClass, Class[]::new);
    }

    private static <T> T[] toParameters(MethodParameter[] methodParameters, Function<MethodParameter, T> function,
                                        IntFunction<T[]> generator) {
        if (methodParameters == null) {
            return Stream.<T>empty().toArray(generator);
        }
        return Stream.of(methodParameters)
                .map(function)
                .toArray(generator);
    }

    public static class MethodParameter<T> {
        private final Class<T> parameterClass;
        private final T parameter;

        public MethodParameter(T parameter, Class<T> parameterClass) {
            this.parameter = parameter;
            this.parameterClass = parameterClass;
        }

        public MethodParameter(T parameter) {
            this(parameter, (Class<T>) parameter.getClass());
        }

        public static <V> MethodParameter[] of(V... values) {
            return Stream.of(values)
                    .toArray(MethodParameter[]::new);
        }

        private Class<T> getParameterClass() {
            return parameterClass;
        }

        private T getParameter() {
            return parameter;
        }
    }
}
