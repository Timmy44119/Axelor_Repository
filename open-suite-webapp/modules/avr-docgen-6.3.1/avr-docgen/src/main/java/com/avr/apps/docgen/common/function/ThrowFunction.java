package com.avr.apps.docgen.common.function;

@FunctionalInterface
public interface ThrowFunction<T,R> {

    R apply(T t) throws Exception;
}
