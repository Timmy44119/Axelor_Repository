package com.avr.apps.docgen.utils.nameOfImpl;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.ClassFileVersion;
import net.bytebuddy.dynamic.DynamicType;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import net.bytebuddy.implementation.MethodDelegation;
import net.bytebuddy.matcher.ElementMatchers;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * *********************************** AVR SOLUTIONS ***********************************
 *
 * @author David
 * @version 1.0
 * @date 05/04/2022
 * @time 12:28
 * @Update 05/04/2022
 */
public final class PropertyNames {
    @SuppressWarnings("unchecked")
    public static <T> T getPropertyNameExtractor(Class<T> type) {
        DynamicType.Builder<?> builder = new ByteBuddy(ClassFileVersion.JAVA_V8)
                .subclass(type.isInterface() ? Object.class : type);

        if (type.isInterface()) {
            builder = builder.implement(type);
        }

        Class<?> proxyType = builder
                .method(ElementMatchers.any())
                .intercept(MethodDelegation.to(PropertyNameExtractorInterceptor.class))
                .make()
                .load(
                        PropertyNames.class.getClassLoader(),
                        ClassLoadingStrategy.Default.WRAPPER
                )
                .getLoaded();

        try {
            Constructor<?> constructor = proxyType.getConstructor();
            return (T) constructor.newInstance();
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            throw new RuntimeException(
                    "Couldn't instantiate proxy for method name retrieval", e
            );
        }
    }
}