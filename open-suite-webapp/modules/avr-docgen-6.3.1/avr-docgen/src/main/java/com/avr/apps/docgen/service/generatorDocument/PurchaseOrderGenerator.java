package com.avr.apps.docgen.service.generatorDocument;

import com.avr.apps.docgen.common.I18n;
import com.avr.apps.docgen.db.DocgenSubType;
import com.avr.apps.docgen.exception.FieldRequiredException;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.purchase.db.PurchaseOrder;

/**
 * *********************************** AVR SOLUTIONS ***********************************
 *
 * @author David
 * @version 1.0
 * @date 13/10/2021
 * @time 12:24
 * @Update 13/10/2021
 */
public class PurchaseOrderGenerator extends GeneratorDocument<PurchaseOrder> {

    @Override
    protected String getSequence(PurchaseOrder purchaseOrder) {
        return purchaseOrder.getPurchaseOrderSeq();
    }

    @Override
    protected Partner getPartner(PurchaseOrder purchaseOrder) throws FieldRequiredException {
        checkValidityField(purchaseOrder, PurchaseOrder::getSupplierPartner);
        return purchaseOrder.getSupplierPartner();
    }

    @Override
    public String getTypeName(PurchaseOrder purchaseOrder) {
        return I18n.get("Commande fournisseur");
    }

    @Override
    protected DocgenSubType getType(PurchaseOrder purchaseOrder) {
        return getApp().getSubTypePurchaseOrderOrder();
    }
}
