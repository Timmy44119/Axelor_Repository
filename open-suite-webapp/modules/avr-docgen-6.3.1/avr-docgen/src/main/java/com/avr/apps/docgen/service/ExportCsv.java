package com.avr.apps.docgen.service;

import com.axelor.db.Model;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public abstract class ExportCsv<T extends Model> {

    private final List<LinkedHashMap<String, Object>> recordBinding = new ArrayList<>();
    private final List<T> beanList;
    private LinkedHashMap<String, Object> recordMap;
    private final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public ExportCsv(List<T> beanList) {
        this.beanList = beanList;
        logger.info("{} data to import", beanList.size());
    }

    public abstract void dataToExport(T t);

    public void bind(String key, Object value) {
        if(value == null) value = "";
        recordMap.put(key, value);
    }

    private void newRecordMap() {
        recordMap = new LinkedHashMap<>();
    }

    public File writeToCsv(String title) throws IOException {
        for (T bean : beanList) {
            newRecordMap();
            dataToExport(bean);
            recordBinding.add(recordMap);
        }

        StringBuilder stringBuilder = new StringBuilder();
        if (!recordBinding.isEmpty()) {
            Set<String> headers = recordBinding.get(0).keySet();
            stringBuilder.append(String.join(";", headers));

            for (LinkedHashMap<String, Object> element : recordBinding) {
                stringBuilder.append("\n").append(element.values().stream().map(Object::toString).collect(Collectors.joining(";")));
            }
        }

        return writeFile(stringBuilder.toString(),title);
    }

    private File writeFile(String csv, String title) throws IOException {
        File file = new File(System.getProperty("java.io.tmpdir"), title + ".csv");
        FileUtils.write(file, csv, StandardCharsets.UTF_8);
        return file;
    }
}
