/**
 * *********************************** AVR SOLUTIONS ***********************************
 *
 * @author David
 * @date 09/06/2021
 * @time 15:27
 * @Update 09/06/2021
 * @version 1.0
 */
package com.avr.apps.docgen.service;

import com.avr.apps.docgen.common.utils.Maps;
import com.avr.apps.docgen.db.TypeData;
import com.avr.apps.docgen.service.interfaces.TypeDataService;
import groovy.lang.Singleton;
import java.util.Map;

/**
 * ***********************************
 *          AVR SOLUTIONS            *
 * ***********************************
 *
 *
 *
 * @author David
 * @date 13/10/2021
 * @time 18:13
 * @version 1.0
 */

@Singleton
public class TypeDataServiceImpl implements TypeDataService {

    private final Map<TypeData, String> prefix = Maps.ofEntries(Maps.of(TypeData.HTML, "html_"), Maps.of(TypeData.HTML_IMAGE, "htmlimage_"));

    @Override
    public String getKeyByType(String keyBinding, TypeData typeData) {
        if (!prefix.containsKey(typeData)) return keyBinding;
        return String.format("%s%s", prefix.get(typeData), keyBinding);
    }
}
