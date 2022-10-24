package com.avr.apps.docgen.common.utils;

import org.jsoup.Jsoup;

public final class HtmlUtils {

	public static String html2text(String html) {
		return Jsoup.parse(html).text();
	}


}
