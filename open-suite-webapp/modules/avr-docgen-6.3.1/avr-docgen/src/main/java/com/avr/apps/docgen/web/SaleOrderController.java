/**
 * ***********************************
 * AVR SOLUTIONS            *
 * ***********************************
 *
 * @author David
 * @date 11/03/2021
 * @time 17:09
 * @Update 11/03/2021
 * @version 1.0
 */

package com.avr.apps.docgen.web;

import com.avr.apps.docgen.common.utils.ObjectUtils;
import com.avr.apps.docgen.common.Mapper;
import com.avr.apps.docgen.common.utils.MetaFilesUtils;
import com.avr.apps.docgen.service.generatorDocument.SaleOrderGenerator;
import com.avr.apps.docgen.service.generatorDocument.SaleOrderProformaGenerator;
import com.avr.apps.docgen.service.interfaces.SaleOrderAvrDocgenService;
import com.avr.apps.docgen.utils.DocGenType;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.apps.sale.db.repo.SaleOrderRepository;
import com.axelor.apps.sale.service.saleorder.SaleOrderWorkflowService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.servlet.RequestScoped;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * The type Sale order controller.
 */
@RequestScoped
public class SaleOrderController {

    /**
     * The constant ERROR_MODEL.
     */
    public static final String ERROR_MODEL = "Template modèle pour %s est introuvable";
    /**
     * The constant DEVIS_COMMANDE_CLIENT_PDF.
     */
    public static final String DEVIS_COMMANDE_CLIENT_PDF = "devis-commande client.pdf";

    @Inject
    SaleOrderGenerator saleOrderGenerator;

    @Inject
    SaleOrderProformaGenerator saleOrderProformaGenerator;

    /**
     * The Sale order repository.
     */
    @Inject
    SaleOrderRepository saleOrderRepository;

    @Inject
    SaleOrderAvrDocgenService saleOrderAvrSupplychainService;

    /**
     * Finalize quotation.
     *
     * @param request  the request
     * @param response the response
     */
    public void finalizeQuotation(ActionRequest request, ActionResponse response) {
        SaleOrder saleOrder = request.getContext().asType(SaleOrder.class);
        saleOrder = Beans.get(SaleOrderRepository.class).find(saleOrder.getId());
        try {
            Beans.get(SaleOrderWorkflowService.class).finalizeQuotation(saleOrder);
        } catch (Exception e) {
            TraceBackService.trace(response, e);
        }

        response.setReload(true);
    }

    /**
     * Print receipt confirmation.
     *
     * @param request  the request
     * @param response the response
     */
    public void printReceiptConfirmation(ActionRequest request, ActionResponse response) {
        SaleOrder saleOrder = Beans.get(SaleOrderRepository.class).find(request.getContext().asType(SaleOrder.class).getId());
        if (saleOrderGenerator.generate(request.getContext().asType(SaleOrder.class), DocGenType.PDF, true, true, response)) {
            response.setNotify("L'accusé de récéption à été généré");
            response.setReload(true);
        }
    }

    /**
     * Show sale order.
     *
     * @param request  the request
     * @param response the response
     * @throws IOException     the io exception
     * @throws AxelorException the axelor exception
     */
    public void showSaleOrder(ActionRequest request, ActionResponse response) throws IOException, AxelorException {
        List<Integer> ids = Mapper.findToList(request.getContext(), "_ids");
        if (ObjectUtils.isEmpty(ids)) saleOrderGenerator.generate(
            request.getContext().asType(SaleOrder.class),
            DocGenType.PDF,
            true,
            false,
            response
        ); else {
            List<File> files = new ArrayList<>();
            for (Integer id : ids) {
                SaleOrder saleOrder = saleOrderRepository.find(new Long(id));
                File file = saleOrderGenerator.generateFile(saleOrder, DocGenType.PDF);
                Preconditions.checkNotNull(
                    file,
                    "Impossible de generer le fichier pour le devis/commande client %s",
                    saleOrder.getSaleOrderSeq()
                );
                files.add(file);
            }
            File file = MetaFilesUtils.mergeFiles(DEVIS_COMMANDE_CLIENT_PDF, files);
            response.setView(MetaFilesUtils.showFileGenerated(DEVIS_COMMANDE_CLIENT_PDF, file));
        }
    }

    /**
     * Save sale order.
     *
     * @param request  the request
     * @param response the response
     */
    public void saveSaleOrder(ActionRequest request, ActionResponse response) {
        saleOrderGenerator.generateToAttachment(request.getContext().asType(SaleOrder.class), DocGenType.PDF, response);
        response.setReload(true);
    }

    /**
     * Print proforma invoice.
     *
     * @param request  the request
     * @param response the response
     */
    public void printProformaInvoice(ActionRequest request, ActionResponse response) {
        if (saleOrderProformaGenerator.generateToAttachment(request.getContext().asType(SaleOrder.class), DocGenType.PDF)) {
            response.setNotify("La facture proforma à été généré");
            response.setReload(true);
            return;
        }
        response.setError(String.format("La facture proforma n'a pas pu être généré : %s", saleOrderGenerator.getError()));
    }

    private final String SO_LINES_WIZARD_QTY_TO_INVOICE_FIELD = "qtyToInvoice";

    /**
     *
     * @param request
     * @param response
     */
    public void computedDeadLine(ActionRequest request, ActionResponse response) {
        response.setValue(
            "deadlineDate",
            saleOrderAvrSupplychainService.computedDeadLineDate(request.getContext().asType(SaleOrder.class))
        );
    }
}
