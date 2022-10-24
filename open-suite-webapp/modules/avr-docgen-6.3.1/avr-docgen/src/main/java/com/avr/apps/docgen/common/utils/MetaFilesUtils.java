package com.avr.apps.docgen.common.utils;

import com.axelor.apps.tool.file.PdfTool;
import com.axelor.auth.db.AuditableModel;
import com.axelor.db.Model;
import com.axelor.dms.db.DMSFile;
import com.axelor.dms.db.repo.DMSFileRepository;
import com.axelor.exception.AxelorException;
import com.axelor.inject.Beans;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import com.axelor.meta.db.repo.MetaFileRepository;
import com.axelor.meta.schema.actions.ActionView;
import com.google.common.base.Preconditions;
import com.google.common.io.Files;
import org.apache.commons.io.FileUtils;
import org.apache.pdfbox.io.MemoryUsageSetting;
import org.apache.pdfbox.multipdf.PDFMergerUtility;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * The type Meta files utils.
 */
public class MetaFilesUtils {

	private final static String FILE_TYPE_DOC_X = "application/vnd.openxmlformats-officedocument.wordprocessingml.document";

	/**
	 * Upload file.
	 *
	 * @param file the file
	 * @return the meta file
	 * @throws IOException the io exception
	 */
	public static MetaFile uploadFile(File file) throws IOException {
		return Beans.get(MetaFiles.class)
		            .upload(file);
	}

	/**
	 * Show file generated.
	 *
	 * @param title the title
	 * @param file  ) the file
	 * @return the map
	 * @throws IOException the io exception
	 */
	public static Map<String, Object> showFileGenerated(
			String title, File file
	) throws IOException {

		File fileTmp;
		fileTmp = MetaFiles.createTempFile(null, "")
		                   .toFile();
		try (
				FileInputStream inputStream = new FileInputStream(file);
				FileOutputStream outputStream = new FileOutputStream(fileTmp)
		) {
			byte[] buffer = new byte[1024];
			int length;
			while ((length = inputStream.read(buffer)) > 0) {
				outputStream.write(buffer, 0, length);
			}
			return ActionView.define(title)
			                 .add("html", PdfTool.getFileLinkFromPdfFile(fileTmp, file.getName()))
			                 .map();
		}
	}

	/**
	 * Upload file and attach.
	 *
	 * @param file  the file
	 * @param model the model
	 * @throws IOException the io exception
	 */
	public static void uploadFileAndAttach(
			File file, Model model
	) throws IOException {
		MetaFile metaFile = uploadFile(file);
		Beans.get(MetaFiles.class)
		     .attach(metaFile, metaFile.getFileName(), model);
	}

	/**
	 * Gets template files ids.
	 *
	 * @param templateFolderDocx the template folder docx
	 * @return the template files ids
	 */
	public static String getTemplateFilesIds(String templateFolderDocx) {
		List<DMSFile> dmsFileFolder = Beans.get(DMSFileRepository.class)
		                                   .all()
		                                   .filter("self.parent.fileName = ? AND self.metaFile.fileType = ?", templateFolderDocx, FILE_TYPE_DOC_X)
		                                   .fetch();
		return dmsFileFolder.stream()
		                    .map(it -> it.getMetaFile()
		                                 .getId()
		                                 .toString())
		                    .collect(Collectors.joining(", "));
	}

	/**
	 * Gets template files ids.
	 *
	 * @param templateFolderDocx the template folder docx
	 * @return the template files ids
	 */
	public static String getTemplateFilesIds(TemplateFolderDocx templateFolderDocx) {
		return getTemplateFilesIds(templateFolderDocx.getValue());
	}

	/**
	 * Gets domain template.
	 *
	 * @param templateFolderDocx the template folder docx
	 * @param langCode           the lang code
	 * @return the domain template
	 */
	public static String getDomainTemplate(
			TemplateFolderDocx templateFolderDocx, String langCode
	) {
		String ids = getTemplateFilesIds(templateFolderDocx);
		return getDomainFromIds(ids, langCode);
	}

	/**
	 * Gets domain template.
	 *
	 * @param templateFolderDocx the template folder docx
	 * @return the domain template
	 */
	public static String getDomainTemplateWithoutLang(
			TemplateFolderDocx templateFolderDocx
	) {
		String ids = getTemplateFilesIds(templateFolderDocx);
		return getDomainFromIdsWithoutLang(ids);
	}

	/**
	 * Gets meta file by template folder docx.
	 *
	 * @param templateFolderDocx the template folder docx
	 * @param langCode           the lang code
	 * @return the meta file by template folder docx
	 */
	public static MetaFile getMetaFileByTemplateFolderDocx(
			TemplateFolderDocx templateFolderDocx, String langCode
	) {
		return computedMetaFileLanguage(Beans.get(MetaFileRepository.class)
		                                     .all()
		                                     .filter(MetaFilesUtils.getDomainTemplate(templateFolderDocx, langCode))
		                                     .fetch(), langCode);
	}

	private static MetaFile computedMetaFileLanguage(
			List<MetaFile> metaFileList, String langCode
	) {

		if (ObjectUtils.isEmpty(metaFileList)) return null;
		MetaFile metaFile = metaFileList.stream()
		                                .filter(it -> it.getFileName()
		                                                .startsWith(String.format("%s_", langCode)))
		                                .max(Comparator.comparing(AuditableModel::getCreatedOn))
		                                .orElse(null);
		if (ObjectUtils.notEmpty(metaFile)) return metaFile;
		return metaFileList.stream()
		                   .filter(it -> it.getFileName()
		                                   .startsWith("def_"))
		                   .max((o1, o2) -> {
			                   if (ObjectUtils.notEmpty(o1.getUpdatedOn()) && ObjectUtils.notEmpty(o2.getUpdatedOn())) {
				                   return o1.getUpdatedOn()
				                            .compareTo(o2.getUpdatedOn());
			                   }
			                   return o1.getCreatedOn()
			                            .compareTo(o2.getCreatedOn());
		                   })
		                   .orElse(null);
	}

	/**
	 * Gets domain from ids.
	 *
	 * @param ids      the ids
	 * @param langCode the lang code
	 * @return the domain from ids
	 */
	public static String getDomainFromIds(
			String ids, String langCode
	) {
		if (ObjectUtils.isEmpty(ids)) ids = "0";
		if (ObjectUtils.isEmpty(langCode)) langCode = "";
		return String.format("self.id in (%s) AND (self.fileName like 'def_%%' OR self.fileName like '%s_%%')", ids, langCode);
	}

	/**
	 * Gets domain from ids.
	 *
	 * @param ids the ids
	 * @return the domain from ids
	 */
	public static String getDomainFromIdsWithoutLang(
			String ids
	) {
		if (ObjectUtils.isEmpty(ids)) ids = "0";
		return String.format("self.id in (%s)", ids);
	}

	/**
	 * Merge files file.
	 *
	 * @param title the title
	 * @param files the files
	 * @return the file
	 * @throws IOException the io exception
	 */
	public static File mergeFiles(String title, List<File> files) throws IOException, AxelorException {
		Preconditions.checkNotNull(files, "list of file is empty");
		PDFMergerUtility merge = new PDFMergerUtility();
		File firstFile = files.get(0);
		for (File file : files) {
			merge.addSource(file);
		}
		merge.setDestinationFileName(firstFile.getAbsolutePath());
		merge.mergeDocuments(MemoryUsageSetting.setupMainMemoryOnly());
		File file = new File(Files.createTempDir().getAbsolutePath(), title);
		FileUtils.writeByteArrayToFile(file, FileUtils.readFileToByteArray(firstFile));
		return file;
	}

	/**
	 * The enum Template folder docx.
	 */
	public enum TemplateFolderDocx {
		/**
		 * The Folders template contract name.
		 */
		FOLDERS_TEMPLATE_CONTRACT_NAME("Template contracts"),

		/**
		 * The Folders template sale order name.
		 */
		FOLDERS_TEMPLATE_SALE_ORDER_NAME("Template saleorder"),

		/**
		 * The Folders template order name.
		 */
		FOLDERS_TEMPLATE_ORDER_NAME("Template order"),

		/**
		 * The Folder template logistical name.
		 */
		FOLDER_TEMPLATE_LOGISTICAL_NAME("Template logistical"),

		/**
		 * The Folder template logistical name.
		 */
		FOLDER_TEMPLATE_LOGISTICAL_WITHOUT_COMPOSITE_NAME("Template logistical without composite"),
		/**
		 * The Folders template proforma infoice name.
		 */
		FOLDERS_TEMPLATE_PROFORMA_INFOICE_NAME("Template proforma invoice"),

		/**
		 * The Folders template delivrery note name.
		 */
		FOLDERS_TEMPLATE_DELIVRERY_NOTE_NAME("Template delivrery note"),

		/**
		 * The Folders template invoice name.
		 */
		FOLDERS_TEMPLATE_INVOICE_NAME("Template invoice"),

		/**
		 * The Folders template pv name.
		 */
		FOLDERS_TEMPLATE_PV_NAME("Template PV"),

		/**
		 * The Folders template infoice name.
		 */
		FOLDERS_TEMPLATE_INFOICE_NAME("Template invoice");

		private final String value;

		TemplateFolderDocx(String value) {
			this.value = value;
		}

		/**
		 * Gets value.
		 *
		 * @return the value
		 */
		public String getValue() {
			return value;
		}
	}
}
