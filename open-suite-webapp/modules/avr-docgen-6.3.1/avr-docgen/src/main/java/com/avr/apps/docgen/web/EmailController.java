/**
 * ***********************************
 * AVR SOLUTIONS
 * ***********************************
 *
 * @author David
 * @date 17/03/2021
 * @time 10:05
 * @Update 17/03/2021
 * @version 1.0
 */
package com.avr.apps.docgen.web;

import com.avr.apps.base.db.EmailWizard;
import com.avr.apps.docgen.exception.AvrException;
import com.avr.apps.docgen.exception.EmailException;
import com.avr.apps.docgen.service.interfaces.EmailAvrBaseService;
import com.axelor.db.Model;
import com.axelor.db.Query;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import javax.mail.MessagingException;

@Singleton
public class EmailController {

    @Inject
    EmailAvrBaseService emailAvrBaseService;

    /**
     *
     * @param request
     * @param response
     */
    public void showEmailWizard(ActionRequest request, ActionResponse response) {
        response.setView(
            emailAvrBaseService.showEmailWizard(request.getModel(), new Long(request.getContext().get("id").toString()))
        );
    }

    /**
     *
     * @param request
     * @param response
     */
    @SuppressWarnings("unchecked")
    public void sendByWithAttachment(ActionRequest request, ActionResponse response)
        throws ClassNotFoundException, EmailException, MessagingException, AvrException {
        EmailWizard emailWizard = request.getContext().asType(EmailWizard.class);
        Class<? extends Model> cls = (Class<? extends Model>) Class.forName(emailWizard.getEntity());
        Model model = Query.of(cls).filter("self.id = :id").bind("id", emailWizard.getIdentities().split(",")[0]).fetchOne();
        emailAvrBaseService.sendMailWithAttachment(
            model,
            emailWizard.getTemplate(),
            emailWizard.getEmailAccount(),
            emailWizard.getMetaFileList()
        );
        response.setNotify("Le mail à été envoyé");
        response.setCanClose(true);
    }

    /**
     *
     * @param request
     * @param response
     */
    public void getDomainMetaFileAttachment(ActionRequest request, ActionResponse response) {
        EmailWizard emailWizard = request.getContext().asType(EmailWizard.class);
        String ids = emailWizard.getIdentities();
        String domain;
        if (ids == null) domain = "self.id = 0"; else domain =
            emailAvrBaseService.getDomainMetaFileAttachment(emailWizard.getEntity(), new Long(ids.split(",")[0]));

        response.setAttr("metaFileList", "domain", domain);
    }

    /**
     *
     * @param request
     * @param response
     */
    public void getDomainByModel(ActionRequest request, ActionResponse response) {
        EmailWizard emailWizard = request.getContext().asType(EmailWizard.class);
        response.setAttr("template", "domain", emailAvrBaseService.getDomainTemplateByModel(emailWizard.getEntity()));
    }

    /**
     *
     * @param request
     * @param response
     */
    public void getTemplateIfAlone(ActionRequest request, ActionResponse response) {
        EmailWizard emailWizard = request.getContext().asType(EmailWizard.class);
        response.setValue(
            "template",
            emailAvrBaseService.getTemplateIfAlone(emailAvrBaseService.findMetaModelByPackageNameAndName(emailWizard.getEntity()))
        );
    }

    /**
     *
     * @param request
     * @param response
     */
    public void getMetaFileIfAlone(ActionRequest request, ActionResponse response) {
        EmailWizard emailWizard = request.getContext().asType(EmailWizard.class);
        String ids = emailWizard.getIdentities();
        response.setValue(
            "metaFileList",
            emailAvrBaseService.getMetaFileIfAlone(emailWizard.getEntity(), new Long(ids.split(",")[0]))
        );
    }
}
