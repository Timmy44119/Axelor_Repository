package com.avr.apps.docgen.service;

import com.avr.apps.docgen.common.ValidatorFields;
import com.axelor.meta.db.MetaField;
import com.axelor.meta.db.MetaModel;
import com.axelor.rpc.ActionResponse;

public interface MetaFieldOverrideSerivce {
	void selectedFields(ValidatorFields validatorFields, ActionResponse response, boolean camelCase, boolean deep);
	void selectedFields(ValidatorFields validatorFields, ActionResponse response, boolean camelCase);
	void selectedFields(ValidatorFields validatorFields, ActionResponse response);
	void getDomain(ValidatorFields validatorFields, MetaModel metaModel, ActionResponse response, boolean autorise, String... limitedTypeFields);
	void getDomain(ValidatorFields validatorFields, MetaModel metaModel, ActionResponse response);
	void checkFieldCorrectly(MetaField metaField, ActionResponse response);
	void clear(ValidatorFields validatorFields, ActionResponse response);
}
