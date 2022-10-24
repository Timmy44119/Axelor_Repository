package com.avr.apps.docgen.repository;

import com.avr.apps.docgen.db.DocgenSubType;
import com.avr.apps.docgen.db.DocgenTemplate;
import com.axelor.db.Query;
import com.google.inject.Singleton;

@Singleton
public class DocgenTemplateDocgenRepository {

    public DocgenTemplate findTemplateReport(DocgenSubType subType) {
        return Query.of(DocgenTemplate.class).filter("self.docgenSubType = :subtype").bind("subtype", subType).fetchOne();
    }
}
