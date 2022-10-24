/**
 * *********************************** AVR SOLUTIONS ***********************************
 *
 * @author David
 * @date 01/06/2021
 * @time 18:18
 * @Update 01/06/2021
 * @version 1.0
 */
package com.avr.apps.docgen.service;

import com.avr.apps.docgen.service.interfaces.AppDocgenService;
import com.axelor.apps.base.db.AppDocgen;
import com.axelor.apps.base.db.repo.AppDocgenRepository;
import com.axelor.apps.base.service.app.AppBaseServiceImpl;
import com.google.inject.Inject;

public class AppDocgenServiceImpl extends AppBaseServiceImpl implements AppDocgenService {

    @Inject
    protected AppDocgenRepository appAccountRepo;

    @Override
    public AppDocgen getAppDocgen() {
        return appAccountRepo.all().fetchOne();
    }
}
