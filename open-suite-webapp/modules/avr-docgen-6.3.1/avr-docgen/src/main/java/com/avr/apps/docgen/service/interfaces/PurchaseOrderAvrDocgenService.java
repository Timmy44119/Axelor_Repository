package com.avr.apps.docgen.service.interfaces;

import com.axelor.apps.purchase.db.PurchaseOrder;
import java.time.LocalDate;

public interface PurchaseOrderAvrDocgenService {
    LocalDate computedDeadLineDate(PurchaseOrder purchaseOrder);
}
