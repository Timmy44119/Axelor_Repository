package com.avr.apps.docgen.service.generatorDocument;

import com.avr.apps.docgen.common.I18n;
import com.avr.apps.docgen.db.DocgenSubType;
import com.avr.apps.docgen.exception.FieldRequiredException;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.contract.db.Contract;
import com.axelor.apps.stock.db.LogisticalForm;

/**
 * *********************************** AVR SOLUTIONS ***********************************
 *
 * @author David
 * @version 1.0
 * @date 13/10/2021
 * @time 15:30
 * @Update 13/10/2021
 */
public class LogisticalFormWithoutComponentGenerator extends GeneratorDocument<LogisticalForm> {

    @Override
    protected String getSequence(LogisticalForm logisticalForm) {
        return logisticalForm.getDeliveryNumberSeq();
    }

    @Override
    protected Partner getPartner(LogisticalForm logisticalForm) throws FieldRequiredException {
        checkValidityField(logisticalForm, LogisticalForm::getDeliverToCustomerPartner);
        return logisticalForm.getDeliverToCustomerPartner();
    }

    @Override
    public String getTypeName(LogisticalForm logisticalForm) {
        return I18n.get("Packing list without component");
    }

    @Override
    protected DocgenSubType getType(LogisticalForm logisticalForm) {
        return getApp().getSubTypeLogisticalFormWithoutComponent();
    }
}
