package com.avr.apps.docgen.web;

import com.avr.apps.docgen.service.generatorDocument.ContractGenerator;
import com.avr.apps.docgen.utils.DocGenType;
import com.axelor.apps.contract.db.Contract;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.exception.service.TraceBackService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;
import com.google.inject.Singleton;

/**
 * *********************************** AVR SOLUTIONS ***********************************
 *
 * @author David
 * @version 1.0
 * @date 13/10/2021
 * @time 14:28
 * @Update 13/10/2021
 */
@Singleton
public class ContractController {

    @Inject
    private ContractGenerator contractGenerator;

    /**
     *
     * @param request
     * @param response
     */
    public void printContract(ActionRequest request, ActionResponse response) throws AxelorException {
        Contract contract = request.getContext().asType(Contract.class);

        if (!contractGenerator.generate(contract, DocGenType.PDF, true, false, response)) {
            TraceBackService.trace(
                new AxelorException(
                    TraceBackRepository.CATEGORY_NO_VALUE,
                    "Impossible de générer le contrat %s",
                    contractGenerator.getError()
                )
            );
            return;
        }
        response.setNotify("Le contract à été généré");
    }

    /**
     *
     * @param request
     * @param response
     */
    public void printAndSaveContract(ActionRequest request, ActionResponse response) {
        Contract contract = request.getContext().asType(Contract.class);
        contractGenerator.generateToAttachment(contract, DocGenType.PDF, response);
        response.setReload(true);
    }
}
