package com.avr.apps.docgen.service;

import com.axelor.db.JPA;
import com.axelor.db.JpaRepository;
import com.axelor.db.Model;
import com.axelor.db.Query;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import java.io.*;
import java.lang.invoke.MethodHandles;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ImportCsv<T extends Model> {

    public static final String SEPARATOR_CSV = ";";
    private final BufferedReader data;
    private final long length;
    private LinkedHashMap<String, Object> recordMap;
    private final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private final Class<T> clazz;
    private final Map<Integer, String> lineMessageError = new HashMap<>();

    public ImportCsv(String csv, Class<T> clazz) {
        this.data = new BufferedReader(new InputStreamReader((IOUtils.toInputStream(csv, StandardCharsets.UTF_8))));
        this.clazz = clazz;
        this.length = csv.length();
    }

    public ImportCsv(File file, Class<T> clazz) throws FileNotFoundException {
        this.data = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
        this.clazz = clazz;
        this.length = file.length();
    }

    protected abstract void dataToImport(ImportMap context, T t) throws AxelorException, IOException;

    protected abstract T findBeanBy(Query<T> query, ImportMap context);

    protected void checkCondition(boolean condition, String message, Object... args) throws AxelorException {
        if (condition) {
            throw new AxelorException(TraceBackRepository.CATEGORY_NO_VALUE, message, args);
        }
    }

    protected <U extends Model> U findOneBy(JpaRepository<U> repo, String filter, Map<String, Object> params) {
        return repo.all().filter(filter).bind(params).fetchOne();
    }

    protected <U extends Model> U findOneByImportId(JpaRepository<U> repo, Object value) {
        return repo.all().filter("self.importId = :importId").bind("importId", value).fetchOne();
    }

    public void csvToDataSave() throws Exception {
        if (length == 0) return;
        String[] column;
        column = data.readLine().split(SEPARATOR_CSV);
        JpaRepository<T> repo = JpaRepository.of(clazz);
        int nbLine = 1;
        String line;
        while ((line = data.readLine()) != null) {
            try {
                String[] dataByColumn = line.split(SEPARATOR_CSV);
                ImportMap context = new ImportMapImpl(
                    IntStream
                        .range(0, column.length)
                        .boxed()
                        .collect(Collectors.toMap(i -> column[i], i -> {
                            String result = (i > dataByColumn.length - 1 || dataByColumn[i].equals("\"\"")) ? "" : dataByColumn[i];
                            if(result.startsWith("\"")) result = result.substring(1);
                            if(result.endsWith("\"")) result = result.substring(0, result.length() - 1);
                            return result;
                        }))
                );
                T bean = findBeanBy(repo.all(), context);
                if (bean == null) bean = clazz.getConstructor().newInstance();
                dataToImport(context, bean);
                T finalBean = bean;
                JPA.runInTransaction(() -> repo.save(finalBean));
            } catch (Exception e) {
                lineMessageError.put(nbLine, e.getMessage());
            } finally {
                nbLine++;
            }
        }

        if (!lineMessageError.isEmpty()) throw new AxelorException(
            TraceBackRepository.CATEGORY_CONFIGURATION_ERROR,
            "Errors : \n" +
            lineMessageError
                .entrySet()
                .stream()
                .map(entry -> String.format("Line %s : %s", entry.getKey(), entry.getValue()))
                .collect(Collectors.joining("\n"))
        );
    }

    protected interface ImportMap extends Map<String, Object> {
        <U> U getByClass(Object key);

        String getToString(Object key);

        <U> U getIfNotEmpty(Object key);
    }

    protected static class ImportMapImpl extends HashMap<String, Object> implements ImportMap {

        public ImportMapImpl(Map<String, Object> values) {
            putAll(values);
        }

        @Override
        public Object get(Object key) {
            return super.get(key);
        }

        @Override
        @SuppressWarnings("unchecked")
        public <U> U getByClass(Object key) {
            return (U) super.get(key);
        }

        @Override
        @SuppressWarnings("unchecked")
        public String getToString(Object key) {
            return String.valueOf(super.get(key));
        }

        @Override
        @SuppressWarnings("unchecked")
        public <U> U getIfNotEmpty(Object key) {
            U obj = (U) super.get(key);
            if (obj == null || String.valueOf(obj).equals("")) return null;
            return obj;
        }
    }
}
