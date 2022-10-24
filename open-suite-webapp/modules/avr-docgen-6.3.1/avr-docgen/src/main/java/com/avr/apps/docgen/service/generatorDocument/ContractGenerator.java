package com.avr.apps.docgen.service.generatorDocument;

import com.avr.apps.docgen.common.I18n;
import com.avr.apps.docgen.db.DocgenSubType;
import com.avr.apps.docgen.exception.FieldRequiredException;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.contract.db.Contract;

/**
 * *********************************** AVR SOLUTIONS ***********************************
 *
 * @author David
 * @version 1.0
 * @date 13/10/2021
 * @time 12:24
 * @Update 13/10/2021
 */
public class ContractGenerator extends GeneratorDocument<Contract> {

    @Override
    protected String getSequence(Contract contract) {
        return contract.getContractId();
    }

    @Override
    protected Partner getPartner(Contract contract) throws FieldRequiredException {
        checkValidityField(contract, Contract::getPartner);
        return contract.getPartner();
    }

    @Override
    public String getTypeName(Contract contract) {
        return I18n.get("contract");
    }

    @Override
    protected DocgenSubType getType(Contract contract) {
        return getApp().getSubTypeContract();
    }
}
