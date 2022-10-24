package com.avr.apps.docgen.common.function;

@FunctionalInterface
public interface ThreeParamsFunction<T, V, R> {

    R apply(T t, V v);

}
