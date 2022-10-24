package com.avr.apps.docgen.service;

import com.avr.apps.docgen.common.ValidatorFields;
import com.axelor.common.ObjectUtils;
import com.axelor.inject.Beans;
import com.axelor.meta.db.MetaField;
import com.axelor.meta.db.MetaModel;
import com.axelor.meta.db.repo.MetaFieldRepository;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Singleton;

import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Singleton
public class MetaFieldOverrideSerivceImpl implements MetaFieldOverrideSerivce {

	/**
	 * Validate field system
	 * @param validatorFields
	 * @param response
	 */
	@Override
	public void selectedFields(ValidatorFields validatorFields, ActionResponse response, boolean camelCase, boolean deep) {
		String targetFields;
		if (ObjectUtils.notEmpty(validatorFields.getTargetField())) {
			String[] tf = validatorFields.getTargetField().split(Pattern.quote("."));
			String lastElement = tf[tf.length - 1];
			if (lastElement.equals(validatorFields.getMetaField().getName()) && deep) {
				return;
			}
			targetFields =
					validatorFields.getTargetField() + "." + validatorFields.getMetaField().getName();
		} else {
			targetFields = validatorFields.getMetaField().getName();
		}
		if(null != validatorFields.getMetaField().getRelationship() && deep) {
			response.setValue("metaField", null);
		} else {
			response.setValue("$isValidate", true);
			response.setReadonly("metaField", true);
			response.setReadonly("selectedMetaField", true);
		}
		response.setValue("targetField", targetFields);
//		String label = validatorFields.getMetaField().getName();
//		if (validatorFields.isUseLabel()) {
//			label = camelCase ? WordUtils.capitalizeFully(com.avr.apps.base.service.ObjectUtils.eval(() -> validatorFields.getMetaField().getLabel(), validatorFields.getMetaField().getName()), new char[]{' '})
//			                                    .replace(" ", "") : validatorFields.getMetaField().getLabel();
//			label = camelCase && !ObjectUtils.isEmpty(label) ? String.format("%s%s", label.substring(0,1).toLowerCase(), label.substring(1)) : label;
//		}
		response.setValue(validatorFields.getColumnName(), targetFields);
	}

	public void selectedFields(ValidatorFields validatorFields, ActionResponse response) {
		selectedFields(validatorFields, response, false, true);
	}

	public void selectedFields(ValidatorFields validatorFields, ActionResponse response, boolean camelCase) {
		selectedFields(validatorFields, response, camelCase, true);
	}

	public void clear(ValidatorFields validatorFields, ActionResponse response) {
		response.setValue("targetField", " ");
		response.setValue(validatorFields.getColumnName(), " ");
		response.setValue("metaField", null);
		response.setAttr("metaField", "readonly", false);
		response.setAttr("selectedMetaField", "readonly", false);
		response.setValue(validatorFields.getIsValidateColumnName(), false);
	}

	/**
	 *
	 * @param validatorFields
	 * @param metaModel
	 * @param response
	 */
	public void getDomain(ValidatorFields validatorFields, MetaModel metaModel, ActionResponse response, boolean autorise, String... limitedTypeFields) {
		String[] targetFields = null;
		if (ObjectUtils.notEmpty(validatorFields.getTargetField())) {
			targetFields = validatorFields.getTargetField().split(Pattern.quote("."));
		}
		String pkg = metaModel.getFullName();
		if (ObjectUtils.notEmpty(targetFields)) {
			for (String tf : targetFields) {
				MetaField mm =
						Beans.get(MetaFieldRepository.class)
						     .all()
						     .filter("self.metaModel.fullName = ? AND self.name = ?", pkg, tf)
						     .fetchOne();
				pkg = String.format("%s.%s", mm.getPackageName(), mm.getTypeName());
			}
		}
		String domain = String.format("self.metaModel.fullName = '%s'", pkg);
		if (!ObjectUtils.isEmpty(limitedTypeFields)) {
			domain = String.format("%s AND (self.relationship %s (%s) %s)", domain, autorise ? "IN" : "NOT IN", Arrays.stream(limitedTypeFields).map(it -> String.format("'%s'", it)).collect(Collectors.joining(",")), autorise ? "" : "OR self.relationship IS NULL");
		}
		response.setAttr(validatorFields.getMetaFieldNameToInsert(), "domain", domain);
	}

	public void getDomain(ValidatorFields validatorFields, MetaModel metaModel, ActionResponse response) {
		getDomain(validatorFields, metaModel, response, false);
	}

	public void checkFieldCorrectly(MetaField metaField, ActionResponse response) {
		if (metaField == null || null != metaField.getRelationship()) {
			response.setValue("$isValidate", false);
		} else {
			response.setValue("$isValidate", true);
			response.setAttr("selectedMetaField", "readonly", true);
			response.setAttr("metaField", "readonly", true);
		}
	}

}
