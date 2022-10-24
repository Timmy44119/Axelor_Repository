/**
 * ***********************************
 * AVR SOLUTIONS
 * ***********************************
 *
 * @author David
 * @date 18/03/2021
 * @time 11:10
 * @Update 18/03/2021
 * @version 1.0
 */
package com.avr.apps.docgen.repository;

import com.axelor.db.Model;
import com.axelor.dms.db.DMSFile;
import com.axelor.dms.db.repo.DMSFileRepository;
import com.google.inject.Singleton;

import java.util.List;

@Singleton
public class DMSFileAvrBaseRepository extends DMSFileRepository {

	public <T extends Model> List<DMSFile> findDmsFileByModelAndId(Class<T> clazz, Long id) {
		return findDmsFileByModelAndId(clazz.getName(), id);
	}

	public <T extends Model> List<DMSFile> findDmsFileByModelAndId(String model, Long id) {
		return all()
				.filter("self.relatedModel = :relatedModel AND relatedId = :relatedId AND isDirectory = false")
				.bind("relatedModel", model)
				.bind("relatedId", id)
				.fetch();
	}




}
