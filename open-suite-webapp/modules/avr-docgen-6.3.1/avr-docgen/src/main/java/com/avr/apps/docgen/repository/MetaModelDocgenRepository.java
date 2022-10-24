package com.avr.apps.docgen.repository;

import com.axelor.db.Query;
import com.axelor.meta.db.MetaModel;
import com.google.inject.Singleton;

@Singleton
public class MetaModelDocgenRepository {


	public MetaModel findMetaModelBy(String fullName) {
		return Query.of(MetaModel.class)
		            .filter("self.fullName = :fullName")
		            .bind("fullName", fullName)
		            .fetchOne();
	}

}
