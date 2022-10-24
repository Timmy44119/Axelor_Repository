package com.avr.apps.docgen.common;

import com.axelor.db.Model;
import com.axelor.db.Query;
import com.axelor.db.mapper.Property;
import com.google.common.collect.Lists;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.avr.apps.docgen.common.utils.ObjectUtils.isEmpty;
import static com.avr.apps.docgen.common.utils.ObjectUtils.notEmpty;

/**
 * The type Mapper.
 */
public final class Mapper {

	/**
	 * The Primitif type.
	 */
	static final List<String> PRIMITIF_TYPE = new ArrayList<>();
	/**
	 * The Type relation one.
	 */
	static final List<String> TYPE_RELATION_ONE = new ArrayList<>();
	static {
		PRIMITIF_TYPE.add(BigDecimal.class.getSimpleName());
		PRIMITIF_TYPE.add(Long.class.getSimpleName());
		PRIMITIF_TYPE.add(Double.class.getSimpleName());
		PRIMITIF_TYPE.add(Integer.class.getSimpleName());
		PRIMITIF_TYPE.add(String.class.getSimpleName());
		PRIMITIF_TYPE.add(Character.class.getSimpleName());
		PRIMITIF_TYPE.add(LocalDate.class.getSimpleName());
		PRIMITIF_TYPE.add(LocalDateTime.class.getSimpleName());
		PRIMITIF_TYPE.add(Boolean.class.getSimpleName());
		PRIMITIF_TYPE.add(Float.class.getSimpleName());
		PRIMITIF_TYPE.add(Void.class.getSimpleName());
		TYPE_RELATION_ONE.add("MANY_TO_ONE");
		TYPE_RELATION_ONE.add("ONE_TO_MANY");
		TYPE_RELATION_ONE.add("ONE_TO_ONE");
	}

	private Mapper() {
	}

	/**
	 * To map map.
	 *
	 * @param bean the bean
	 * @return the map
	 */
	public static Map<String, Object> toMap(Object bean) {
		return com.axelor.db.mapper.Mapper.toMap(bean);
	}

	/**
	 * To map without childs map.
	 *
	 * @param bean     the bean
	 * @param prefix   the prefix
	 * @param codeLang the code lang
	 * @return the map
	 */
	public static Map<String, Object> toMapWithoutChilds(Object bean, String prefix, String codeLang) {
		if (bean == null) {
			return null;
		}
		final Map<String, Object> map = new HashMap<>();
		final com.axelor.db.mapper.Mapper mapper = com.axelor.db.mapper.Mapper.of(bean.getClass());
		for (Property p : mapper.getProperties()) {
			if(TYPE_RELATION_ONE.contains(p.getType().name()) || p.get(bean) == null) continue;
			if ("DATE".equalsIgnoreCase(p.getType().name())) {
				map.put(prefix != null ? String.format("%s.%s", prefix, p.getName()) : p.getName(), DateFormatter.transformDate((LocalDate) p.get(bean), codeLang));
			}else if ("DATETIME".equalsIgnoreCase(p.getType().name())) {
				map.put(prefix != null ? String.format("%s.%s", prefix, p.getName()) : p.getName(), DateFormatter.transformDate((LocalDateTime) p.get(bean), codeLang));
			}else {
				map.put(prefix != null ? String.format("%s.%s", prefix, p.getName()) : p.getName(), p.get(bean));
			}
		}
		return map;
	}

	/**
	 * To map without childs map.
	 *
	 * @param bean     the bean
	 * @param codeLang the code lang
	 * @return the map
	 */
	public static Map<String, Object> toMapWithoutChilds(Object bean, String codeLang) {
		return toMapWithoutChilds(bean, null, codeLang);
	}

	/**
	 * To map from object map.
	 *
	 * @param bean the bean
	 * @return the map
	 */
	public static Map<String, Object> toMapFromObject(Object bean) {
		Field[] declaredFields = bean.getClass()
		                             .getDeclaredFields();
		HashMap<String, Object> objectObjectHashMap = new HashMap<>();
		for (Field declaredField : declaredFields) {
			declaredField.setAccessible(true);
			try {
				objectObjectHashMap.put(declaredField.getName(), declaredField.get(bean));
			} catch (IllegalAccessException ignored) {
			}
		}

		return objectObjectHashMap;
	}

	/**
	 * To map from object limited map.
	 *
	 * @param bean the bean
	 * @return the map
	 */
	public static Map<String, Object> toMapFromObjectLimited(Object bean) {
		Field[] declaredFields = bean.getClass()
		                             .getDeclaredFields();
		HashMap<String, Object> objectObjectHashMap = new HashMap<>();
		for (Field declaredField : declaredFields) {
			declaredField.setAccessible(true);
			try {
				objectObjectHashMap.put(declaredField.getName(), declaredField.get(bean));
			} catch (IllegalAccessException ignored) {
			}
		}

		return objectObjectHashMap;
	}

	/**
	 * To list of map list.
	 *
	 * @param <T>         the type parameter
	 * @param listOfBeans the list of beans
	 * @return the list
	 */
	public static <T> List<Map<String, Object>> toListOfMap(final List<T> listOfBeans) {
		List<Map<String, Object>> list = Lists.newArrayList();
		for (Object map : listOfBeans) {
			list.add(com.axelor.db.mapper.Mapper.toMap(map));
		}
		return list;
	}

	/**
	 * Find object.
	 *
	 * @param data the data
	 * @param str  the str
	 * @return the object
	 */
	@SuppressWarnings("unchecked")
	public static Object find(
			final Map<String, Object> data,
			final String str) {
		String[] fields = str.split(Pattern.quote("."));
		List<String> fn = new ArrayList<>(Arrays.asList(fields));
		if (fn.isEmpty()) return data;
		if (!data.containsKey(fn.get(0))) return null;
		Object dt = data.get(fn.get(0));
		if (Arrays.stream(dt.getClass().getInterfaces())
		          .anyMatch(it -> it.getSimpleName().equals(Map.class.getSimpleName()))) {
			fn.remove(0);
			return find((Map<String, Object>) dt, String.join(".", fn));
		} else
			return dt;
	}

	/**
	 * Find to list list.
	 *
	 * @param <T>  the type parameter
	 * @param data the data
	 * @param str  the str
	 * @return the list
	 */
	@SuppressWarnings("unchecked")
	public static <T> List<T> findToList(
			final Map<String, Object> data,
			final String str) {
		return (List<T>) find(data, str);
	}

	/**
	 * Find by class t.
	 *
	 * @param <T>  the type parameter
	 * @param data the data
	 * @param str  the str
	 * @return the t
	 */
	@SuppressWarnings("unchecked")
	public static <T> T findByClass(
			final Map<String, Object> data,
			final String str) {
		return (T) find(data, str);
	}

	/**
	 * To map limited map.
	 *
	 * @param bean   the bean
	 * @param fields the fields
	 * @return the map
	 */
	public static Map<String, Object> toMapLimited(Object bean, String... fields) {
		if (bean == null) {
			return null;
		}
		List<String> fieldList = Arrays.stream(fields).collect(Collectors.toList());
		if (!fieldList.contains("id"))
			fieldList.add("id");
		if (!fieldList.contains("name"))
			fieldList.add("name");
		if (!fieldList.contains("code"))
			fieldList.add("code");

		final Map<String, Object> map = new HashMap<>();
		final com.axelor.db.mapper.Mapper mapper = com.axelor.db.mapper.Mapper.of(bean.getClass());
		for (Property p : mapper.getProperties()) {
			if (fieldList.contains(p.getName()))
				map.put(p.getName(), p.get(bean));
		}
		return map;
	}

	/**
	 * To bean t.
	 *
	 * @param <T>   the type parameter
	 * @param klass the klass
	 * @param map   the map
	 * @return the t
	 */
	public static <T> T toBean(Class<T> klass, Map<String, Object> map) {
		return com.axelor.db.mapper.Mapper.toBean(klass, map);
	}

	/**
	 * To bean deep t.
	 *
	 * @param <T>   the type parameter
	 * @param klass the klass
	 * @param map   the map
	 * @return the t
	 * @throws IllegalAccessException the illegal access exception
	 */
	public static <T extends Model> T toBeanDeep(Class<T> klass, Map<String, Object> map) throws IllegalAccessException {
		T obj = toBean(klass, map);

		if (notEmpty(obj.getId())) {
			return Query.of(klass).filter("self.id = ?", obj.getId()).fetchOne();
		}

		Field[] declaredFields = obj.getClass()
		                            .getDeclaredFields();
		for (Field declaredField : declaredFields) {
			if (PRIMITIF_TYPE.contains(declaredField.getType().getSimpleName())) continue;
			declaredField.setAccessible(true);
			Model ob = (Model) declaredField.get(obj);
			if (isEmpty(ob)) continue;
			declaredField.set(obj, Query.of(ob.getClass()).filter("self.id = ?", ob.getId()).fetchOne());
		}

		return obj;
	}


}
