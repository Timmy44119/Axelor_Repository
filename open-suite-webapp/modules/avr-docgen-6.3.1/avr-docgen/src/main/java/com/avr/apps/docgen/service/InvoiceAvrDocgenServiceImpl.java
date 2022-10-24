/*
 * ***********************************
 *          AVR SOLUTIONS            *
 * ***********************************
 *
 *
 *
 * @author David
 * @date 11/03/2021
 * @time 17:05
 * @version 1.0
 */

package com.avr.apps.docgen.service;

import com.avr.apps.docgen.service.generatorDocument.InvoiceGenerator;
import com.avr.apps.docgen.utils.DocGenType;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.repo.InvoiceLineRepository;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.service.app.AppAccountService;
import com.axelor.apps.account.service.config.AccountConfigService;
import com.axelor.apps.account.service.invoice.InvoiceLineService;
import com.axelor.apps.account.service.invoice.InvoiceToolService;
import com.axelor.apps.account.service.invoice.factory.CancelFactory;
import com.axelor.apps.account.service.invoice.factory.ValidateFactory;
import com.axelor.apps.account.service.invoice.factory.VentilateFactory;
import com.axelor.apps.account.service.move.MoveToolService;
import com.axelor.apps.base.db.AppDocgen;
import com.axelor.apps.base.service.PartnerService;
import com.axelor.apps.base.service.alarm.AlarmEngineService;
import com.axelor.apps.base.service.tax.TaxService;
import com.axelor.apps.cash.management.service.InvoiceEstimatedPaymentService;
import com.axelor.apps.cash.management.service.InvoiceServiceManagementImpl;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
import com.axelor.apps.stock.service.StockMoveService;
import com.axelor.apps.supplychain.service.IntercoService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;

@Singleton
public class InvoiceAvrDocgenServiceImpl extends InvoiceServiceManagementImpl {

    protected final InvoiceGenerator invoiceGenerator;

    @Inject
    public InvoiceAvrDocgenServiceImpl(
        ValidateFactory validateFactory,
        VentilateFactory ventilateFactory,
        CancelFactory cancelFactory,
        AlarmEngineService<Invoice> alarmEngineService,
        InvoiceRepository invoiceRepo,
        AppAccountService appAccountService,
        PartnerService partnerService,
        InvoiceLineService invoiceLineService,
        AccountConfigService accountConfigService,
        MoveToolService moveToolService,
        InvoiceLineRepository invoiceLineRepo,
        IntercoService intercoService,
        TaxService taxService,
        StockMoveRepository stockMoveRepository,
        InvoiceEstimatedPaymentService invoiceEstimatedPaymentService,
        InvoiceGenerator invoiceGenerator
    ) {
        super(
            validateFactory,
            ventilateFactory,
            cancelFactory,
            alarmEngineService,
            invoiceRepo,
            appAccountService,
            partnerService,
            invoiceLineService,
            accountConfigService,
            moveToolService,
            invoiceLineRepo,
            intercoService,
            taxService,
            stockMoveRepository,
            invoiceEstimatedPaymentService
        );
        this.invoiceGenerator = invoiceGenerator;
    }

    @Override
    @Transactional(rollbackOn = { AxelorException.class, Exception.class })
    public void ventilate(Invoice invoice) throws AxelorException {
        super.ventilate(invoice);
        if (
            checkEnablePDFGenerationOnVentilationForDocgen(invoice) &&
            appAccountService.isApp("docgen") &&
            ((AppDocgen) appAccountService.getApp("docgen")).getEnableInvoice()
        ) {
            if (!invoiceGenerator.generateToAttachment(invoice, DocGenType.PDF)) {
                throw new AxelorException(
                    TraceBackRepository.CATEGORY_MISSING_FIELD,
                    "Le document de type %s n'a pas pu être généré : %s",
                    invoiceGenerator.getTypeName(invoice),
                    invoiceGenerator.getError()
                );
            }
        }
    }

    @Override
    protected boolean checkEnablePDFGenerationOnVentilation(Invoice invoice) throws AxelorException {
        return false;
    }

    protected boolean checkEnablePDFGenerationOnVentilationForDocgen(Invoice invoice) throws AxelorException {
        if (
            appAccountService.getAppInvoice().getAutoGenerateInvoicePrintingFileOnSaleInvoice() &&
            !InvoiceToolService.isPurchase(invoice)
        ) {
            return true;
        }
        if (
            appAccountService.getAppInvoice().getAutoGenerateInvoicePrintingFileOnPurchaseInvoice() &&
            InvoiceToolService.isPurchase(invoice)
        ) {
            return true;
        }
        return false;
    }
}
