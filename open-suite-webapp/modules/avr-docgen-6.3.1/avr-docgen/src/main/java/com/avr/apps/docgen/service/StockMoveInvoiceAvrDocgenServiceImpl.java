package com.avr.apps.docgen.service;

import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.repo.InvoiceLineRepository;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.apps.account.service.invoice.InvoiceService;
import com.axelor.apps.account.service.invoice.generator.InvoiceGenerator;
import com.axelor.apps.businessproject.service.ProjectStockMoveInvoiceServiceImpl;
import com.axelor.apps.purchase.db.repo.PurchaseOrderRepository;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.repo.StockMoveLineRepository;
import com.axelor.apps.supplychain.exception.IExceptionMessage;
import com.axelor.apps.supplychain.service.PurchaseOrderInvoiceService;
import com.axelor.apps.supplychain.service.SaleOrderInvoiceService;
import com.axelor.apps.supplychain.service.StockMoveLineServiceSupplychain;
import com.axelor.apps.supplychain.service.app.AppSupplychainService;
import com.axelor.apps.supplychain.service.config.SupplyChainConfigService;
import com.axelor.common.ObjectUtils;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.persist.Transactional;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Singleton
public class StockMoveInvoiceAvrDocgenServiceImpl extends ProjectStockMoveInvoiceServiceImpl {

    protected final SaleOrderInvoiceService saleOrderInvoiceService;
    protected final SupplyChainConfigService supplyChainConfigService;
    protected final InvoiceRepository invoiceRepository;

    @Inject
    public StockMoveInvoiceAvrDocgenServiceImpl(
        SaleOrderInvoiceService saleOrderInvoiceService,
        PurchaseOrderInvoiceService purchaseOrderInvoiceService,
        StockMoveLineServiceSupplychain stockMoveLineServiceSupplychain,
        InvoiceRepository invoiceRepository,
        SaleOrderRepository saleOrderRepo,
        PurchaseOrderRepository purchaseOrderRepo,
        StockMoveLineRepository stockMoveLineRepository,
        InvoiceLineRepository invoiceLineRepository,
        SupplyChainConfigService supplyChainConfigService,
        AppSupplychainService appSupplychainService,
        SaleOrderInvoiceService saleOrderInvoiceService1,
        SupplyChainConfigService supplyChainConfigService1,
        InvoiceRepository invoiceRepository1
    ) {
        super(
            saleOrderInvoiceService,
            purchaseOrderInvoiceService,
            stockMoveLineServiceSupplychain,
            invoiceRepository,
            saleOrderRepo,
            purchaseOrderRepo,
            stockMoveLineRepository,
            invoiceLineRepository,
            supplyChainConfigService,
            appSupplychainService
        );
        this.saleOrderInvoiceService = saleOrderInvoiceService1;
        this.supplyChainConfigService = supplyChainConfigService1;
        this.invoiceRepository = invoiceRepository1;
    }

    @Override
    @Transactional(rollbackOn = { Exception.class })
    public Invoice createInvoiceFromSaleOrder(StockMove stockMove, SaleOrder saleOrder, Map<Long, BigDecimal> qtyToInvoiceMap)
        throws AxelorException {
        // we block if we are trying to invoice partially if config is deactivated
        if (
            !supplyChainConfigService.getSupplyChainConfig(stockMove.getCompany()).getActivateOutStockMovePartialInvoicing() &&
            computeNonCanceledInvoiceQty(stockMove).signum() > 0
        ) {
            throw new AxelorException(
                TraceBackRepository.CATEGORY_INCONSISTENCY,
                I18n.get(IExceptionMessage.STOCK_MOVE_PARTIAL_INVOICE_ERROR),
                stockMove.getStockMoveSeq()
            );
        }

        InvoiceGenerator invoiceGenerator = saleOrderInvoiceService.createInvoiceGenerator(saleOrder, stockMove.getIsReversion());

        Invoice invoice = invoiceGenerator.generate();

        checkSplitSalePartiallyInvoicedStockMoveLines(stockMove, stockMove.getStockMoveLineList());

        invoiceGenerator.populate(
            invoice,
            this.createInvoiceLines(invoice, stockMove, stockMove.getStockMoveLineList(), qtyToInvoiceMap)
        );

        if (invoice != null) {
            // do not create empty invoices
            if (invoice.getInvoiceLineList() == null || invoice.getInvoiceLineList().isEmpty()) {
                return null;
            }
            invoice.setSaleOrder(saleOrder);
            this.extendInternalReference(stockMove, invoice);
            invoice.setDeliveryAddress(stockMove.getToAddress());
            invoice.setDeliveryAddressStr(stockMove.getToAddressStr());
            invoice.setAddressStr(saleOrder.getMainInvoicingAddressStr());
            invoice.setTitle(saleOrder.getTitle());

            // fill default advance payment invoice
            if (invoice.getOperationSubTypeSelect() != InvoiceRepository.OPERATION_SUB_TYPE_ADVANCE) {
                invoice.setAdvancePaymentInvoiceSet(Beans.get(InvoiceService.class).getDefaultAdvancePaymentInvoice(invoice));
            }

            invoice.setPartnerTaxNbr(saleOrder.getClientPartner().getTaxNbr());
            if (!Strings.isNullOrEmpty(saleOrder.getInvoiceComments())) {
                invoice.setNote(saleOrder.getInvoiceComments());
            }

            if (ObjectUtils.isEmpty(invoice.getProformaComments()) && !Strings.isNullOrEmpty(saleOrder.getProformaComments())) {
                invoice.setProformaComments(saleOrder.getProformaComments());
            }

            Set<StockMove> stockMoveSet = invoice.getStockMoveSet();
            if (stockMoveSet == null) {
                stockMoveSet = new HashSet<>();
                invoice.setStockMoveSet(stockMoveSet);
            }
            stockMoveSet.add(stockMove);

            invoiceRepository.save(invoice);
        }

        return invoice;
    }
}
