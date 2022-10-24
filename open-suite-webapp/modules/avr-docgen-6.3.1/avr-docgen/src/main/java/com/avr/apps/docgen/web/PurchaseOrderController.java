/**
 * ***********************************
 *          AVR SOLUTIONS            *
 * ***********************************
 *
 *
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
import com.avr.apps.docgen.service.generatorDocument.PurchaseOrderGenerator;
import com.avr.apps.docgen.service.interfaces.PurchaseOrderAvrDocgenService;
import com.avr.apps.docgen.utils.DocGenType;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.apps.purchase.db.repo.PurchaseOrderRepository;
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
 * The type Purchase order controller.
 */
@RequestScoped
public class PurchaseOrderController {

    /**
     * The Purchase order repository.
     */
    @Inject
    PurchaseOrderRepository purchaseOrderRepository;

    @Inject
    PurchaseOrderGenerator purchaseOrderGenerator;

    @Inject
    PurchaseOrderAvrDocgenService purchaseOrderAvrSupplychainService;

    /**
     * The constant DEVIS_COMMANDE_FOURNISSEUR_PDF.
     */
    public static final String DEVIS_COMMANDE_FOURNISSEUR_PDF = "devis-commande fournisseur.pdf";

    /**
     * Show purchase order.
     *
     * @param request  the request
     * @param response the response
     * @throws AxelorException the axelor exception
     * @throws IOException     the io exception
     */
    public void showPurchaseOrder(ActionRequest request, ActionResponse response) throws AxelorException, IOException {
        List<Integer> ids = Mapper.findToList(request.getContext(), "_ids");
        if (ObjectUtils.isEmpty(ids)) purchaseOrderGenerator.generate(
            request.getContext().asType(PurchaseOrder.class),
            DocGenType.PDF,
            true,
            false,
            response
        ); else {
            List<File> files = new ArrayList<>();
            for (Integer id : ids) {
                PurchaseOrder purchaseOrder = purchaseOrderRepository.find(new Long(id));
                File file = purchaseOrderGenerator.generateFile(purchaseOrder, DocGenType.PDF);
                Preconditions.checkNotNull(
                    file,
                    "Impossible de generer le fichier pour le devis/commande fournisseur %s",
                    purchaseOrder.getPurchaseOrderSeq()
                );
                files.add(file);
            }
            File file = MetaFilesUtils.mergeFiles(DEVIS_COMMANDE_FOURNISSEUR_PDF, files);
            response.setView(MetaFilesUtils.showFileGenerated(DEVIS_COMMANDE_FOURNISSEUR_PDF, file));
        }
    }

    /**
     * Show purchase order.
     *
     * @param request  the request
     * @param response the response
     */
    public void savePurchaseOrder(ActionRequest request, ActionResponse response) {
        purchaseOrderGenerator.generate(request.getContext().asType(PurchaseOrder.class), DocGenType.PDF, false, true, response);
        response.setReload(true);
    }

    /**
     * Computed deadline
     *
     * @param request
     * @param response
     */
    public void computedDeadLine(ActionRequest request, ActionResponse response) {
        response.setValue(
            "deadlineDate",
            purchaseOrderAvrSupplychainService.computedDeadLineDate(request.getContext().asType(PurchaseOrder.class))
        );
    }
}
