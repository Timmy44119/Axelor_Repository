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

import com.avr.apps.docgen.common.Mapper;
import com.avr.apps.docgen.common.utils.MetaFilesUtils;
import com.avr.apps.docgen.common.utils.ObjectUtils;
import com.avr.apps.docgen.service.generatorDocument.StockMoveGenerator;
import com.avr.apps.docgen.utils.DocGenType;
import com.axelor.apps.stock.db.StockMove;
import com.axelor.apps.stock.db.repo.StockMoveRepository;
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
 * The type Stock move controller.
 */
@RequestScoped
public class StockMoveController {

    /**
     * The Stock move repository.
     */
    @Inject
    StockMoveRepository stockMoveRepository;

    @Inject
    StockMoveGenerator stockMoveGenerator;

    /**
     * The constant BL_BR_PDF.
     */
    public static final String BL_BR_PDF = "BL-BR.pdf";

    /**
     * Print stock move.
     *
     * @param request  the request
     * @param response the response
     * @throws AxelorException the axelor exception
     * @throws IOException     the io exception
     */
    public void printStockMove(ActionRequest request, ActionResponse response) throws AxelorException, IOException {
        List<Integer> ids = Mapper.findToList(request.getContext(), "_ids");
        if (ObjectUtils.isEmpty(ids)) stockMoveGenerator.generate(
            request.getContext().asType(StockMove.class),
            DocGenType.PDF,
            true,
            false,
            response
        ); else {
            List<File> files = new ArrayList<>();
            for (Integer id : ids) {
                StockMove stockMove = stockMoveRepository.find(new Long(id));
                File file = stockMoveGenerator.generateFile(stockMove, DocGenType.PDF);
                Preconditions.checkNotNull(
                    file,
                    "Impossible de generer le fichier pour le BL/BR %s",
                    stockMove.getStockMoveSeq()
                );
                files.add(file);
            }
            File file = MetaFilesUtils.mergeFiles(BL_BR_PDF, files);
            response.setView(MetaFilesUtils.showFileGenerated(BL_BR_PDF, file));
        }
    }

    /**
     * Print stock move.
     *
     * @param request  the request
     * @param response the response
     * @throws AxelorException the axelor exception
     * @throws IOException     the io exception
     */
    public void savePrintStockMove(ActionRequest request, ActionResponse response) {
        stockMoveGenerator.generateToAttachment(request.getContext().asType(StockMove.class), DocGenType.PDF, response);
        response.setReload(true);
    }
}
