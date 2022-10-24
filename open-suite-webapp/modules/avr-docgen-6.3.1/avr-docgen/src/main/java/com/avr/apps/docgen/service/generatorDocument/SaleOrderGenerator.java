package com.avr.apps.docgen.service.generatorDocument;

import com.avr.apps.docgen.common.I18n;
import com.avr.apps.docgen.common.utils.ObjectUtils;
import com.avr.apps.docgen.db.DocgenSubType;
import com.avr.apps.docgen.exception.FieldRequiredException;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.sale.db.SaleOrder;

/**
 * *********************************** AVR SOLUTIONS ***********************************
 *
 * @author David
 * @version 1.0
 * @date 13/10/2021
 * @time 12:24
 * @Update 13/10/2021
 */
public class SaleOrderGenerator extends GeneratorDocument<SaleOrder> {

    @Override
    protected String getSequence(SaleOrder saleOrder) {
        return saleOrder.getSaleOrderSeq();
    }

    @Override
    protected Partner getPartner(SaleOrder saleOrder) throws FieldRequiredException {
        checkValidityField(saleOrder, SaleOrder::getClientPartner);
        return saleOrder.getClientPartner();
    }

    @Override
    public String getTypeName(SaleOrder saleOrder) {
        return I18n.get(
            isQuotation(saleOrder) ? "devis" : "commande",
            ObjectUtils.eval(() -> saleOrder.getClientPartner().getLanguage().getCode(), null)
        );
    }

    @Override
    protected DocgenSubType getType(SaleOrder saleOrder) {
        return isQuotation(saleOrder) ? getApp().getSubTypeQuotation() : getApp().getSubTypeOrderCustomer();
    }

    private boolean isQuotation(SaleOrder saleOrder) {
        return saleOrder.getStatusSelect() < 3;
    }
}
