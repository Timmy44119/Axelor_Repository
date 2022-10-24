package com.avr.apps.docgen.service;

import static java.util.stream.Collectors.toList;

import com.avr.apps.docgen.common.utils.MetaFilesUtils;
import com.avr.apps.docgen.db.*;
import com.avr.apps.docgen.db.repo.*;
import com.avr.apps.docgen.service.interfaces.DocgenTraitmentDataService;
import com.axelor.apps.base.db.Language;
import com.axelor.apps.base.db.repo.LanguageRepository;
import com.axelor.db.JPA;
import com.axelor.db.Query;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.exception.service.TraceBackService;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.axelor.meta.db.repo.MetaFieldRepository;
import com.axelor.meta.db.repo.MetaModelRepository;
import com.google.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.lingala.zip4j.ZipFile;
import net.lingala.zip4j.exception.ZipException;

public class DocgenTraitmentDataServiceImpl implements DocgenTraitmentDataService {

    public static final String DOCGEN_BINDING_NAME = "DocgenBinding";
    public static final String DOCGEN_CHILD_BINDING_NAME = "DocgenChildBinding";
    public static final String DOCGEN_TEMPLATE_NAME = "DocgenTemplate";
    public static final String DOCGEN_MODEL_NAME = "DocgenModel";
    public final List<String> listOfFileOrdered = Arrays.asList(
        DOCGEN_TEMPLATE_NAME,
        DOCGEN_MODEL_NAME,
        DOCGEN_CHILD_BINDING_NAME,
        DOCGEN_BINDING_NAME
    );
    private final DocgenSubTypeRepository docgenSubTypeRepository;
    private final DocgenTemplateRepository docgenTemplateRepository;
    private final DocgenChildBindingRepository docgenChildBindingRepository;
    private final DocgenBindingRepository docgenBindingRepository;
    private final DocgenModelRepository docgenModelRepository;
    private final MetaModelRepository metaModelRepository;
    private final MetaFieldRepository metaFieldRepository;
    private final LanguageRepository languageRepository;

    @Inject
    public DocgenTraitmentDataServiceImpl(
        DocgenSubTypeRepository docgenSubTypeRepository,
        DocgenTemplateRepository docgenTemplateRepository,
        DocgenChildBindingRepository docgenChildBindingRepository,
        DocgenBindingRepository docgenBindingRepository,
        DocgenModelRepository docgenModelRepository,
        MetaModelRepository metaModelRepository,
        MetaFieldRepository metaFieldRepository,
        LanguageRepository languageRepository
    ) {
        this.docgenSubTypeRepository = docgenSubTypeRepository;
        this.docgenTemplateRepository = docgenTemplateRepository;
        this.docgenChildBindingRepository = docgenChildBindingRepository;
        this.docgenBindingRepository = docgenBindingRepository;
        this.docgenModelRepository = docgenModelRepository;
        this.metaModelRepository = metaModelRepository;
        this.metaFieldRepository = metaFieldRepository;
        this.languageRepository = languageRepository;
    }

    private void createImportIdIfNotExist() {
        String formatDateNow = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        JPA.runInTransaction(
            () -> {
                updateImportIdIfNotNull("docgen_docgen_template", formatDateNow);
                updateImportIdIfNotNull("docgen_docgen_child_binding", formatDateNow);
                updateImportIdIfNotNull("docgen_docgen_binding", formatDateNow);
                updateImportIdIfNotNull("docgen_docgen_model", formatDateNow);
            }
        );
    }

    private void updateImportIdIfNotNull(String entity, String formatDateNow) {
        JPA
            .em()
            .createNativeQuery(
                String.format("UPDATE %s Set import_id = CONCAT(:formatNow, '-', id) Where import_id IS NULL", entity)
            )
            .setParameter("formatNow", formatDateNow)
            .executeUpdate();
    }

    @Override
    public MetaFile exportFileToZipFile(List<Long> ids) throws IOException {
        createImportIdIfNotExist();
        List<File> files = exportAllDataDocgen(ids);
        ZipFile zipFile = new ZipFile(
            String.format(
                "%s/export-docgen-%s.zip",
                System.getProperty("java.io.tmpdir"),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
            )
        );
        zipFile.addFiles(files);
        return MetaFilesUtils.uploadFile(zipFile.getFile());
    }

    private List<File> exportAllDataDocgen(List<Long> ids) throws IOException {
        List<DocgenTemplate> docgenTemplateList = docgenTemplateRepository
            .all()
            .filter("self.id IN (:ids)")
            .bind("ids", ids)
            .fetch();

        List<DocgenModel> docgenModelList = docgenModelRepository
            .all()
            .filter("self.docgenTemplate.id IN (:ids)")
            .bind("ids", ids)
            .fetch();

        List<DocgenChildBinding> docgenChildBindingList = docgenChildBindingRepository
            .all()
            .filter("self.docgenTemplate.id IN (:ids)")
            .bind("ids", ids)
            .fetch();

        List<DocgenBinding> docgenBindingList = docgenBindingRepository
            .all()
            .filter("self.docgenTemplate.id IN (:ids) OR self.docgenChildBinding.docgenTemplate.id IN (:ids)")
            .bind("ids", ids)
            .fetch();

        List<File> files = Stream
            .of(
                docgenTemplateExportCsv(docgenTemplateList),
                docgenModelExportCsv(docgenModelList),
                docgenChildBindingExportCsv(docgenChildBindingList),
                docgenBindingExportCsv(docgenBindingList)
            )
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
        List<File> fileFromModel = getTemplateFileFromGed(docgenTemplateList);
        files.addAll(fileFromModel);
        return files;
    }

    private File docgenTemplateExportCsv(List<DocgenTemplate> docgenTemplateList) throws IOException {
        return new ExportCsv<DocgenTemplate>(docgenTemplateList) {
            @Override
            public void dataToExport(DocgenTemplate docgenTemplate) {
                bind("defaultModel", docgenTemplate.getModelDefault().getFilePath());
                bind("defaultModelName", docgenTemplate.getModelDefault().getFileName());
                bind("patternTitle", docgenTemplate.getPatternTitle());
                bind("importId", docgenTemplate.getImportId());
                bind("name", docgenTemplate.getName());
                bind("type", docgenTemplate.getDocgenSubType().getName());
                bind("metaModelFullName", docgenTemplate.getMetaModel().getFullName());
            }
        }
            .writeToCsv(DOCGEN_TEMPLATE_NAME);
    }

    private File docgenModelExportCsv(List<DocgenModel> docgenTemplateList) throws IOException {
        return new ExportCsv<DocgenModel>(docgenTemplateList) {
            @Override
            public void dataToExport(DocgenModel docgenModel) {
                bind("metaFile", docgenModel.getMetaFile().getFilePath());
                bind("importId", docgenModel.getImportId());
                bind("langueCode", docgenModel.getLanguage().getCode());
                bind("docgenTemplateImportId", docgenModel.getDocgenTemplate().getImportId());
            }
        }
            .writeToCsv(DOCGEN_MODEL_NAME);
    }

    private File docgenChildBindingExportCsv(List<DocgenChildBinding> docgenChildBindingsList) throws IOException {
        return new ExportCsv<DocgenChildBinding>(docgenChildBindingsList) {
            @Override
            public void dataToExport(DocgenChildBinding docgenChildBinding) {
                bind("targetField", docgenChildBinding.getTargetField());
                bind("importId", docgenChildBinding.getImportId());
                bind("keyBinding", docgenChildBinding.getKeyBinding());
                bind("typeTemplate", docgenChildBinding.getTypeTemplate());
                bind("isAscending", docgenChildBinding.getIsAscending());
                bind("isOrderData", docgenChildBinding.getIsOrderData());
                String metaFielOrder = "";
                if (docgenChildBinding.getMetaField() != null) {
                    metaFielOrder =
                        String.format(
                            "%s-%s",
                            docgenChildBinding.getMetaField().getMetaModel().getFullName(),
                            docgenChildBinding.getMetaField().getName()
                        );
                }
                bind("metaFieldOrder", metaFielOrder);
                bind("docgenTemplateImportId", docgenChildBinding.getDocgenTemplate().getImportId());
                bind("metaModelFullName", docgenChildBinding.getMetaModel().getFullName());
                bind(
                    "metaFieldElement",
                    String.format(
                        "%s-%s",
                        docgenChildBinding.getMetaField().getMetaModel().getFullName(),
                        docgenChildBinding.getMetaField().getName()
                    )
                );
            }
        }
            .writeToCsv(DOCGEN_CHILD_BINDING_NAME);
    }

    public File docgenBindingExportCsv(List<DocgenBinding> docgenBindingList) throws IOException {
        return new ExportCsv<DocgenBinding>(docgenBindingList) {
            @Override
            public void dataToExport(DocgenBinding docgenBinding) {
                bind("typeTemplate", docgenBinding.getTypeTemplate().getValue());
                bind("targetField", docgenBinding.getTargetField());
                bind("importId", docgenBinding.getImportId());
                bind("hasDateOnlyReturning", docgenBinding.getHasDateOnlyReturning().toString());
                bind("isJoiningResulting", docgenBinding.getIsJoiningResult());
                bind("typeData", docgenBinding.getTypeData().getValue());
                bind("query", "\"" + docgenBinding.getQuery() + "\"");
                bind("keyBinding", docgenBinding.getKeyBinding());
                bind("bigDecimalScale", docgenBinding.getBigDecimalScale());
                bind(
                    "docgenTemplateImportId",
                    Optional.of(docgenBinding).map(DocgenBinding::getDocgenTemplate).map(DocgenTemplate::getImportId).orElse(null)
                );
                bind(
                    "docgenChildBindingImportId",
                    Optional
                        .of(docgenBinding)
                        .map(DocgenBinding::getDocgenChildBinding)
                        .map(DocgenChildBinding::getImportId)
                        .orElse(null)
                );
                String metaFieldElement = null;
                if (docgenBinding.getMetaField() != null) metaFieldElement =
                    String.format(
                        "%s-%s",
                        docgenBinding.getMetaField().getMetaModel().getFullName(),
                        docgenBinding.getMetaField().getName()
                    );
                bind("metaFieldElement", metaFieldElement);
            }
        }
            .writeToCsv(DOCGEN_BINDING_NAME);
    }

    private List<File> getTemplateFileFromGed(List<DocgenTemplate> docgenTemplateList) {
        List<MetaFile> metaFileList = docgenTemplateList.stream().map(DocgenTemplate::getModelDefault).collect(toList());
        metaFileList.addAll(
            docgenTemplateList
                .stream()
                .flatMap(it -> it.getModelTemplateList().stream())
                .map(DocgenModel::getMetaFile)
                .collect(toList())
        );

        return metaFileList.stream().map(it -> MetaFiles.getPath(it).toFile()).collect(toList());
    }

    public int importData(List<File> files) {
        List<File> filesOrdered = orderFile(files);
        List<File> templateFiles = getTemplateFiles(files);
        int countError = 0;
        for (File file : filesOrdered) {
            try {
                importData(file, templateFiles);
            } catch (Exception e) {
                countError++;
                TraceBackService.trace(e);
            }
        }
        return countError;
    }

    @Override
    public File[] extractZip(MetaFile metaFileImportZip) throws ZipException {
        String pathExtract = String.format("%s/docgen-extract", System.getProperty("java.io.tmpdir"));
        File file = new File(pathExtract);
        File[] filesContentIntoFolderExtract = file.listFiles();

        if (file.exists() && filesContentIntoFolderExtract != null && filesContentIntoFolderExtract.length > 0) {
            Stream.of(filesContentIntoFolderExtract).forEach(File::delete);
        }

        ZipFile zipFile = new ZipFile(MetaFiles.getPath(metaFileImportZip).toFile());
        zipFile.extractAll(pathExtract);
        return file.listFiles();
    }

    @Override
    public List<Long> getAllIdsDocgenTemplate() {
        return docgenTemplateRepository.all().fetch().stream().map(it -> it.getId()).collect(toList());
    }

    private List<File> orderFile(List<File> files) {
        List<File> newFilesList = new ArrayList<>();
        for (String file : listOfFileOrdered) {
            Optional<File> first = files.stream().filter(it -> it.getName().equals(file + ".csv")).findFirst();
            if (!first.isPresent()) continue;
            newFilesList.add(first.get());
        }
        return newFilesList;
    }

    private List<File> getTemplateFiles(List<File> files) {
        return files.stream().filter(it -> it.getName().endsWith(".docx")).collect(toList());
    }

    private void importData(File file, List<File> templateFiles) throws Exception {
        String nameFileWithoutExtension = file.getName().replace(".csv", "");
        if (nameFileWithoutExtension.equals(DOCGEN_BINDING_NAME)) {
            docgenBindingImport(file);
        } else if (nameFileWithoutExtension.equals(DOCGEN_CHILD_BINDING_NAME)) {
            docgenChildBindingImport(file);
        } else if (nameFileWithoutExtension.equals(DOCGEN_TEMPLATE_NAME)) {
            docgenTemplateImport(file, templateFiles);
        } else if (nameFileWithoutExtension.equals(DOCGEN_MODEL_NAME)) {
            docgenModelImport(file, templateFiles);
        } else throw new AxelorException(TraceBackRepository.CATEGORY_CONFIGURATION_ERROR, "Fichier %s inconnu", file.getName());
    }

    private void docgenTemplateImport(File csv, List<File> templateFiles) throws Exception {
        new ImportCsv<DocgenTemplate>(csv, DocgenTemplate.class) {
            @Override
            public void dataToImport(ImportMap context, DocgenTemplate docgenTemplate) throws IOException, AxelorException {
                Optional<File> defaultModelFile = templateFiles
                    .stream()
                    .filter(it -> it.getName().equals(context.getToString("defaultModel")))
                    .findFirst();
                if (!defaultModelFile.isPresent()) throw new AxelorException(
                    TraceBackRepository.CATEGORY_NO_VALUE,
                    "fichier template %s non trouvé",
                    context.getToString("defaultModel")
                );
                MetaFile metaFile = MetaFilesUtils.uploadFile(defaultModelFile.get());
                metaFile.setFileName(context.getToString("defaultModelName"));
                docgenTemplate.setModelDefault(metaFile);
                docgenTemplate.setPatternTitle(context.getToString("patternTitle"));
                docgenTemplate.setImportId(context.getToString("importId"));
                docgenTemplate.setName(context.getToString("name"));
                docgenTemplate.setDocgenSubType(docgenSubTypeRepository.findByName(context.getToString("type")));
                docgenTemplate.setMetaModel(
                    metaModelRepository
                        .all()
                        .filter("self.fullName = :fullName")
                        .bind("fullName", context.getToString("metaModelFullName"))
                        .fetchOne()
                );
            }

            @Override
            public DocgenTemplate findBeanBy(Query<DocgenTemplate> query, ImportMap line) {
                return query.filter("self.importId = :importId").bind("importId", line.getToString("importId")).fetchOne();
            }
        }
            .csvToDataSave();
    }

    private void docgenModelImport(File csv, List<File> templateFiles) throws Exception {
        new ImportCsv<DocgenModel>(csv, DocgenModel.class) {
            @Override
            protected void dataToImport(ImportMap context, DocgenModel docgenModel) throws AxelorException, IOException {
                Optional<File> defaultModelFile = templateFiles
                    .stream()
                    .filter(it -> it.getName().equals(context.getToString("metaFile")))
                    .findFirst();
                if (!defaultModelFile.isPresent()) throw new AxelorException(
                    TraceBackRepository.CATEGORY_NO_VALUE,
                    "fichier template %s non trouvé",
                    context.getToString("metaFile")
                );
                MetaFile metaFile = MetaFilesUtils.uploadFile(defaultModelFile.get());
                docgenModel.setMetaFile(metaFile);
                docgenModel.setImportId(context.getToString("importId"));
                docgenModel.setDocgenTemplate(
                    docgenTemplateRepository
                        .all()
                        .filter("self.importId = :importId")
                        .bind("importId", context.getIfNotEmpty("docgenTemplateImportId"))
                        .fetchOne()
                );
                Language langueCode = languageRepository.findByCode(context.getToString("langueCode"));
                docgenModel.setLanguage(langueCode);
            }

            @Override
            protected DocgenModel findBeanBy(Query<DocgenModel> query, ImportMap context) {
                return query.filter("self.importId = :importId").bind("importId", context.getToString("importId")).fetchOne();
            }
        }
            .csvToDataSave();
    }

    private void docgenChildBindingImport(File csv) throws Exception {
        new ImportCsv<DocgenChildBinding>(csv, DocgenChildBinding.class) {
            @Override
            public void dataToImport(ImportMap context, DocgenChildBinding docgenChildBinding) {
                docgenChildBinding.setTargetField(context.getToString("targetField"));
                docgenChildBinding.setImportId(context.getToString("importId"));
                docgenChildBinding.setKeyBinding(context.getToString("keyBinding"));
                docgenChildBinding.setDocgenTemplate(
                    docgenTemplateRepository
                        .all()
                        .filter("self.importId = :importId")
                        .bind("importId", context.getIfNotEmpty("docgenTemplateImportId"))
                        .fetchOne()
                );
                docgenChildBinding.setIsOrderData(Boolean.valueOf(context.getToString("isOrderData")));
                docgenChildBinding.setIsAscending(Boolean.valueOf(context.getToString("isAscending")));
                docgenChildBinding.setTypeTemplate(TypeTemplate.valueOf(context.getToString("typeTemplate")));
                docgenChildBinding.setMetaModel(
                    metaModelRepository
                        .all()
                        .filter("self.fullName = :fullName")
                        .bind("fullName", context.getToString("metaModelFullName"))
                        .fetchOne()
                );
                String metaFieldElement = context.getToString("metaFieldElement");
                String[] metaFieldElementSplit = metaFieldElement.split("-");
                if (metaFieldElementSplit.length == 2) docgenChildBinding.setMetaField(
                    metaFieldRepository
                        .all()
                        .filter("self.metaModel.fullName = :fullName AND self.name = :name")
                        .bind("fullName", metaFieldElementSplit[0])
                        .bind("name", metaFieldElementSplit[1])
                        .fetchOne()
                );
                String metaFieldOrder = context.getToString("metaFieldOrder");
                String[] metaFieldOrderSplit = metaFieldElement.split("-");
                if (metaFieldOrderSplit.length == 2) docgenChildBinding.setMetaFieldOrder(
                    metaFieldRepository
                        .all()
                        .filter("self.metaModel.fullName = :fullName AND self.name = :name")
                        .bind("fullName", metaFieldOrderSplit[0])
                        .bind("name", metaFieldOrderSplit[1])
                        .fetchOne()
                );
            }

            @Override
            public DocgenChildBinding findBeanBy(Query<DocgenChildBinding> query, ImportMap context) {
                return query.filter("self.importId = :importId").bind("importId", context.getToString("importId")).fetchOne();
            }
        }
            .csvToDataSave();
    }

    private void docgenBindingImport(File csv) throws Exception {
        new ImportCsv<DocgenBinding>(csv, DocgenBinding.class) {
            @Override
            public void dataToImport(ImportMap context, DocgenBinding docgenBinding) {
                docgenBinding.setTypeTemplate(TypeTemplate.valueOf(context.getToString("typeTemplate")));
                docgenBinding.setTargetField(context.getToString("targetField"));
                docgenBinding.setImportId(context.getToString("importId"));
                docgenBinding.setHasDateOnlyReturning(Boolean.valueOf(context.getToString("hasDateOnlyReturning")));
                docgenBinding.setIsJoiningResult(Boolean.valueOf(context.getToString("isJoiningResulting")));
                docgenBinding.setTypeData(TypeData.valueOf(context.getToString("typeData")));
                docgenBinding.setQuery(context.getToString("query"));
                docgenBinding.setKeyBinding(context.getToString("keyBinding"));
                docgenBinding.setDocgenTemplate(
                    findOneByImportId(docgenTemplateRepository, context.getIfNotEmpty("docgenTemplateImportId"))
                );
                docgenBinding.setDocgenChildBinding(
                    findOneByImportId(docgenChildBindingRepository, context.getIfNotEmpty("docgenChildBindingImportId"))
                );
                String metaFieldElement = context.getToString("metaFieldElement");
                String[] metaFieldElementSplit = metaFieldElement.split("-");
                if (metaFieldElementSplit.length == 2) docgenBinding.setMetaField(
                    metaFieldRepository
                        .all()
                        .filter("self.metaModel.fullName = :fullName AND self.name = :name")
                        .bind("fullName", metaFieldElementSplit[0])
                        .bind("name", metaFieldElementSplit[1])
                        .fetchOne()
                );
            }

            @Override
            public DocgenBinding findBeanBy(Query<DocgenBinding> query, ImportMap line) {
                return query.filter("self.importId = :importId").bind("importId", line.getToString("importId")).fetchOne();
            }
        }
            .csvToDataSave();
    }
}
