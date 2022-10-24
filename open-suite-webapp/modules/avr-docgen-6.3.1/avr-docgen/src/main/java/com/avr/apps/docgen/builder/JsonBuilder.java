package com.avr.apps.docgen.builder;

import com.avr.apps.docgen.common.utils.ObjectUtils;
import com.avr.apps.docgen.common.Mapper;
import com.avr.apps.docgen.common.function.ThreeParamsFunction;
import wslite.json.JSONArray;
import wslite.json.JSONException;
import wslite.json.JSONObject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * The type Json builder.
 */
public class JsonBuilder {

	private final JSONObject json;

	/**
	 * Instantiates a new Json builder.
	 */
	public JsonBuilder() {
		this.json = new JSONObject();
	}

	/**
	 * get Map Creator
	 *
	 * @return MapCreator map creator
	 */
	public static MapCreator getMapCreator() {
		return new MapCreator();
	}

	/**
	 * Add array of object json builder.
	 *
	 * @param <U>       the type parameter
	 * @param fieldName the field name
	 * @param list      the list
	 * @param fn        the fn
	 * @return the json builder
	 */
	public <U> JsonBuilder addArrayOfObject(String fieldName, List<U> list, ThreeParamsFunction<MapCreator, U, MapCreator> fn) {
		MapCreator mapCreator = new MapCreator();
		mapCreator.addArrayOfObject(fieldName, list, fn).map.forEach((key, value) -> {
			try {
				json.put(key, value);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		});
		return this;
	}

	/**
	 * Add array of object json builder.
	 *
	 * @param <U>       the type parameter
	 * @param fieldName the field name
	 * @param list      the list
	 * @param fn        the fn
	 * @return the json builder
	 */
	public <U> MapCreator addArrayOfObjectToMapCreator(String fieldName, List<U> list, ThreeParamsFunction<MapCreator, U, MapCreator> fn) {
		MapCreator mapCreator = new MapCreator();
		return mapCreator.addArrayOfObject(fieldName, list, fn);
	}

	/**
	 * Add array of object json builder.
	 *
	 * @param <U>       the type parameter
	 * @param fieldName the field name
	 * @param list      the list
	 * @param fn        the fn
	 * @return the json builder
	 */
	public <U> MapCreator addArrayOfObjectToMapCreator(String fieldName, Collection<U> list, ThreeParamsFunction<MapCreator, U, MapCreator> fn) {
		MapCreator mapCreator = new MapCreator();
		return mapCreator.addArrayOfObject(fieldName, list, fn);
	}

	/**
	 * Add array of object json builder.
	 *
	 * @param <U>       the type parameter
	 * @param fieldName the field name
	 * @param list      the list
	 * @param codeLang        the codeLang
	 * @return the json builder
	 */
	public <U> MapCreator addArrayOfObjectToMapCreator(String fieldName, List<U> list, String codeLang) {
		MapCreator mapCreator = new MapCreator();
		return mapCreator.addArrayOfObject(fieldName, list, codeLang);
	}

	/**
	 * Add array of object json builder.
	 *
	 * @param <U>       the type parameter
	 * @param fieldName the field name
	 * @param set       the list
	 * @param fn        the fn
	 * @return the json builder
	 */
	public <U> MapCreator addArrayOfObjectToMapCreator(String fieldName, Set<U> set, ThreeParamsFunction<MapCreator, U, MapCreator> fn) {
		MapCreator mapCreator = new MapCreator();
		return mapCreator.addArrayOfObject(fieldName, set, fn);
	}

	/**
	 * Creator of array of object
	 *
	 * @param <U>       the type parameter
	 * @param fieldName the field name
	 * @param set       the set
	 * @param fn        the fn
	 * @return json builder
	 */
	public <U> JsonBuilder addArrayOfObject(String fieldName, Set<U> set, ThreeParamsFunction<MapCreator, U, MapCreator> fn) {
		MapCreator mapCreator = new MapCreator();
		mapCreator.addArrayOfObject(fieldName, set, fn).map.forEach((key, value) -> {
			try {
				json.put(key, value);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		});
		return this;
	}

	/**
	 * Add object json builder.
	 *
	 * @param fieldName the field name
	 * @param value     the value
	 * @return json builder
	 * @throws JSONException the json exception
	 */
	public JsonBuilder addObject(String fieldName, Object value) throws JSONException {
		json.put(fieldName, value);
		return this;
	}

	/**
	 * Create object
	 *
	 * @param fieldName  the field name
	 * @param mapCreator the map creator
	 * @return the json builder
	 */
	public JsonBuilder addObject(String fieldName, MapCreator mapCreator) {
		MapCreator mapCreatorCurr = new MapCreator();
		mapCreatorCurr.addObject(fieldName, mapCreator).map.forEach((key, value) -> {
			try {
				json.put(key, value);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		});
		return this;
	}

	/**
	 * Create object
	 *
	 * @param fieldName  the field name
	 * @param mapCreator the map creator
	 * @return the json builder
	 */
	public JsonBuilder addObject(String fieldName, List<MapCreator> mapCreator) {
		MapCreator mapCreatorCurr = new MapCreator();
		mapCreatorCurr.addObject(fieldName, mapCreator).map.forEach((key, value) -> {
			try {
				json.put(key, value);
			} catch (JSONException e) {
				e.printStackTrace();
			}
		});
		return this;
	}

	/**
	 * Build string.
	 *
	 * @return the string
	 */
	public String build() {
		return json.toString();
	}

	/**
	 * The type Map creator.
	 */
	public static final class MapCreator {

		/**
		 * The Map.
		 */
		protected Map<String, Object> map;

		/**
		 * Instantiates a new Map creator.
		 */
		public MapCreator() {
			this.map = new HashMap<>();
		}

		/**
		 * Add array of object map creator.
		 *
		 * @param <U>       the type parameter
		 * @param fieldName the field name
		 * @param list      the list
		 * @param fn        the fn
		 * @return the map creator
		 */
		public <U> MapCreator addArrayOfObject(String fieldName, List<U> list, ThreeParamsFunction<MapCreator, U, MapCreator> fn) {
			JSONArray jsonArray = new JSONArray();
			list.forEach(l -> {
				JSONObject jsonObject = new JSONObject();
				jsonObject.putAll(fn.apply(new MapCreator(), l).map);
				jsonArray.add(jsonObject);
			});
			map.put(fieldName, jsonArray);
			return this;
		}

		/**
		 * Add array of object map creator.
		 *
		 * @param <U>       the type parameter
		 * @param fieldName the field name
		 * @param list      the list
		 * @param fn        the fn
		 * @return the map creator
		 */
		public <U> MapCreator addArrayOfObject(String fieldName, Collection<U> list, ThreeParamsFunction<MapCreator, U, MapCreator> fn) {
			JSONArray jsonArray = new JSONArray();
			list.forEach(l -> {
				JSONObject jsonObject = new JSONObject();
				jsonObject.putAll(fn.apply(new MapCreator(), l).map);
				jsonArray.add(jsonObject);
			});
			map.put(fieldName, jsonArray);
			return this;
		}

		/**
		 * Add array of object map creator.
		 *
		 * @param <U>       the type parameter
		 * @param fieldName the field name
		 * @param list      the list
		 * @param codeLang  the codeLang
		 * @return the map creator
		 */
		public <U> MapCreator addArrayOfObject(String fieldName, List<U> list, String codeLang) {
			JSONArray jsonArray = new JSONArray();
			list.forEach(l -> {
				JSONObject jsonObject = new JSONObject();
				jsonObject.putAll(Mapper.toMapWithoutChilds(l, fieldName, codeLang));
				jsonArray.add(jsonObject);
			});
			map.put(fieldName, jsonArray);
			return this;
		}

		/**
		 * Add array of object map creator.
		 *
		 * @param <U>       the type parameter
		 * @param fieldName the field name
		 * @param set       the set
		 * @param fn        the fn
		 * @return the map creator
		 */
		public <U> MapCreator addArrayOfObject(String fieldName, Set<U> set, ThreeParamsFunction<MapCreator, U, MapCreator> fn) {
			JSONArray jsonArray = new JSONArray();
			if (set.size() > 0) {
				set.forEach(s -> {
					JSONObject jsonObject = new JSONObject();
					jsonObject.putAll(fn.apply(new MapCreator(), s).map);
					jsonArray.add(jsonObject);
				});
				map.put(fieldName, jsonArray);
			} else {
				map.put(fieldName, null);
			}
			return this;
		}

		/**
		 * Add object map creator.
		 *
		 * @param fieldName the field name
		 * @param value     the value
		 * @return the map creator
		 */
		public MapCreator addObject(String fieldName, Object value) {
			map.put(fieldName, value);
			return this;
		}

		/**
		 * Add object map creator.
		 *
		 * @param fieldName the field name
		 * @param value     the value
		 * @return the map creator
		 */
		public MapCreator addObject(String fieldName, Supplier<Object> value) {
			map.put(fieldName, ObjectUtils.eval(value, ""));
			return this;
		}

		/**
		 * Add object map creator.
		 *
		 * @param fieldName the field name
		 * @param value     the value
		 * @return the map creator
		 */
		public MapCreator addObject(String fieldName, BigDecimal value) {
			map.put(fieldName, value.setScale(2, RoundingMode.HALF_UP));
			return this;
		}

		/**
		 * Add object map creator.
		 *
		 * @param fieldName the field name
		 * @param value     the value
		 * @return the map creator
		 */
		public MapCreator addObject(String fieldName, BigDecimal value, int scale) {
			map.put(fieldName, value.setScale(scale, RoundingMode.HALF_UP));
			return this;
		}

		/**
		 * Add object map creator.
		 *
		 * @param fieldName the field name
		 * @param value     the value
		 * @return the map creator
		 */
		public MapCreator addObjectStringOnly(String fieldName, Object value) {
			map.put(fieldName, value.toString());
			return this;
		}

		/**
		 * Add object map creator.
		 *
		 * @param fieldName the field name
		 * @param value     the value
		 * @return the map creator
		 */
		public MapCreator addObjectStringOnly(String fieldName, BigDecimal value) {
			map.put(fieldName, value.setScale(2, RoundingMode.HALF_UP).toString());
			return this;
		}

		/**
		 * Add object map creator.
		 *
		 * @param fieldName the field name
		 * @param value     the value
		 * @return the map creator
		 */
		public MapCreator addObjectStringOnly(String fieldName, BigDecimal value, int scale) {
			map.put(fieldName, value.setScale(scale, RoundingMode.HALF_UP).toString());
			return this;
		}

		/**
		 * Add object map creator.
		 *
		 * @param fieldName the field name
		 * @param fn        the fn
		 * @return the map creator
		 */
		public MapCreator addObject(String fieldName, Function<MapCreator, MapCreator> fn) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.putAll(fn.apply(new MapCreator()).map);
			map.put(fieldName, jsonObject);
			return this;
		}

		/**
		 * Add object map creator.
		 *
		 * @param fieldName  the field name
		 * @param mapCreator the fn
		 * @return the map creator
		 */
		public MapCreator addObject(String fieldName, MapCreator mapCreator) {
			JSONObject jsonObject = new JSONObject();
			jsonObject.putAll(mapCreator.map);
			map.put(fieldName, jsonObject);
			return this;
		}


		/**
		 * Add object map creator from list of MapCreator.
		 *
		 * @param fieldName      the field name
		 * @param mapCreatorList the map creator list
		 * @return the map creator
		 */
		public MapCreator addObject(String fieldName, List<MapCreator> mapCreatorList) {
			JSONObject jsonObject = new JSONObject();
			Map<String, Object> mapTmp = new HashMap<String, Object>();
			mapCreatorList.forEach(mc -> mapTmp.putAll(mc.map));
			jsonObject.putAll(mapTmp);
			map.put(fieldName, jsonObject);
			return this;
		}
	}
}
