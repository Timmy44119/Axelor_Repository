package com.avr.apps.docgen.common.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * *********************************** AVR SOLUTIONS ***********************************
 *
 * @author David
 * @version 1.0
 * @date 30 /07/2021
 * @time 13 :35
 * @Update 30 /07/2021
 */
public class RegexUtils {

    /**
     * Find multiline string [ ].
     *
     * @param regex   the regex
     * @param matcher the matcher
     * @return the string [ ]
     */
    public static String[] findMultiline(String regex, String matcher) {
        return find(regex, matcher, Pattern.MULTILINE);
    }

    /**
     * Find string [ ].
     *
     * @param regex the regex
     * @param input the input
     * @param flag  the flag
     * @return the string [ ]
     */
    public static String[] find(String regex, String input, int flag) {
        final Pattern pattern = Pattern.compile(regex, flag);
        final Matcher matcher = pattern.matcher(input);
        String[] res = new String[matcher.groupCount()];
        while (matcher.find()) {
            for (int i = 0; i < matcher.groupCount(); i++) {
                res[i] = matcher.group(i);
            }
        }
        return res;
    }

    public static List<String> find(String text, String pattern) {
        Pattern pn = Pattern.compile(pattern);
        List<String> list = new ArrayList<>();
        Matcher matcher = pn.matcher(text);
        while (matcher.find()) {
            for (int i = 1; i <= matcher.groupCount(); i++) {
                list.add(matcher.group(i));
            }
        }
        return list;
    }
}
