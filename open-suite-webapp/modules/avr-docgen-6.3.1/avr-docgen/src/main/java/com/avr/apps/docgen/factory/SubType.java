package com.avr.apps.docgen.factory;

import com.avr.apps.docgen.db.DocgenSubType;
import com.avr.apps.docgen.service.interfaces.Docgen;
import com.axelor.exception.AxelorException;

/**
 * *********************************** AVR SOLUTIONS ***********************************
 *
 * @author David
 * @version 1.0
 * @date 13/10/2021
 * @time 17:10
 * @Update 13/10/2021
 */
public interface SubType {
    DocgenSubType getSubTypeByType(Docgen type) throws AxelorException;
}
