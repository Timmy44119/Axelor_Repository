package com.avr.apps.docgen.factory;

import com.avr.apps.docgen.db.DocgenSubType;
import com.avr.apps.docgen.service.interfaces.AppDocgenService;
import com.avr.apps.docgen.service.interfaces.Docgen;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * *********************************** AVR SOLUTIONS ***********************************
 *
 * @author David
 * @version 1.0
 * @date 13/10/2021
 * @time 17:01
 * @Update 13/10/2021
 */
@Singleton
public class SubTypeFactory implements SubType {

    /**
     * The App docgen service.
     */
    protected final AppDocgenService appDocgenService;

    @Inject
    public SubTypeFactory(AppDocgenService appDocgenService) {
        this.appDocgenService = appDocgenService;
    }

    /**
     * Gets sub type by type.
     *
     * @param type the type
     * @return the sub type by type
     * @throws AxelorException the axelor exception
     */
    @Override
    public DocgenSubType getSubTypeByType(Docgen type) throws AxelorException {
        switch (type) {
            case QUOTATION:
                return appDocgenService.getAppDocgen().getSubTypeQuotation();
            case ORDER_CUSTOMER:
                return appDocgenService.getAppDocgen().getSubTypeOrderCustomer();
            case SALE_ORDER_PROFORMA:
                return appDocgenService.getAppDocgen().getSubTypeSaleOrderProforma();
            case INVOICE:
                return appDocgenService.getAppDocgen().getSubTypeInvoiceInvoice();
            case PURCHASE_ORDER_ORDER:
                return appDocgenService.getAppDocgen().getSubTypePurchaseOrderOrder();
            case INVOICE_PROFORMA:
                return appDocgenService.getAppDocgen().getSubTypeInvoiceProforma();
            case INVOICE_REFUNDS:
                return appDocgenService.getAppDocgen().getSubTypeInvoiceRefunds();
            case STOCK_MOVE_DELIVERY_NOTE:
                return appDocgenService.getAppDocgen().getSubTypeStockMoveDeliveryNote();
            case STOCK_MOVE_RETURN_VOUCHER:
                return appDocgenService.getAppDocgen().getSubTypeStockMoveReturnVoucher();
            case CONTRACT:
                return appDocgenService.getAppDocgen().getSubTypeContract();
            case LOGISTICAL_FORM:
                return appDocgenService.getAppDocgen().getSubTypeLogisticalForm();
            case LOGISTICAL_FORM_WITHOUT_COMPOSITE:
                return appDocgenService.getAppDocgen().getSubTypeLogisticalFormWithoutComponent();
            default:
                throw new AxelorException(TraceBackRepository.CATEGORY_NO_VALUE, "Cannot found %s. Not implemented yet", type);
        }
    }
}
