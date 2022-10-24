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
import com.avr.apps.docgen.service.generatorDocument.InvoiceGenerator;
import com.avr.apps.docgen.utils.DocGenType;
import com.axelor.apps.account.db.Invoice;
import com.axelor.apps.account.db.repo.InvoiceRepository;
import com.axelor.exception.AxelorException;
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
 * The type Invoice controller.
 */
@RequestScoped
public class InvoiceController {

    /**
     * The Invoice repository.
     */
    @Inject
    InvoiceRepository invoiceRepository;

    @Inject
    InvoiceGenerator invoiceGenerator;

    /**
     * The constant INVOICE_PROFORMA_INVOICE_CLIENT_PDF.
     */
    public static final String INVOICE_PROFORMA_INVOICE_CLIENT_PDF = "invoice_proforma-invoice.pdf";

    /**
     * Print and show invoice.
     *
     * @param request  the request
     * @param response the response
     * @throws AxelorException the axelor exception
     * @throws IOException     the io exception
     */
    public void printAndShowInvoice(ActionRequest request, ActionResponse response) throws AxelorException, IOException {
        List<Integer> ids = Mapper.findToList(request.getContext(), "_ids");
        if (ObjectUtils.isEmpty(ids)) {
            invoiceGenerator.generate(request.getContext().asType(Invoice.class), DocGenType.PDF, true, false, response);
        } else {
            List<File> files = new ArrayList<>();
            for (Integer id : ids) {
                Invoice invoice = invoiceRepository.find(new Long(id));
                File file = invoiceGenerator.generateFile(invoice, DocGenType.PDF);
                Preconditions.checkNotNull(
                    file,
                    "Impossible de generer le fichier pour la facture proforma/facture %s",
                    invoice.getInvoiceId()
                );
                files.add(file);
            }
            File file = MetaFilesUtils.mergeFiles(INVOICE_PROFORMA_INVOICE_CLIENT_PDF, files);
            response.setView(MetaFilesUtils.showFileGenerated(INVOICE_PROFORMA_INVOICE_CLIENT_PDF, file));
        }
    }

    /**
     * Show invoice.
     *
     * @param request  the request
     * @param response the response
     * @throws IOException     the io exception
     * @throws AxelorException the axelor exception
     */
    public void showInvoice(ActionRequest request, ActionResponse response) throws IOException, AxelorException {
        printAndShowInvoice(request, response);
    }

    /**
     * Print and save invoice.
     *
     * @param request  the request
     * @param response the response
     */
    public void printAndSaveInvoice(ActionRequest request, ActionResponse response) {
        invoiceGenerator.generate(request.getContext().asType(Invoice.class), DocGenType.PDF, false, true, response);
        response.setReload(true);
    }
}
