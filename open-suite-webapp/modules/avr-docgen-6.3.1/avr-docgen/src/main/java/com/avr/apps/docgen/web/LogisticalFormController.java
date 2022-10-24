package com.avr.apps.docgen.web;

import com.avr.apps.docgen.service.generatorDocument.LogisticalFormGenerator;
import com.avr.apps.docgen.service.generatorDocument.LogisticalFormWithoutComponentGenerator;
import com.avr.apps.docgen.utils.DocGenType;
import com.axelor.apps.stock.db.LogisticalForm;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;
import com.google.inject.servlet.RequestScoped;

/**
 * *********************************** AVR SOLUTIONS ***********************************
 *
 * @author David
 * @version 1.0
 * @date 13/10/2021
 * @time 15:58
 * @Update 13/10/2021
 */
@RequestScoped
public class LogisticalFormController {

    @Inject
    LogisticalFormGenerator logisticalFormGenerator;

    @Inject
    LogisticalFormWithoutComponentGenerator logisticalFormWithoutComponentGenerator;

    /**
     *
     * @param request
     * @param response
     */
    public void printWithoutComposite(ActionRequest request, ActionResponse response) {
        LogisticalForm logisticalForm = request.getContext().asType(LogisticalForm.class);
        logisticalFormWithoutComponentGenerator.generate(logisticalForm, DocGenType.PDF, true, false, response);
    }

    /**
     *
     * @param request
     * @param response
     */
    public void printComposite(ActionRequest request, ActionResponse response) {
        LogisticalForm logisticalForm = request.getContext().asType(LogisticalForm.class);
        logisticalFormGenerator.generate(logisticalForm, DocGenType.PDF, true, false, response);
    }

    /**
     *
     * @param request
     * @param response
     */
    public void printWithoutCompositeAndSave(ActionRequest request, ActionResponse response) {
        LogisticalForm logisticalForm = request.getContext().asType(LogisticalForm.class);
        logisticalFormWithoutComponentGenerator.generate(logisticalForm, DocGenType.PDF, true, true, response);
    }

    /**
     *
     * @param request
     * @param response
     */
    public void printCompositeAndSave(ActionRequest request, ActionResponse response) {
        LogisticalForm logisticalForm = request.getContext().asType(LogisticalForm.class);
        logisticalFormGenerator.generate(logisticalForm, DocGenType.PDF, true, true, response);
    }
}
