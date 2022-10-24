package com.avr.apps.docgen.utils.io;

import com.google.common.io.Files;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.apache.commons.io.FileUtils;

/**
 * *********************************** AVR SOLUTIONS ***********************************
 *
 * @author David
 * @version 1.0
 * @date 01/10/2021
 * @time 14:10
 * @Update 01/10/2021
 */
public class FileFromJar implements Closeable {

    private final Class<?> clazz;
    private final File tmpDir;
    List<File> lists = new ArrayList<>();

    public FileFromJar(Class<?> clazz) {
        this.clazz = clazz;
        this.tmpDir = Files.createTempDir();
    }

    public List<File> copyFiles(String pathFromJar, String glob) throws IOException {
        List<File> lists = new ArrayList<>();
        for (String pathFile : getElementsFromJar(pathFromJar, glob)) {
            File file = new File(tmpDir + File.separator + pathFile);
            if (!file.getParentFile().exists()) file.getParentFile().mkdirs();
            InputStream resourceAsStream = clazz.getResourceAsStream("/" + pathFile);
            if (resourceAsStream == null) continue;
            java.nio.file.Files.copy(resourceAsStream, file.getAbsoluteFile().toPath());
            lists.add(file);
        }
        this.lists.addAll(lists);
        return lists;
    }

    public File copyFile(String pathFromJar, String glob) throws IOException {
        List<File> lists = copyFiles(pathFromJar, glob);
        if (lists.isEmpty()) return null;
        this.lists.add(lists.get(0));
        return lists.get(0);
    }

    private String convertGlobToRegEx(String line) {
        line = line.trim();
        int strLen = line.length();
        StringBuilder sb = new StringBuilder(strLen);

        if (line.startsWith("*")) {
            line = line.substring(1);
            strLen--;
        }
        if (line.endsWith("*")) {
            line = line.substring(0, strLen - 1);
            strLen--;
        }
        boolean escaping = false;
        int inCurlies = 0;
        for (char currentChar : line.toCharArray()) {
            switch (currentChar) {
                case '*':
                    if (escaping) sb.append("\\*"); else sb.append(".*");
                    escaping = false;
                    break;
                case '?':
                    if (escaping) sb.append("\\?"); else sb.append('.');
                    escaping = false;
                    break;
                case '.':
                case '(':
                case ')':
                case '+':
                case '|':
                case '^':
                case '$':
                case '@':
                case '%':
                    sb.append('\\');
                    sb.append(currentChar);
                    escaping = false;
                    break;
                case '\\':
                    if (escaping) {
                        sb.append("\\\\");
                        escaping = false;
                    } else escaping = true;
                    break;
                case '{':
                    if (escaping) {
                        sb.append("\\{");
                    } else {
                        sb.append('(');
                        inCurlies++;
                    }
                    escaping = false;
                    break;
                case '}':
                    if (inCurlies > 0 && !escaping) {
                        sb.append(')');
                        inCurlies--;
                    } else if (escaping) sb.append("\\}"); else sb.append("}");
                    escaping = false;
                    break;
                case ',':
                    if (inCurlies > 0 && !escaping) {
                        sb.append('|');
                    } else if (escaping) sb.append("\\,"); else sb.append(",");
                    break;
                default:
                    escaping = false;
                    sb.append(currentChar);
            }
        }
        return sb.toString();
    }

    private Set<String> getElementsFromJar(String path, String glob) throws IOException {
        Pattern pattern = Pattern.compile(convertGlobToRegEx(glob));
        Set<String> lists = new HashSet<>();
        CodeSource src = clazz.getProtectionDomain().getCodeSource();
        if (src != null) {
            URL jar = src.getLocation();
            ZipInputStream zip = new ZipInputStream(jar.openStream());
            while (true) {
                ZipEntry e = zip.getNextEntry();
                if (e == null) break;
                if (!e.getName().startsWith(path)) continue;
                if (!pattern.matcher(e.getName()).find()) continue;
                lists.add(e.getName());
            }
        }
        return lists;
    }

    @Override
    public void close() throws IOException {
        FileUtils.forceDelete(tmpDir);
    }
}
