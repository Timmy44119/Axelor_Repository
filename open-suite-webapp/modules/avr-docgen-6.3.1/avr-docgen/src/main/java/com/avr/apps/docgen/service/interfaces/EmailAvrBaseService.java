/**
 * ***********************************
 * AVR SOLUTIONS
 * ***********************************
 *
 * @author David
 * @date 16/03/2021
 * @time 15:06
 * @Update 16/03/2021
 * @version 1.0
 */
package com.avr.apps.docgen.service.interfaces;

import com.avr.apps.docgen.exception.AvrException;
import com.avr.apps.docgen.exception.EmailException;
import com.axelor.apps.message.db.EmailAccount;
import com.axelor.apps.message.db.Template;
import com.axelor.db.Model;
import com.axelor.meta.db.MetaFile;
import com.axelor.meta.db.MetaModel;
import java.util.List;
import java.util.Map;
import javax.mail.MessagingException;

/**
 * The interface Email avr base service.
 */
public interface EmailAvrBaseService {
    /**
     * Send mail with attachment.
     *
     * @param object       the object
     * @param template     the template
     * @param emailAccount the email account
     * @param files        the files
     * @throws MessagingException the messaging exception
     * @throws AvrException       the avr exception
     * @throws EmailException     the email exception
     */
    void sendMailWithAttachment(Model object, Template template, EmailAccount emailAccount, List<MetaFile> files)
        throws MessagingException, AvrException, EmailException;

    /**
     * Show email wizard map.
     *
     * @param clazzName the clazz name
     * @param id        the id
     * @return the map
     */
    Map<String, Object> showEmailWizard(String clazzName, Long id);

    /**
     * Gets files by model and id.
     *
     * @param model the model
     * @param id    the id
     * @return the files by model and id
     */
    List<MetaFile> getFilesByModelAndId(String model, Long id);

    /**
     * Gets files by model and id.
     *
     * @param <T>   the type parameter
     * @param model the model
     * @param id    the id
     * @return the files by model and id
     */
    <T extends Model> List<MetaFile> getFilesByModelAndId(Class<T> model, Long id);

    /**
     * Gets domain meta file attachment.
     *
     * @param model the model
     * @param id    the id
     * @return the domain meta file attachment
     */
    String getDomainMetaFileAttachment(String model, Long id);

    /**
     * Gets domain meta file attachment.
     *
     * @param <T>   the type parameter
     * @param model the model
     * @param id    the id
     * @return the domain meta file attachment
     */
    <T extends Model> String getDomainMetaFileAttachment(Class<T> model, Long id);

    /**
     * Gets domain template by model.
     *
     * @param model the model
     * @return the domain template by model
     */
    String getDomainTemplateByModel(String model);

    /**
     * Find meta model by package name and name meta model.
     *
     * @param model the model
     * @return the meta model
     */
    MetaModel findMetaModelByPackageNameAndName(String model);

    /**
     * Gets template if alone.
     *
     * @param metaModel the meta model
     * @return the template if alone
     */
    Template getTemplateIfAlone(MetaModel metaModel);

    List<MetaFile> getMetaFileIfAlone(String model, Long id);

    /**
     * Gets template list by meta model.
     *
     * @param metaModel the meta model
     * @return the template list by meta model
     */
    List<Template> getTemplateListByMetaModel(MetaModel metaModel);
}
