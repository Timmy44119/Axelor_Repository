package com.avr.apps.docgen.service.generatorDocument;

import com.avr.apps.docgen.common.utils.MetaFilesUtils;
import com.avr.apps.docgen.db.DocgenSubType;
import com.avr.apps.docgen.db.DocgenTemplate;
import com.avr.apps.docgen.exception.FieldRequiredException;
import com.avr.apps.docgen.internal.DocgenWorkflowService;
import com.avr.apps.docgen.service.interfaces.AppDocgenService;
import com.avr.apps.docgen.utils.DocGenType;
import com.avr.apps.docgen.utils.LangUtils;
import com.axelor.apps.base.db.AppDocgen;
import com.axelor.apps.base.db.Partner;
import com.axelor.auth.db.AuditableModel;
import com.axelor.db.Model;
import com.axelor.db.Query;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.meta.db.MetaFile;
import com.axelor.rpc.ActionResponse;
import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.function.Function;

public abstract class GeneratorDocument<T extends Model> {

    private final DocgenWorkflowService docgenWorkflowService;
    private final AppDocgenService appDocgenService = Beans.get(AppDocgenService.class);

    private Logger log = LoggerFactory.getLogger(GeneratorDocument.class);

    private String error = null;

    public GeneratorDocument() {
        this.docgenWorkflowService = Beans.get(DocgenWorkflowService.class);
    }

    protected abstract String getSequence(T t);

    protected abstract Partner getPartner(T t) throws FieldRequiredException;

    public abstract String getTypeName(T t);

    protected abstract DocgenSubType getType(T t);

    public boolean generate(T bean, DocGenType type, boolean isShow, boolean addInAttachment, ActionResponse response) {
        try {
            log.debug("bean from generate {}", bean);
            bean = reloadToDatabase(bean);
            log.debug("bean from generate after reload database {}", bean);
            DocgenTemplate docgenTemplate = docgenWorkflowService.getDocgenTemplate(getType(bean));
            Partner partner = getPartner(bean);
            Preconditions.checkNotNull(
                docgenTemplate,
                String.format(
                    "la configuration pour %s n'a pas été trouvé, merci de regarder dans la configuration docgen",
                    getTypeName(bean)
                )
            );
            MetaFile metaFile = docgenWorkflowService.computedMetaFileLanguage(docgenTemplate, partner);
            Preconditions.checkNotNull(
                metaFile,
                String.format(
                    "Impossible de trouver le fichier de template de %s, merci de regarder dans le module docgen",
                    docgenTemplate.getName()
                )
            );
            File file = docgenWorkflowService.generateDocument(bean, partner, getSequence(bean), metaFile, docgenTemplate, type);
            if (isShow) response.setView(MetaFilesUtils.showFileGenerated(file.getName(), file));
            if (addInAttachment) MetaFilesUtils.uploadFileAndAttach(file, bean);
            return true;
        } catch (Exception e) {
            error = e.getMessage();
            TraceBackService.trace(e);
            if (response != null) response.setError(String.format("Erreur lors de la génération - %s", e.getMessage()));
            return false;
        }
    }

    public String getError() {
        String err = error;
        error = null;
        return err;
    }

    protected AppDocgen getApp() {
        return appDocgenService.getAppDocgen();
    }

    public boolean generateToAttachment(T t, DocGenType type, ActionResponse response) {
        return generate(t, type, false, true, response);
    }

    public boolean generateToAttachment(T t, DocGenType type) {
        return generate(t, type, false, true, null);
    }

    public File generateFile(T t, DocGenType type) {
        try {
            t = reloadToDatabase(t);
            DocgenTemplate docgenTemplate = docgenWorkflowService.getDocgenTemplate(getType(t));
            Partner partner = getPartner(t);
            Preconditions.checkNotNull(
                docgenTemplate,
                String.format(
                    "la configuration pour %s n'a pas été trouvé, merci de regarder dans la configuration docgen",
                    getTypeName(t)
                )
            );
            MetaFile metaFile = docgenWorkflowService.computedMetaFileLanguage(docgenTemplate, partner);
            return docgenWorkflowService.generateDocument(t, partner, getSequence(t), metaFile, docgenTemplate, type);
        } catch (Exception e) {
            TraceBackService.trace(e);
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private T reloadToDatabase(T t) {
        Class<? extends Model> aClass = (Class<? extends Model>) t.getClass().getSuperclass();
        if (aClass.getName().equals(AuditableModel.class.getName())) aClass = t.getClass();
        return (T) Query.of(aClass).filter("self.id = ?", t.getId()).fetchOne();
    }

    @SuppressWarnings("unchecked")
    protected <T extends Model> void checkValidityField(T bean, Function<? super T, ?> method) throws FieldRequiredException {
        if(method.apply(bean) == null) {
            throw new FieldRequiredException(LangUtils.nameOfProperty((Class<T>) bean.getClass(), method));
        }
    }

}
