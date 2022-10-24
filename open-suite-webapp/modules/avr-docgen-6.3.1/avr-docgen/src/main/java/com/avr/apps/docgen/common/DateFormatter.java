package com.avr.apps.docgen.common;

import com.axelor.common.ObjectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.Temporal;
import java.util.HashMap;

/**
 * The type Date formatter.
 */
public class DateFormatter {

    /**
     * The Instance.
     */
    static DateFormatter instance = null;
    private final java.util.Map<String, String> configLang = new HashMap<>();
    private static Logger log = LoggerFactory.getLogger(DateFormatter.class);

    private DateFormatter() {
        configLang.put("fr", "dd/MM/yyyy HH:mm:ss");
        configLang.put("fr_d", "dd/MM/yyyy");
        configLang.put("en", "yyyy-MM-dd hh:mm:ss");
        configLang.put("en_d", "yyyy-MM-dd");
    }

    /**
     * Gets instance.
     *
     * @return the instance
     */
    public static DateFormatter getInstance() {
        if (ObjectUtils.isEmpty(instance)) {
            instance = new DateFormatter();
        }
        return instance;
    }

    /**
     * Transform date string.
     *
     * @param date   the date
     * @param format the format
     * @return the string
     */
    public static String transformDate(String date, String format) {
        if (ObjectUtils.isEmpty(date)) return null;
        if (ObjectUtils.isEmpty(format)) return null;
        String[] dtString = date.split(" ");
        if (dtString.length > 1) {
            return LocalDateTime.parse(date).format(DateTimeFormatter.ofPattern(format));
        } else {
            return LocalDate.parse(date).format(DateTimeFormatter.ofPattern(format));
        }
    }

    /**
     * Transform date string.
     *
     * @param date     the date
     * @param langCode the lang code
     * @return the string
     */
    public static String transformDate(LocalDate date, String langCode) {
        return transformDateCommon("%s_d", date, langCode);
    }

    /**
     * Transform date string.
     *
     * @param date     the date
     * @param langCode the lang code
     * @return the string
     */
    public static String transformDate(LocalDateTime date, String langCode) {
        return transformDateCommon("%s", date, langCode);
    }

    private static String getLang(String format, String langCode) {
        return getInstance().configLang.get(getCode(format, langCode));
    }

    private static String getCode(String format, String langCode) {
        return String.format(format, langCode.toLowerCase());
    }

    private static String transformDateCommon(String format, Temporal date, String langCode) {
        if (ObjectUtils.isEmpty(date)) return null;
        if (ObjectUtils.isEmpty(langCode)) langCode = "fr";
        if (!getInstance().configLang.containsKey(getCode(format, langCode))) langCode = "fr";
        log.debug("langCode {}", langCode);
        log.debug("format {}", format);
        log.debug("format searched {}", getLang(format, langCode));
        return DateTimeFormatter.ofPattern(getLang(format, langCode)).format(date);
    }
}
