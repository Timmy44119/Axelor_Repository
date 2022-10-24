package com.avr.apps.docgen.csv.script;

import com.avr.apps.docgen.db.DocgenSubType;
import com.avr.apps.docgen.db.DocgenTemplate;
import com.axelor.db.Query;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.google.common.base.Strings;
import com.google.inject.Inject;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * *********************************** AVR SOLUTIONS ***********************************
 *
 * @author David
 * @version 1.0
 * @date 10/08/2021
 * @time 16:48
 * @Update 10/08/2021
 */
public class ImportTemplate {

    private static final Logger LOG = LoggerFactory.getLogger(ImportTemplate.class);

    @Inject
    private MetaFiles metaFiles;

    public Object importTemplate(Object bean, Map<String, Object> values) {
        assert bean instanceof DocgenTemplate;

        String fileName = (String) values.get("modelDefault");

        DocgenTemplate docgenTemplate = (DocgenTemplate) bean;

        if (!Strings.isNullOrEmpty(fileName)) {
            try {
                final File file = new File(System.getProperty("java.io.tmpdir") + "/" + fileName);
                copyInputStreamToFile(this.getClass().getResourceAsStream("/data-import/files/" + fileName), file);
                if (file.exists()) {
                    final MetaFile metafile = metaFiles.upload(file);
                    docgenTemplate.setModelDefault(metafile);
                }
            } catch (Exception e) {
                LOG.warn("Can't load file {} for line {}", fileName, docgenTemplate.getName());
            }
        }

        return docgenTemplate;
    }

    private static void copyInputStreamToFile(InputStream inputStream, File file) throws IOException {
        // append = false
        try (FileOutputStream outputStream = new FileOutputStream(file, false)) {
            int read;
            byte[] bytes = new byte[8192];
            while ((read = inputStream.read(bytes)) != -1) {
                outputStream.write(bytes, 0, read);
            }
        }
    }
}
