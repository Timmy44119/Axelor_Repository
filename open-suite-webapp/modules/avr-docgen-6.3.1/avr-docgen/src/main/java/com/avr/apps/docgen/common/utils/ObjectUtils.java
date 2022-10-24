package com.avr.apps.docgen.common.utils;

import com.avr.apps.docgen.exception.AvrException;
import com.avr.apps.docgen.common.function.ThrowSupplier;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.exception.service.TraceBackService;
import com.axelor.i18n.I18n;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * This class defines from static helper methods to deal with objects.
 */
public class ObjectUtils {


	private static final String MESSAGE_EMPTY_COLLECTION = "L'élément est vide";

	 /**
		* Check whether the given value is an array.
		*
		* @param value the value to check
		* @return true if value is array false otherwise
		*/
	 public static boolean isArray(Object value) {
		return value != null && value.getClass()
									 .isArray();
	}

	 /**
		* Check whether the given value is empty.
		*
		* <p>An object value is empty if:
		*
		* <ul>
		*   <li>value is null
		*   <li>value is {@link Optional} and {@link Optional#empty()}
		*   <li>value is {@link Array} with length 0
		*   <li>value is {@link CharSequence} with length 0
		*   <li>value is {@link Collection} or {@link Map} with size 0
		* </ul>
		*
		* @param value the object value to check
		* @return true if empty false otherwise
		*/
	 public static boolean isEmpty(Object value) {
		if (value == null) {
			return true;
		}
		if (value instanceof Optional) {
			return !((Optional<?>) value).isPresent();
		}
		if (value.getClass()
				 .isArray()) {
			return Array.getLength(value) == 0;
		}
		if (value instanceof CharSequence) {
			return ((CharSequence) value).length() == 0;
		}
		if (value instanceof Collection) {
			return ((Collection<?>) value).size() == 0;
		}
		if (value instanceof Map) {
			return ((Map<?, ?>) value).size() == 0;
		}
		return false;
	}

	 /**
		* Check whether the given value is not empty.
		*
		* @param value the object value to check
		* @return true if not empty false otherwise
		* @see #isEmpty(Object) #isEmpty(Object)
		*/
	 public static boolean notEmpty(Object value) {
		return !isEmpty(value);
	}

	 /**
		* check element is required
		*
		* @param value   the value
		* @param message the message
		* @throws AvrException the avr exception
		*/
	 public static void isRequired(
			Object value,
			String message) throws AvrException {
		if (isEmpty(value)) {
			throw new AvrException(TraceBackRepository.CATEGORY_NO_VALUE, I18n.get(message));
		}
	}


	 /**
		* Is required.
		*
		* @param condition the condition
		* @param message   the message
		* @throws AvrException the avr exception
		*/
	 public static void isRequiredIf(
			boolean condition,
			String message) throws AvrException {
		if (condition) {
			throw new AvrException(TraceBackRepository.CATEGORY_NO_VALUE, I18n.get(message));
		}
	}

	 /**
		* Is required.
		*
		* @param object  the object
		* @param message the message
		* @param args    the args
		* @throws AvrException the avr exception
		*/
	 public static void isRequired(
			Object object,
			String message,
			Object... args) throws AvrException {
		if (isEmpty(object)) {
			throw new AvrException(TraceBackRepository.CATEGORY_NO_VALUE, String.format(message, args));
		}
	}

	 /**
		* Eval r.
		*
		* @param <R>           the type parameter
		* @param chainSupplier the chain supplier
		* @param defaultValue  the default value
		* @return the r
		*/
	 public static <R> R eval(Supplier<R> chainSupplier, R defaultValue){
		try {
			R val = chainSupplier.get();
			if(val == null)
				return defaultValue;
			return val;
		} catch (Exception e) {
			return defaultValue;
		}
	}

	 /**
		* Eval throws r.
		*
		* @param <R>           the type parameter
		* @param chainSupplier the chain supplier
		* @param defaultValue  the default value
		* @return the r
		*/
	 public static <R> R evalThrows(ThrowSupplier<R> chainSupplier, R defaultValue){
		try {
			R val = chainSupplier.get();
			if(val == null)
				return defaultValue;
			return val;
		} catch (Exception e) {
			return defaultValue;
		}
	}

	 /**
		* check element is required
		*
		* @param value the value
		* @throws AvrException the avr exception
		*/
	 public static void isRequired(Object value) throws AvrException {
		isRequired(value, MESSAGE_EMPTY_COLLECTION);
	}


	 /**
		* Trace if empty.
		*
		* @param value   the value
		* @param message the message
		* @param args    the args
		*/
	 public static void traceIfEmpty(Object value, String message, Object... args){
		 if(isEmpty(value)) {
				TraceBackService.trace(new AvrException(TraceBackRepository.CATEGORY_NO_VALUE, message, args));
		 }
	}

	 /**
		* Trace if empty.
		*
		* @param value the value
		*/
	 public static void traceIfEmpty(Object value) {
		 traceIfEmpty(value, MESSAGE_EMPTY_COLLECTION);
	}

	 /**
		* check element is required
		*
		* @param value the value
		*/
	 public static void isRequiredNotChecked(Object value) {
		 if (isEmpty(value)) {
				throw new IllegalArgumentException();
		 }
	}

	 /**
		* check element is required
		*
		* @param value   the value
		* @param message the message
		* @param args    the args
		*/
	 public static void isRequiredNotChecked(Object value, String message, Object... args) {
		 if (isEmpty(value)) {
				throw new IllegalArgumentException(String.format(message, args));
		 }
	}

	 /**
		* Element or default t.
		*
		* @param <T>    the type parameter
		* @param object the object
		* @param def    the def
		* @return the t
		* @throws AvrException the avr exception
		*/
	 public static <T> T elementOrDefault(
			T object,
			Class<T> def) throws AvrException {
		try {
			return notEmpty(object) ? object : def.newInstance();
		} catch (InstantiationException | IllegalAccessException e) {
			throw new AvrException(
					TraceBackRepository.CATEGORY_INCONSISTENCY,
					I18n.get(String.format("Impossible to instanciate class %s", def.getName()))
			);
		}
	}

	 /**
		* Element or default t.
		*
		* @param <T>    the type parameter
		* @param object the object
		* @param def    the def
		* @return the t
		*/
	 public static <T> T elementOrDefault(
			T object,
			T def) {
		return notEmpty(object) ? object : def;
	}

	 /**
		* Is equals boolean.
		*
		* @param object     the object
		* @param comparator the comparator
		* @return the boolean
		*/
	 public static boolean isEquals(Object object, Object comparator) {
		return object.equals(comparator);
	}

	 /**
		* Not equals boolean.
		*
		* @param object     the object
		* @param comparator the comparator
		* @return the boolean
		*/
	 public static boolean notEquals(Object object, Object comparator) {
		return !isEquals(object, comparator);
	}

}
