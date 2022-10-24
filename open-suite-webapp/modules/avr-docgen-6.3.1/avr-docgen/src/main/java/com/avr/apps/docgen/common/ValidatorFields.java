package com.avr.apps.docgen.common;

import com.axelor.meta.db.MetaField;

public final class ValidatorFields {

	String targetField;
	MetaField metaField;
	String metaFieldNameToInsert;
	String columnName = "name";
	String isValidateColumnName = "$isValidate";
	boolean useLabel;

	public ValidatorFields(String targetField, MetaField metaField, String columnName, boolean useLabel) {
		this.targetField = targetField;
		this.metaField = metaField;
		this.columnName = columnName;
		this.useLabel = useLabel;
		this.metaFieldNameToInsert = "metaField";
	}

	public ValidatorFields(String targetField, MetaField metaField, String columnName, boolean useLabel, String isValidateColumnName) {
		this.targetField = targetField;
		this.metaField = metaField;
		this.columnName = columnName;
		this.useLabel = useLabel;
		this.isValidateColumnName = isValidateColumnName;
		this.metaFieldNameToInsert = "metaField";
	}

	public ValidatorFields(String targetField, MetaField metaField, String columnName, boolean useLabel, String isValidateColumnName, String metaFieldNameToInsert) {
		this.targetField = targetField;
		this.metaField = metaField;
		this.columnName = columnName;
		this.useLabel = useLabel;
		this.isValidateColumnName = isValidateColumnName;
		this.metaFieldNameToInsert = metaFieldNameToInsert;
	}

	public ValidatorFields(String targetField, MetaField metaField, String columnName) {
		this.targetField = targetField;
		this.metaField = metaField;
		this.columnName = columnName;
		this.useLabel = true;
		this.metaFieldNameToInsert = "metaField";
	}

	public ValidatorFields(String targetField, MetaField metaField, String columnName, String metaFieldNameToInsert) {
		this.targetField = targetField;
		this.metaField = metaField;
		this.columnName = columnName;
		this.useLabel = true;
		this.metaFieldNameToInsert = metaFieldNameToInsert;
	}

	public ValidatorFields(String targetField, MetaField metaField) {
		this.targetField = targetField;
		this.metaField = metaField;
		this.useLabel = true;
	}

	public String getIsValidateColumnName() {
		return isValidateColumnName;
	}

	public String getTargetField() {
		return targetField != null ? targetField.trim() : targetField;
	}

	public MetaField getMetaField() {
		return metaField;
	}

	public boolean isUseLabel() {
		return useLabel;
	}

	public String getColumnName() {
		return columnName != null ? columnName.trim() : columnName;
	}

	public String getMetaFieldNameToInsert() {
		return metaFieldNameToInsert;
	}
}
