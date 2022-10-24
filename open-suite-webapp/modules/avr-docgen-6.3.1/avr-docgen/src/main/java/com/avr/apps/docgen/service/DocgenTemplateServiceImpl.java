package com.avr.apps.docgen.service;

import com.avr.apps.docgen.db.DocgenTemplate;
import com.axelor.apps.base.db.Partner;
import com.axelor.common.ObjectUtils;
import com.axelor.db.JpaRepository;
import com.axelor.db.Model;
import com.axelor.db.mapper.Mapper;
import com.axelor.db.mapper.Property;
import com.axelor.meta.schema.actions.ActionView;
import com.google.inject.Singleton;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
@Singleton
public class DocgenTemplateServiceImpl {

    public Model getDataFromId(JpaRepository<Model> repo, Long id) {
        return repo.find(id);
    }

    public Set<Long> getPartnersBy(Model model) {
        Set<Long> partnerIds = new HashSet<>();
        Mapper mapper = Mapper.of(model.getClass());
        for (Property property : mapper.getProperties()) {
            if (property.getJavaType().getName().equals("com.axelor.apps.base.db.Partner")) {
                Partner p = (Partner) property.get(model);
                if (ObjectUtils.isEmpty(p)) continue;
                partnerIds.add(p.getId());
            }
        }
        return partnerIds;
    }

    public Map<String, Object> computedWizardViewBy(DocgenTemplate docgenTemplate) {
        return ActionView
            .define("Test Template Wizard")
            .model(DocgenTemplate.class.getName())
            .add("form", "avr-docgen-template-wizard-form")
            .param("show-toolbar", "false")
            .param("show-confirm", "false")
            .param("popup", "true")
            .param("popup-save", "false")
            .context("showRecord", docgenTemplate.getId())
            .context("_relatedToSelect", docgenTemplate.getMetaModel().getFullName())
            .map();
    }

    @SuppressWarnings("unchecked")
    public JpaRepository<Model> getRepositoryBy(String fullName) throws ClassNotFoundException {
        Class<Model> klass = (Class<Model>) Class.forName(fullName);
        return JpaRepository.of(klass);
    }
}
