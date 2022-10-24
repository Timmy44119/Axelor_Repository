package com.avr.apps.docgen.service.interfaces;

import com.axelor.meta.db.MetaFile;
import net.lingala.zip4j.exception.ZipException;

import java.io.File;
import java.io.IOException;
import java.util.List;

public interface DocgenTraitmentDataService {

    MetaFile exportFileToZipFile(List<Long> ids) throws IOException;

    int importData(List<File> files);

    File[] extractZip(MetaFile metaFileImportZip) throws ZipException;

    List<Long> getAllIdsDocgenTemplate();
}
