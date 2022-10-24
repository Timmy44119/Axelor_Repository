package com.avr.apps.docgen.service;

import com.avr.apps.docgen.service.interfaces.PurchaseOrderAvrDocgenService;
import com.axelor.apps.account.db.PaymentCondition;
import com.axelor.apps.account.db.repo.PaymentConditionRepository;
import com.axelor.apps.purchase.db.PurchaseOrder;
import com.axelor.common.ObjectUtils;
import com.google.inject.Singleton;
import java.time.LocalDate;

@Singleton
public class PurchaseOrderAvrDocgenServiceImpl implements PurchaseOrderAvrDocgenService {

    @Override
    public LocalDate computedDeadLineDate(PurchaseOrder purchaseOrder) {
        PaymentCondition paymentCondition = purchaseOrder.getPaymentCondition();
        if (ObjectUtils.isEmpty(paymentCondition)) return null;
        if (ObjectUtils.isEmpty(purchaseOrder.getDeliveryDate())) return null;
        if (paymentCondition.getPaymentTime() == 0) return purchaseOrder.getDeliveryDate();
        if (paymentCondition.getPeriodTypeSelect().equals(PaymentConditionRepository.PERIOD_TYPE_DAYS)) {
            return purchaseOrder.getDeliveryDate().plusDays(purchaseOrder.getPaymentCondition().getPaymentTime());
        } else if (paymentCondition.getPeriodTypeSelect().equals(PaymentConditionRepository.PERIOD_TYPE_MONTH)) {
            return purchaseOrder.getDeliveryDate().plusMonths(purchaseOrder.getPaymentCondition().getPaymentTime());
        } else {
            return purchaseOrder.getDeliveryDate();
        }
    }
}
