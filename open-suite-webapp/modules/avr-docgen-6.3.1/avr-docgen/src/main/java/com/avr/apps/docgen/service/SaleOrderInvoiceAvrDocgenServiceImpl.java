package com.avr.apps.docgen.service;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.service.invoice.InvoiceService;
import com.axelor.apps.base.service.app.AppBaseService;
import com.axelor.apps.businessproject.service.SaleOrderInvoiceProjectServiceImpl;
import com.axelor.apps.businessproject.service.app.AppBusinessProjectService;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.SaleOrderLine;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.sale.service.saleorder.SaleOrderLineService;
import com.axelor.apps.sale.service.saleorder.SaleOrderWorkflowServiceImpl;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.supplychain.db.Timetable;
import com.axelor.apps.supplychain.db.repo.TimetableRepository;
import com.axelor.apps.supplychain.exception.IExceptionMessage;
import com.axelor.apps.supplychain.service.CommonInvoiceService;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.axelor.apps.supplychain.service.invoice.InvoiceServiceSupplychainImpl;
import com.axelor.common.ObjectUtils;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SaleOrderInvoiceAvrDocgenServiceImpl extends SaleOrderInvoiceProjectServiceImpl {

    @Inject
    public SaleOrderInvoiceAvrDocgenServiceImpl(
        AppBaseService appBaseService,
        AppSupplychainService appSupplychainService,
        SaleOrderRepository saleOrderRepo,
        InvoiceRepository invoiceRepo,
        InvoiceServiceSupplychainImpl invoiceServiceSupplychain,
        AppBusinessProjectService appBusinessProjectService,
        StockMoveRepository stockMoveRepository,
        SaleOrderLineService saleOrderLineService,
        SaleOrderWorkflowServiceImpl saleOrderWorkflowServiceImpl,
        CommonInvoiceService commonInvoiceService
    ) {
        super(
            appBaseService,
            appSupplychainService,
            saleOrderRepo,
            invoiceRepo,
            invoiceServiceSupplychain,
            appBusinessProjectService,
            stockMoveRepository,
            saleOrderLineService,
            saleOrderWorkflowServiceImpl,
            commonInvoiceService
        );
    }

    @Override
    @Transactional(rollbackOn = { Exception.class })
    public Invoice generateInvoice(
        SaleOrder saleOrder,
        int operationSelect,
        BigDecimal amount,
        boolean isPercent,
        Map<Long, BigDecimal> qtyToInvoiceMap,
        List<Long> timetableIdList
    ) throws AxelorException {
        Invoice invoice;
        switch (operationSelect) {
            case SaleOrderRepository.INVOICE_ALL:
                invoice = generateInvoice(saleOrder);
                break;
            case SaleOrderRepository.INVOICE_LINES:
                invoice = generateInvoiceFromLines(saleOrder, qtyToInvoiceMap, isPercent);
                break;
            case SaleOrderRepository.INVOICE_ADVANCE_PAYMENT:
                invoice = generateAdvancePayment(saleOrder, amount, isPercent);
                break;
            case SaleOrderRepository.INVOICE_TIMETABLES:
                BigDecimal percentSum = BigDecimal.ZERO;
                TimetableRepository timetableRepo = Beans.get(TimetableRepository.class);
                List<Timetable> timetableList = new ArrayList<>();
                if (timetableIdList == null || timetableIdList.isEmpty()) {
                    throw new AxelorException(
                        saleOrder,
                        TraceBackRepository.CATEGORY_INCONSISTENCY,
                        I18n.get(IExceptionMessage.SO_INVOICE_NO_TIMETABLES_SELECTED)
                    );
                }
                for (Long timetableId : timetableIdList) {
                    Timetable timetable = timetableRepo.find(timetableId);
                    timetableList.add(timetable);
                    percentSum = percentSum.add(timetable.getPercentage());
                }
                invoice = generateInvoiceFromLines(saleOrder, this.generateQtyToInvoiceMap(saleOrder, percentSum), true);

                if (!timetableList.isEmpty()) {
                    for (Timetable timetable : timetableList) {
                        timetable.setInvoice(invoice);
                        timetableRepo.save(timetable);
                    }
                }

                break;
            default:
                return null;
        }
        invoice.setSaleOrder(saleOrder);

        if (!Strings.isNullOrEmpty(saleOrder.getInvoiceComments())) {
            invoice.setNote(saleOrder.getInvoiceComments());
        }

        if (ObjectUtils.isEmpty(invoice.getProformaComments()) && !Strings.isNullOrEmpty(saleOrder.getProformaComments())) {
            invoice.setProformaComments(saleOrder.getProformaComments());
        }

        // fill default advance payment invoice
        if (invoice.getOperationSubTypeSelect() != InvoiceRepository.OPERATION_SUB_TYPE_ADVANCE) {
            invoice.setAdvancePaymentInvoiceSet(invoiceService.getDefaultAdvancePaymentInvoice(invoice));
        }

        invoice.setPartnerTaxNbr(saleOrder.getClientPartner().getTaxNbr());
        invoice.setTitle(saleOrder.getTitle());

        invoice = invoiceRepo.save(invoice);

        return invoice;
    }

    private Map<Long, BigDecimal> generateQtyToInvoiceMap(SaleOrder saleOrder, BigDecimal percentage) {
        Map<Long, BigDecimal> map = new HashMap<>();

        for (SaleOrderLine soLine : saleOrder.getSaleOrderLineList()) {
            map.put(soLine.getId(), percentage);
        }
        return map;
    }
}
