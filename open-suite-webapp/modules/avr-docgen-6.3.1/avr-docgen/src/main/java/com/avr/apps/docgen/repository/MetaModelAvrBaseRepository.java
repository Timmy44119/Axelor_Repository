/**
 * ***********************************
 * AVR SOLUTIONS
 * ***********************************
 *
 * @author David
 * @date 18/03/2021
 * @time 14:32
 * @Update 18/03/2021
 * @version 1.0
 */
package com.avr.apps.docgen.repository;

import com.axelor.meta.db.MetaModel;
import com.axelor.meta.db.repo.MetaModelRepository;
import com.google.inject.Singleton;

@Singleton
public class MetaModelAvrBaseRepository extends MetaModelRepository {

	public MetaModel findMetaModelByPackageNameAndName(String packageName, String name) {
		return all()
				.filter("self.name = :name AND self.packageName = :packageName")
				.bind("name", name)
				.bind("packageName", packageName)
				.fetchOne();
	}

}
