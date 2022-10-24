package com.avr.apps.docgen.service;

import com.avr.apps.docgen.service.interfaces.SaleOrderAvrDocgenService;
import com.axelor.apps.account.db.PaymentCondition;
import com.axelor.apps.account.db.repo.PaymentConditionRepository;
import com.axelor.apps.sale.db.SaleOrder;
import com.axelor.common.ObjectUtils;
import com.google.inject.Singleton;
import java.time.LocalDate;

@Singleton
public class SaleOrderAvrDocgenServiceImpl implements SaleOrderAvrDocgenService {

    @Override
    public LocalDate computedDeadLineDate(SaleOrder saleOrder) {
        PaymentCondition paymentCondition = saleOrder.getPaymentCondition();
        if (ObjectUtils.isEmpty(paymentCondition)) return null;
        if (ObjectUtils.isEmpty(saleOrder.getDeliveryDate())) return null;
        if (paymentCondition.getPaymentTime() == 0) return saleOrder.getDeliveryDate();
        if (paymentCondition.getPeriodTypeSelect().equals(PaymentConditionRepository.PERIOD_TYPE_DAYS)) {
            return saleOrder.getDeliveryDate().plusDays(saleOrder.getPaymentCondition().getPaymentTime());
        } else if (paymentCondition.getPeriodTypeSelect().equals(PaymentConditionRepository.PERIOD_TYPE_MONTH)) {
            return saleOrder.getDeliveryDate().plusMonths(saleOrder.getPaymentCondition().getPaymentTime());
        } else {
            return saleOrder.getDeliveryDate();
        }
    }
}
