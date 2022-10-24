package com.avr.apps.docgen.service.interfaces;

import com.axelor.apps.sale.db.SaleOrder;
import java.time.LocalDate;

public interface SaleOrderAvrDocgenService {
    LocalDate computedDeadLineDate(SaleOrder saleOrder);
}
