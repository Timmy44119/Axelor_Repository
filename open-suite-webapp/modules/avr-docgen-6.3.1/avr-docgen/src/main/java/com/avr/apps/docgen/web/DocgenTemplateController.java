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

import com.avr.apps.docgen.common.Mapper;
import com.avr.apps.docgen.common.utils.MetaFilesUtils;
import com.avr.apps.docgen.common.utils.ObjectUtils;
import com.avr.apps.docgen.db.DocgenTemplate;
import com.avr.apps.docgen.db.repo.DocgenBindingRepository;
import com.avr.apps.docgen.db.repo.DocgenChildBindingRepository;
import com.avr.apps.docgen.db.repo.DocgenModelRepository;
import com.avr.apps.docgen.db.repo.DocgenTemplateRepository;
import com.avr.apps.docgen.internal.DocgenWorkflowService;
import com.avr.apps.docgen.repository.MetaSelectItemAvrBaseRepository;
import com.avr.apps.docgen.service.DocgenTemplateServiceImpl;
import com.avr.apps.docgen.service.interfaces.DocgenTraitmentDataService;
import com.avr.apps.docgen.utils.DocGenType;
import com.axelor.db.Model;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.inject.Beans;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.axelor.meta.db.MetaSelectItem;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.servlet.RequestScoped;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;

@RequestScoped
public class DocgenTemplateController {

    public static final String TITLE_DOCUMENT = "Test document de type %s";
    public static final String SEQUENCE = "SEQUENCE";

    @Inject
    DocgenTemplateServiceImpl docgenTemplateService;

    @Inject
    DocgenWorkflowService docgenWorkflowService;

    @Inject
    MetaSelectItemAvrBaseRepository metaSelectItemRepository;

    @Inject
    DocgenTraitmentDataService docgenTraitmentDataService;

    /**
     * @param request
     * @param response
     */
    public void showWizard(ActionRequest request, ActionResponse response) {
        DocgenTemplate docgenTemplate = request.getContext().asType(DocgenTemplate.class);
        Preconditions.checkArgument(docgenTemplate.getModelDefault() != null, "model par default non trouvé");
        response.setView(docgenTemplateService.computedWizardViewBy(request.getContext().asType(DocgenTemplate.class)));
    }

    /**
     * @param request
     * @param response
     */
    public void getMetaModelBySubType(ActionRequest request, ActionResponse response) {
        DocgenTemplate docgenTemplate = request.getContext().asType(DocgenTemplate.class);
        if (docgenTemplate.getDocgenSubType() == null) return;
        response.setValue("metaModel", request.getContext().asType(DocgenTemplate.class).getDocgenSubType().getMetaModel());
    }

    /**
     * @param request
     * @param response
     */
    public void startTest(ActionRequest request, ActionResponse response) throws Exception {
        DocgenTemplate docgenTemplate = request.getContext().asType(DocgenTemplate.class);
        if (docgenTemplate.getRelatedToSelectId() == 0) {
            response.setError("A Record don't be empty");
            return;
        }
        Model data = docgenTemplateService.getDataFromId(
            docgenTemplateService.getRepositoryBy(docgenTemplate.getRelatedToSelect()),
            docgenTemplate.getRelatedToSelectId()
        );
        if (ObjectUtils.isEmpty(docgenTemplate.getPartner())) {
            response.setError("Partner not found");
            return;
        }
        DocgenTemplate docgenTemplateParent = Beans.get(DocgenTemplateRepository.class).find(docgenTemplate.getId());
        MetaSelectItem metaSelectBy = metaSelectItemRepository.findMetaSelectBy(docgenTemplate.getRelatedToSelect());
        String titleDocument = String.format(TITLE_DOCUMENT, metaSelectBy.getTitle().toLowerCase());
        File file = docgenWorkflowService.generateDocument(
            data,
            docgenTemplate.getPartner(),
            SEQUENCE,
            docgenWorkflowService.computedMetaFileLanguage(docgenTemplateParent, docgenTemplate.getPartner()),
            docgenTemplateParent,
            DocGenType.PDF
        );
        response.setView(MetaFilesUtils.showFileGenerated(titleDocument, file));
        response.setCanClose(true);
    }

    /**
     * @param request
     * @param response
     */
    public void getListOfPartner(ActionRequest request, ActionResponse response) throws ClassNotFoundException {
        DocgenTemplate docgenTemplate = request.getContext().asType(DocgenTemplate.class);
        Model data = docgenTemplateService.getDataFromId(
            docgenTemplateService.getRepositoryBy(docgenTemplate.getRelatedToSelect()),
            docgenTemplate.getRelatedToSelectId()
        );
        response.setAttr(
            "partner",
            "domain",
            String.format(
                "self.id in (%s)",
                docgenTemplateService.getPartnersBy(data).stream().map(Object::toString).collect(Collectors.joining(","))
            )
        );
    }

    /**
     * @param request
     * @param response
     */
    public void checkModelTemplateUniqueLanguage(ActionRequest request, ActionResponse response) {
        DocgenTemplate docgenTemplate = request.getContext().asType(DocgenTemplate.class);
        if (
            docgenTemplate.getModelTemplateList().size() !=
            new HashSet<>(
                docgenTemplate.getModelTemplateList().stream().map(it -> it.getLanguage().getCode()).collect(Collectors.toList())
            )
                .size()
        ) {
            response.setError("La langue doit être unique !");
        }
    }

    /**
     * @param request
     * @param response
     */
    public void importData(ActionRequest request, ActionResponse response) throws AxelorException, ZipException {
        DocgenTemplate docgenTemplate = request.getContext().asType(DocgenTemplate.class);

        Preconditions.checkNotNull(docgenTemplate.getMetaFileImportZip(), "Le fichier de configuration doit être remplis");
        MetaFile metaFileImportZip = docgenTemplate.getMetaFileImportZip();

        if (!metaFileImportZip.getFileType().equals("application/x-zip-compressed")) throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            "format de fichier incorrect, zip attendu"
        );

        File[] files = docgenTraitmentDataService.extractZip(metaFileImportZip);
        if (docgenTraitmentDataService.importData(Arrays.asList(files)) == 0) {
            response.setNotify("Les données ont été importées. Merci de réactualiser la page.");
        } else {
            response.setError("Des erreurs ont été détectées. Veuillez vérifier les logs.");
        }
        response.setCanClose(true);
    }

    /**
     * @param request
     * @param response
     */
    public void exportData(ActionRequest request, ActionResponse response) throws IOException {
        List<Long> ids = Mapper.findByClass(request.getContext(), "_ids");
        if(ids == null) {
            ids = docgenTraitmentDataService.getAllIdsDocgenTemplate();
        }
        MetaFile metaFile = docgenTraitmentDataService.exportFileToZipFile(ids);
        response.setView(
            ActionView
                .define(metaFile.getFileName())
                .add("html", String.format("ws/rest/com.axelor.meta.db.MetaFile/%s/content/download", metaFile.getId()))
                .param("download", "true")
                .map()
        );
    }

    /**
     * @param request
     * @param response
     */
    public void showViewImportWizard(ActionRequest request, ActionResponse response) {
        response.setView(
            ActionView
                .define("Import Wizard")
                .model(DocgenTemplate.class.getName())
                .add("form", "avr-docgen-import-wizard-form")
                .param("popup", "reload")
                .param("show-toolbar", "false")
                .param("show-confirm", "false")
                .param("popup-save", "false")
                .context("_showRecord", request.getContext().asType(DocgenTemplate.class).getId())
                .map()
        );
    }
}
