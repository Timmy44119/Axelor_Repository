package com.avr.apps.docgen.service.generatorDocument;

import com.avr.apps.docgen.common.I18n;
import com.avr.apps.docgen.db.DocgenSubType;
import com.avr.apps.docgen.exception.FieldRequiredException;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.stock.db.LogisticalForm;

import java.util.AbstractMap;

import static com.axelor.apps.account.db.repo.InvoiceRepository.*;

/**
 * *********************************** AVR SOLUTIONS ***********************************
 *
 * @author David
 * @version 1.0
 * @date 13/10/2021
 * @time 12:24
 * @Update 13/10/2021
 */
public class InvoiceGenerator extends GeneratorDocument<Invoice> {

    @Override
    protected String getSequence(Invoice invoice) {
        return invoice.getInvoiceId();
    }

    @Override
    protected Partner getPartner(Invoice invoice) throws FieldRequiredException {
        checkValidityField(invoice, Invoice::getPartner);
        return invoice.getPartner();
    }

    @Override
    public String getTypeName(Invoice invoice) {
        return getTypeBinder(invoice).getValue();
    }

    @Override
    protected DocgenSubType getType(Invoice invoice) {
        return getTypeBinder(invoice).getKey();
    }

    private AbstractMap.SimpleEntry<DocgenSubType, String> getTypeBinder(Invoice invoice) {
        return binder(invoice.getOperationTypeSelect(), invoice.getStatusSelect());
    }

    private AbstractMap.SimpleEntry<DocgenSubType, String> binder(int operationTypeSelect, int statusSelect) {
        switch (operationTypeSelect) {
            case OPERATION_TYPE_CLIENT_REFUND:
                return new AbstractMap.SimpleEntry<>(getApp().getSubTypeInvoiceRefunds(), I18n.get("Avoir"));
            case OPERATION_TYPE_CLIENT_SALE:
                return new AbstractMap.SimpleEntry<>(
                    isProforma(statusSelect) ? getApp().getSubTypeInvoiceProforma() : getApp().getSubTypeInvoiceInvoice(),
                    I18n.get(isProforma(statusSelect) ? "Facture proforma" : "Facture")
                );
            case OPERATION_TYPE_SUPPLIER_PURCHASE:
                return new AbstractMap.SimpleEntry<>(getApp().getSubTypeInvoiceSupplier(), I18n.get("Facture fournisseur"));
            case OPERATION_TYPE_SUPPLIER_REFUND:
                return new AbstractMap.SimpleEntry<>(getApp().getSubTypeInvoiceSupplierRefunds(), I18n.get("Avoir fournisseur"));
            default:
                throw new IllegalArgumentException(String.format("type de la facture non trouver : %s", operationTypeSelect));
        }
    }

    private boolean isProforma(int statusSelect) {
        return statusSelect < 3;
    }
}
