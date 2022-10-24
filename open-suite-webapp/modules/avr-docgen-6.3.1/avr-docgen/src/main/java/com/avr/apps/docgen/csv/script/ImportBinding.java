package com.avr.apps.docgen.csv.script;

import com.avr.apps.docgen.db.DocgenBinding;
import com.avr.apps.docgen.db.DocgenChildBinding;
import com.avr.apps.docgen.db.DocgenTemplate;
import com.avr.apps.docgen.db.TypeData;
import com.avr.apps.docgen.db.TypeTemplate;
import com.axelor.db.Query;
import com.axelor.meta.db.MetaField;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * *********************************** AVR SOLUTIONS ***********************************
 *
 * @author David
 * @version 1.0
 * @date 11/08/2021
 * @time 12:45
 * @Update 11/08/2021
 */
public class ImportBinding {

    static Logger logger = LoggerFactory.getLogger(ImportBinding.class);

    public Object importMetaField(Object bean, Map<String, Object> values) {
        assert bean instanceof DocgenBinding;
        DocgenBinding docgenBinding = (DocgenBinding) bean;

        try {

            if (docgenBinding == null) {
                docgenBinding = new DocgenBinding();
            }

            docgenBinding.setKeyBinding(values.get("keyBinding").toString());
            docgenBinding.setTypeTemplate(TypeTemplate.valueOf(values.get("typeTemplate").toString()));
            docgenBinding.setTargetField(values.get("targetField").toString());
            docgenBinding.setTypeData(TypeData.valueOf(values.get("typeData").toString()));
            docgenBinding.setHasDateOnlyReturning(Boolean.valueOf(values.get("hasDateOnlyReturning").toString()));
            docgenBinding.setQuery(values.get("query").toString());
            docgenBinding.setBigDecimalScale(Integer.valueOf(values.get("bigDecimalScale").toString()));
            docgenBinding.setImportId(values.get("importId").toString());

            String metaFieldElement = values.get("metaFieldElement").toString();

            if (metaFieldElement != null && !metaFieldElement.equals("0")) {
                String[] metaFieldElementSplitter = metaFieldElement.split("-");
                docgenBinding.setMetaField(
                        Query
                                .of(MetaField.class)
                                .filter("self.metaModel.fullName = :fullName AND self.name = :name")
                                .bind("fullName", metaFieldElementSplitter[0])
                                .bind("name", metaFieldElementSplitter[1])
                                .fetchOne()
                );
            }

            DocgenTemplate docgenTemplate = Query
                    .of(DocgenTemplate.class)
                    .filter("self.importId = :importId")
                    .bind("importId", values.get("docgenTemplateImportId").toString())
                    .fetchOne();
            docgenBinding.setDocgenTemplate(docgenTemplate);

            DocgenChildBinding docgenChildBinding = Query
                    .of(DocgenChildBinding.class)
                    .filter("self.importId = :importId")
                    .bind("importId", values.get("docgenChildBindingImportId").toString())
                    .fetchOne();
            docgenBinding.setDocgenChildBinding(docgenChildBinding);

            return docgenBinding;
        } catch(Exception e) {
            System.err.println(String.format("error on %s", bean));
            e.printStackTrace();
        }
        return docgenBinding;
    }
}
