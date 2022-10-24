package com.avr.apps.docgen.csv.script;

import com.avr.apps.docgen.db.DocgenChildBinding;
import com.axelor.db.Query;
import com.axelor.meta.db.MetaField;
import java.util.Map;

/**
 * *********************************** AVR SOLUTIONS ***********************************
 *
 * @author David
 * @version 1.0
 * @date 11/08/2021
 * @time 12:45
 * @Update 11/08/2021
 */
public class ImportChildBinding {

    public Object importMetaField(Object bean, Map<String, Object> values) {
        assert bean instanceof DocgenChildBinding;

        DocgenChildBinding docgenChildBinding = (DocgenChildBinding) bean;
        String metaFieldElement = values.get("metaFieldElement").toString();
        String[] metaFieldElementSplitter = metaFieldElement.split("-");

        docgenChildBinding.setMetaField(
            Query
                .of(MetaField.class)
                .filter("self.metaModel.fullName = :fullName AND self.name = :name")
                .bind("fullName", metaFieldElementSplitter[0])
                .bind("name", metaFieldElementSplitter[1])
                .fetchOne()
        );

        metaFieldElement = values.get("metaFieldOrderElement").toString();
        metaFieldElementSplitter = metaFieldElement.split("-");
        if (metaFieldElement.length() > 0) {
            docgenChildBinding.setMetaFieldOrder(
                Query
                    .of(MetaField.class)
                    .filter("self.metaModel.fullName = :fullName AND self.name = :name")
                    .bind("fullName", metaFieldElementSplitter[0])
                    .bind("name", metaFieldElementSplitter[1])
                    .fetchOne()
            );
        }

        docgenChildBinding.setIsOrderData(Boolean.parseBoolean(values.get("isOrderDataToBoolean").toString()));

        return docgenChildBinding;
    }
}
