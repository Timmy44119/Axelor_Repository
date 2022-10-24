/**
 * ***********************************
 *          AVR SOLUTIONS            *
 * ***********************************
 *
 *
 *
 * @author David
 * @date 11/03/2021
 * @time 17:09
 * @Update 11/03/2021
 * @version 1.0
 */


package com.avr.apps.docgen.utils;

public enum DocGenType {
	UNKNOWN("unknown"),
	DOC("doc"),
	DOT("dot"),
	DOCX("docx"),
	DOCM("docm"),
	DOTX("dotx"),
	DOTM("dotm"),
	FLATOPC("flatopc"),
	FLATOPCMACROENABLED("flatopcmacroenabled"),
	FLATOPCTEMPLATE("flatopctemplate"),
	FLATOPCTEMPLATEMACROENABLED("flatopctemplatemacroenabled"),
	RTF("rtf"),
	WORDML("wordml"),
	PDF("pdf"),
	XPS("xps"),
	XAMLFIXED("xamlfixed"),
	SVG("svg"),
	HTMLFIXED("htmlfixed"),
	OPENXPS("openxps"),
	PS("ps"),
	PCL("pcl"),
	HTML("html"),
	MHTML("mhtml"),
	EPUB("epub"),
	ODT("odt"),
	OTT("ott"),
	TEXT("text"),
	XAMLFLOW("xamlflow"),
	XAMLFLOWPACK("xamlflowpack"),
	MARKDOWN("markdown"),
	TIFF("tiff"),
	PNG("png"),
	BMP("bmp"),
	EMF("emf"),
	JPEG("jpeg"),
	GIF("gif");

	private final String value;

	DocGenType(String value) {
		this.value = value;
	}

	public String getValue() {
		return value;
	}
}
