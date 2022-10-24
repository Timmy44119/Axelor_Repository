package com.avr.apps.docgen.repository;

import com.axelor.db.Query;
import com.axelor.meta.db.MetaSelectItem;
import com.google.inject.Singleton;

@Singleton
public class MetaSelectItemAvrBaseRepository {

	public MetaSelectItem findMetaSelectBy(String value) {
		return Query.of(MetaSelectItem.class).filter("self.value = :value")
		            .bind("value", value)
		            .fetchOne();
	}

}
