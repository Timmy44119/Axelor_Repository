package com.axelor.apps.production.pro.web;

import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.OperationOrder;
import com.axelor.apps.production.db.ProdProcessLine;
import com.axelor.apps.production.db.WorkCenter;
import com.axelor.apps.production.db.WorkCenterGroup;
import com.axelor.apps.production.db.repo.ManufOrderRepository;
import com.axelor.apps.production.db.repo.OperationOrderRepository;
import com.axelor.apps.production.pro.service.ProProductionOrderService;
import com.axelor.apps.production.service.operationorder.OperationOrderWorkflowService;
import com.axelor.exception.AxelorException;
import com.axelor.exception.service.TraceBackService;
import com.axelor.inject.Beans;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProProductionOrderController {

  @Inject private ManufOrderRepository manufOrderRepo;

  @Inject OperationOrderRepository operationOrderRepo;

  @Inject private ProProductionOrderService proProductionOrderService;

  public void operationOrderMove(ActionRequest request, ActionResponse response) {}

  public void operationOrderPlan(ActionRequest request, ActionResponse response)
      throws AxelorException {

    List<Object> orderIds = (List<Object>) request.getContext().get("currentViewEvents");

    List<ManufOrder> manufOrders =
        manufOrderRepo
            .all()
            .filter(
                "self.id in (select manufOrder.id from OperationOrder where id in (?))", orderIds)
            .fetch();
  }

  @Transactional(rollbackOn = {Exception.class})
  public void plan(ActionRequest request, ActionResponse response) {
    try {
      OperationOrder operationOrder = request.getContext().asType(OperationOrder.class);
      if (operationOrder != null && operationOrder.getId() != null) {
        operationOrder = Beans.get(OperationOrderRepository.class).find(operationOrder.getId());
        if (operationOrder.getStatusSelect() == OperationOrderRepository.STATUS_DRAFT) {
          operationOrder =
              Beans.get(OperationOrderWorkflowService.class).plan(operationOrder, null);
          response.setReload(true);
        } else if (operationOrder.getStatusSelect() == OperationOrderRepository.STATUS_PLANNED) {
          proProductionOrderService.microPlan(operationOrder, null);
        }
        operationOrder.setIsMicroPlanningDone(true);
        operationOrderRepo.save(operationOrder);
      }
    } catch (Exception e) {
      TraceBackService.trace(response, e);
    }
  }

  public OperationOrder getPreviousOperation(OperationOrder operationOrder) {

    OperationOrder lastOperationOrder =
        operationOrderRepo
            .all()
            .filter(
                "self.isMicroPlanningDone IS TRUE AND self.prodProcessLine.workCenterTypeSelect = 2 and self.prodProcessLine.workCenterGroup is not null and self.manufOrder = ?1 AND self.priority <= ?2 AND self.statusSelect >= 3 AND self.statusSelect < 6 AND self.id != ?3",
                operationOrder.getManufOrder(),
                operationOrder.getPriority(),
                operationOrder.getId())
            .order("-priority")
            .order("-plannedEndDateT")
            .fetchOne();
    return lastOperationOrder;
  }

  @Transactional
  public void toPlan(ActionRequest request, ActionResponse response) {
    Map<String, Object> data = request.getData();
    Long operationOrderId = null;
    if (data.get("id") != null) {
      operationOrderId = Long.decode((String) data.get("id"));
      OperationOrder operationOrder =
          Beans.get(OperationOrderRepository.class).find(operationOrderId);
      operationOrder.setStatusSelect(OperationOrderRepository.STATUS_DRAFT);
      Beans.get(OperationOrderRepository.class).save(operationOrder);
      response.setReload(true);
    }
  }

  @Transactional
  public void undoMicroPlanning(ActionRequest request, ActionResponse response) {
    Map<String, Object> data = request.getData();
    Long operationOrderId = null;
    if (data.get("id") != null) {
      operationOrderId = Long.decode((String) data.get("id"));
      OperationOrder operationOrder =
          Beans.get(OperationOrderRepository.class).find(operationOrderId);
      operationOrder.setIsMicroPlanningDone(false);
      Beans.get(OperationOrderRepository.class).save(operationOrder);
      response.setReload(true);
    }
  }

  public List<Long> getAuthorizedMachine(OperationOrder operationOrder) {
    List<Long> machinesIds = Lists.newArrayList();
    ProdProcessLine ppl = operationOrder.getProdProcessLine();
    if (ppl != null) {
      WorkCenterGroup wcg = ppl.getWorkCenterGroup();
      if (wcg != null) {
        for (WorkCenter wc : wcg.getWorkCenterSet()) {
          if (wc.getMachine() != null) {
            machinesIds.add(wc.getMachine().getId());
          }
        }
      }
    }
    return machinesIds;
  }

  public void getAuthorizedMachineAndPreviousOperation(
      ActionRequest request, ActionResponse response) {
    OperationOrder operationOrder = request.getContext().asType(OperationOrder.class);

    OperationOrder lastOperationOrder = this.getPreviousOperation(operationOrder);
    List<Long> machineIds = this.getAuthorizedMachine(operationOrder);
    Map<String, Object> responseData = new HashMap<String, Object>();
    responseData.put("lastOperationOrder", lastOperationOrder);
    responseData.put("machineIds", machineIds);
    response.setData(responseData);
  }
}
