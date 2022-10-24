package com.avr.apps.docgen.common;

import com.avr.apps.docgen.common.utils.ObjectUtils;
import com.axelor.common.StringUtils;

import java.util.Locale;

public class I18n {

	 public static String get(String message, String langCode) {
			if(message.startsWith("com.")) {
				 return getCustom(message, langCode);
			} else {
				 return getNormal(message, langCode);
			}
	 }

	 private static String getNormal(String message, String langCode) {
			if(StringUtils.isBlank(message)) return message;
			if(!ObjectUtils.isEmpty(langCode)) {
				 return com.axelor.i18n.I18n.getBundle(new Locale(langCode))
																		.getString(message);
			} else {
				 return com.axelor.i18n.I18n.getBundle()
																		.getString(message);
			}
	 }

	 private static String getCustom(String message, String langCode) {
			if(StringUtils.isBlank(message)) return message;
			if(!ObjectUtils.isEmpty(langCode)) {
				 return com.axelor.i18n.I18n.getBundle(new Locale(langCode))
																		.getString(String.format("value:%s", message));
			} else {
				 return com.axelor.i18n.I18n.getBundle()
																		.getString(String.format("value:%s", message));
			}
	 }

	 public static String get(String message) {
			return com.axelor.i18n.I18n.get(message);
	 }

}
