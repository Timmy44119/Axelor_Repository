package com.avr.apps.docgen.exception;

/**
 * *********************************** AVR SOLUTIONS ***********************************
 *
 * @author David
 * @version 1.0
 * @date 05/04/2022
 * @time 12:22
 * @Update 05/04/2022
 */
public class FieldRequiredException extends Exception {

    private static final String MESSAGE_DEFAULT = "Le champs %s est obligatoire";

    public FieldRequiredException(String message, Object... args) {
        super(String.format(message, message));
    }

    public FieldRequiredException(String fieldName) {
        super(String.format(MESSAGE_DEFAULT, fieldName));
    }
}
