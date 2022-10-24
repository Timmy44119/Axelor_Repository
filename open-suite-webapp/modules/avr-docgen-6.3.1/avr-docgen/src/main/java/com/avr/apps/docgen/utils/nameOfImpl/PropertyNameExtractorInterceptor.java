package com.avr.apps.docgen.utils.nameOfImpl;

import net.bytebuddy.implementation.bind.annotation.Origin;
import net.bytebuddy.implementation.bind.annotation.RuntimeType;

import java.lang.reflect.Method;

/**
 * *********************************** AVR SOLUTIONS ***********************************
 *
 * @author David
 * @version 1.0
 * @date 05/04/2022
 * @time 12:29
 * @Update 05/04/2022
 */
public class PropertyNameExtractorInterceptor {

    private static final ThreadLocal<String> currentExtractedMethodName = new ThreadLocal<>();

    @RuntimeType
    public static Object intercept(@Origin Method method) {
        currentExtractedMethodName.set(getPropertyName(method));

        if (method.getReturnType() == byte.class) {
            return (byte) 0;
        }
        if (method.getReturnType() == int.class) {
            return 0;
        }
        if (method.getReturnType() == long.class) {
            return (long) 0;
        }
        if (method.getReturnType() == char.class) {
            return (char) 0;
        }
        if (method.getReturnType() == short.class) {
            return (short) 0;
        }
        return null;
    }

    private static String getPropertyName(Method method) {
        if (method.getParameterTypes().length == 0
                && method.getReturnType() != Void.TYPE) {
            String name = method.getName();

            if (name.startsWith("get")) {
                return name.substring(3, 4).toLowerCase() + name.substring(4);
            } else if (name.startsWith("is")) {
                return name.substring(2, 3).toLowerCase() + name.substring(3);
            }
        }

        throw new RuntimeException("Only property getter methods are expected to be passed");
    }

    public static String extractMethodName() {
        String methodName = currentExtractedMethodName.get();
        currentExtractedMethodName.remove();
        return methodName;
    }
}