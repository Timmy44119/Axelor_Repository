/**
 * *********************************** AVR SOLUTIONS ***********************************
 *
 * @author David
 * @date 28/04/2021
 * @time 17:43
 * @Update 28/04/2021
 * @version 1.0
 */
package com.avr.apps.docgen.common.utils;

import com.google.common.base.Preconditions;

import java.util.HashMap;
import java.util.Map;

public abstract class Maps {

    @SafeVarargs
    public static <K, V> Map<K, V> ofEntries(Map<K, V>... maps) {
        Preconditions.checkNotNull(maps);
        Map<K, V> map = new HashMap<>();
        for (Map<K, V> m : maps) {
            map.putAll(m);
        }
        return map;
    }

    public static <K, V> Map<K, V> of(K key, V val) {
        Map<K, V> map = new HashMap<>();
        map.put(key, val);
        return map;
    }
}
