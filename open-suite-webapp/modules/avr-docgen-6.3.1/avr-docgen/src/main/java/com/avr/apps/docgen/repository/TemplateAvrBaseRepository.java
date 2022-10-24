/**
 * ***********************************
 * AVR SOLUTIONS
 * ***********************************
 *
 * @author David
 * @date 18/03/2021
 * @time 15:16
 * @Update 18/03/2021
 * @version 1.0
 */
package com.avr.apps.docgen.repository;

import com.axelor.apps.message.db.Template;
import com.axelor.apps.message.db.repo.TemplateRepository;
import com.axelor.meta.db.MetaModel;

import java.util.List;

public class TemplateAvrBaseRepository extends TemplateRepository {

	public List<Template> findTemplateListByMetaModel(MetaModel metaModel) {
		return all().filter("self.metaModel.id = :metaModelId").bind("metaModelId", metaModel.getId()).fetch();
	}

}
