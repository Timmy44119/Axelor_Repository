package com.avr.apps.docgen.web;

import com.avr.apps.docgen.utils.io.FileFromJar;
import com.axelor.apps.base.service.imports.listener.ImporterListener;
import com.axelor.data.csv.CSVImporter;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * *********************************** AVR SOLUTIONS ***********************************
 *
 * @author David
 * @version 1.0
 * @date 11/08/2021
 * @time 12:21
 * @Update 11/08/2021
 */
public class AppDocgenController {

    String csv = "data-import/fr";

    /**
     *
     */
    public void importData(ActionRequest request, ActionResponse response) {
        try {
            try (FileFromJar docgenJar = new FileFromJar(this.getClass())) {
                List<File> csvLists = docgenJar.copyFiles(csv, "*.csv");
                if (csvLists.isEmpty()) throw new AxelorException(TraceBackRepository.CATEGORY_NO_VALUE, "Aucune csv trouvé");
                File xml = docgenJar.copyFile("data-import", "*.xml");
                CSVImporter importer = new CSVImporter(xml.getAbsolutePath(), csvLists.get(0).getParentFile().getAbsolutePath());
                ImporterListener listener = new ImporterListener("docgen-data-import");
                importer.addListener(listener);
                importer.run();
                response.setNotify("Les données ont été importé");
            }
        } catch (IOException | AxelorException e) {
            e.printStackTrace();
        }
    }
}
