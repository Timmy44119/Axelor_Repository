/**
 * ***********************************
 * AVR SOLUTIONS
 * ***********************************
 *
 * @author David
 * @date 16/03/2021
 * @time 15:05
 * @Update 16/03/2021
 * @version 1.0
 */
package com.avr.apps.docgen.service;

import com.avr.apps.base.db.EmailWizard;
import com.avr.apps.docgen.exception.AvrException;
import com.avr.apps.docgen.exception.EmailException;
import com.avr.apps.docgen.repository.DMSFileAvrBaseRepository;
import com.avr.apps.docgen.repository.MetaModelAvrBaseRepository;
import com.avr.apps.docgen.repository.TemplateAvrBaseRepository;
import com.avr.apps.docgen.common.utils.ObjectUtils;
import com.avr.apps.docgen.common.Email;
import com.avr.apps.docgen.service.interfaces.EmailAvrBaseService;
import com.axelor.apps.message.db.EmailAccount;
import com.axelor.apps.message.db.Template;
import com.axelor.db.Model;
import com.axelor.dms.db.DMSFile;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.axelor.meta.db.MetaModel;
import com.axelor.meta.schema.actions.ActionView;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.mail.MessagingException;

/**
 * The type Email avr base service.
 */
@Singleton
public class EmailAvrBaseServiceImpl implements EmailAvrBaseService {

    /**
     * The Dms file repository.
     */
    protected DMSFileAvrBaseRepository dmsFileRepository;
    /**
     * The Meta model repository.
     */
    protected MetaModelAvrBaseRepository metaModelRepository;
    /**
     * The Template repository.
     */
    protected TemplateAvrBaseRepository templateRepository;

    /**
     * Instantiates a new Email avr base service.
     *
     * @param dmsFileRepository   the dms file repository
     * @param metaModelRepository the meta model repository
     * @param templateRepository  the template repository
     */
    @Inject
    public EmailAvrBaseServiceImpl(
        DMSFileAvrBaseRepository dmsFileRepository,
        MetaModelAvrBaseRepository metaModelRepository,
        TemplateAvrBaseRepository templateRepository
    ) {
        this.dmsFileRepository = dmsFileRepository;
        this.metaModelRepository = metaModelRepository;
        this.templateRepository = templateRepository;
    }

    @Override
    public void sendMailWithAttachment(Model object, Template template, EmailAccount emailAccount, List<MetaFile> files)
        throws MessagingException, AvrException, EmailException {
        Email email = new Email(template, emailAccount).withObject(object);
        for (MetaFile file : files) {
            email.withFile(MetaFiles.getPath(file).toFile());
        }
        email.send();
    }

    @Override
    public Map<String, Object> showEmailWizard(String clazzName, Long id) {
        return ActionView
            .define("Email Wizard")
            .model(EmailWizard.class.getName())
            .add("form", "avr-base-email-wizard-form")
            .param("forceEdit", "true")
            .param("popup", "true")
            .param("show-toolbar", "false")
            .param("show-confirm", "false")
            .param("forceEdit", "true")
            .param("popup-save", "false")
            .context("_entity", clazzName)
            .context("_identities", id.toString())
            .map();
    }

    @Override
    public List<MetaFile> getFilesByModelAndId(String model, Long id) {
        List<DMSFile> dmsFileByModelAndId = dmsFileRepository.findDmsFileByModelAndId(model, id);
        List<MetaFile> metaFiles = new ArrayList<>();
        dmsFileByModelAndId.forEach(dms -> metaFiles.add(dms.getMetaFile()));
        return metaFiles;
    }

    @Override
    public <T extends Model> List<MetaFile> getFilesByModelAndId(Class<T> model, Long id) {
        return getFilesByModelAndId(model.getName(), id);
    }

    @Override
    public String getDomainMetaFileAttachment(String model, Long id) {
        StringBuilder domain = new StringBuilder("self.id in (");
        List<MetaFile> filesByModelAndId = getFilesByModelAndId(model, id);
        domain.append(
            ObjectUtils.eval(
                () -> filesByModelAndId.stream().map(it -> it.getId().toString()).collect(Collectors.joining(",")),
                null
            )
        );
        domain.append(")");
        return domain.toString();
    }

    @Override
    public <T extends Model> String getDomainMetaFileAttachment(Class<T> model, Long id) {
        return getDomainMetaFileAttachment(model.getName(), id);
    }

    @Override
    public String getDomainTemplateByModel(String model) {
        return String.format(
            "self.metaModel = %s",
            ObjectUtils.eval(() -> findMetaModelByPackageNameAndName(model).getId(), null)
        );
    }

    @Override
    public MetaModel findMetaModelByPackageNameAndName(String model) {
        String[] m = model.split(Pattern.quote("."));
        String entityName = m[m.length - 1];
        String packageName = String.join(".", Arrays.copyOf(m, m.length - 1));
        return metaModelRepository.findMetaModelByPackageNameAndName(packageName, entityName);
    }

    @Override
    public Template getTemplateIfAlone(MetaModel metaModel) {
        List<Template> templateListByMetaModel = getTemplateListByMetaModel(metaModel);
        if (templateListByMetaModel.size() == 1) return templateListByMetaModel.get(0);
        return null;
    }

    @Override
    public List<MetaFile> getMetaFileIfAlone(String model, Long id) {
        List<MetaFile> filesByModelAndId = getFilesByModelAndId(model, id);
        if (filesByModelAndId.size() == 1) return filesByModelAndId;
        return null;
    }

    @Override
    public List<Template> getTemplateListByMetaModel(MetaModel metaModel) {
        return templateRepository.findTemplateListByMetaModel(metaModel);
    }
}
