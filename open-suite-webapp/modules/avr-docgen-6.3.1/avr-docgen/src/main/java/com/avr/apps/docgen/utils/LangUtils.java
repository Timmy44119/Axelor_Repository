package com.avr.apps.docgen.utils;

import com.avr.apps.docgen.utils.nameOfImpl.PropertyNameExtractorInterceptor;
import com.avr.apps.docgen.utils.nameOfImpl.PropertyNames;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/**
 * *********************************** AVR SOLUTIONS ***********************************
 *
 * @author David
 * @version 1.0
 * @date 05/04/2022
 * @time 12:28
 * @Update 05/04/2022
 */
public final class LangUtils {
    private static final Map<Class<?>, Object> extractors = new ConcurrentHashMap<>();

    public static String nameOf(Class<?> clazz) {
        return clazz.getName();
    }

    @SuppressWarnings("unchecked")
    public static <T> String nameOfProperty(Class<T> clazz, Function<? super T, ?> bridge) {
        T extractor = (T) extractors.computeIfAbsent(clazz, PropertyNames::getPropertyNameExtractor);

        bridge.apply(extractor);

        return PropertyNameExtractorInterceptor.extractMethodName();
    }

    public static <T> String $$(Class<T> clazz, Function<T, ?> bridge) {
        return nameOfProperty(clazz, bridge);
    }
}
