/**
 * *********************************** AVR SOLUTIONS ***********************************
 *
 * @author David
 * @date 09/06/2021
 * @time 15:34
 * @Update 09/06/2021
 * @version 1.0
 */
package com.avr.apps.docgen.service.interfaces;

import com.avr.apps.docgen.db.TypeData;

public interface TypeDataService {
    String getKeyByType(String keyBinding, TypeData typeData);
}
