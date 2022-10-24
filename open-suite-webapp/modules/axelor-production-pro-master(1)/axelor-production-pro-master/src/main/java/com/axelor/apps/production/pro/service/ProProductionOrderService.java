package com.axelor.apps.production.pro.service;

import com.axelor.apps.base.db.WeeklyPlanning;
import com.axelor.apps.production.db.Machine;
import com.axelor.apps.production.db.OperationOrder;
import com.axelor.apps.production.db.repo.OperationOrderRepository;
import com.axelor.apps.production.service.operationorder.OperationOrderWorkflowService;
import com.axelor.apps.tool.date.DateTool;
import com.axelor.apps.tool.date.DurationTool;
import com.axelor.exception.AxelorException;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import java.time.Duration;
import java.time.LocalDateTime;

public class ProProductionOrderService {

  @Inject protected OperationOrderRepository operationOrderRepo;

  @Inject protected OperationOrderWorkflowService operationOrderWorkflowService;

  /**
   * When we drag an operation order and the status is planned. We take the machine, the planned
   * start date and the workCenter and recalculate all the dates and duration with thos new datas.
   *
   * @param operationOrder
   * @return
   * @throws AxelorException
   */
  @Transactional(rollbackOn = {Exception.class})
  public OperationOrder microPlan(OperationOrder operationOrder, Long cumulatedDuration)
      throws AxelorException {

    System.out.println("Micro planning");
    LocalDateTime plannedStartDate = operationOrder.getPlannedStartDateT();

    LocalDateTime lastOPerationDate =
        operationOrderWorkflowService.getLastOperationOrder(operationOrder);
    LocalDateTime maxDate = DateTool.max(plannedStartDate, lastOPerationDate);
    operationOrder.setPlannedStartDateT(maxDate);

    Machine machine = operationOrder.getMachine();
    WeeklyPlanning weeklyPlanning = null;
    if (machine != null) {
      weeklyPlanning = machine.getWeeklyPlanning();
    }
    if (weeklyPlanning != null) {
      operationOrderWorkflowService.planWithPlanning(operationOrder, weeklyPlanning);
    }

    operationOrder.setPlannedEndDateT(
        operationOrderWorkflowService.computePlannedEndDateT(operationOrder));
    if (cumulatedDuration != null) {
      operationOrder.setPlannedEndDateT(
          operationOrder
              .getPlannedEndDateT()
              .minusSeconds(cumulatedDuration)
              .plusSeconds(operationOrderWorkflowService.getMachineSetupDuration(operationOrder)));
    }
    Long plannedDuration =
        DurationTool.getSecondsDuration(
            Duration.between(
                operationOrder.getPlannedStartDateT(), operationOrder.getPlannedEndDateT()));

    operationOrder.setPlannedDuration(plannedDuration);

    if (weeklyPlanning != null) {
      operationOrderWorkflowService.manageDurationWithMachinePlanning(
          operationOrder, weeklyPlanning, plannedDuration);
    }
    return operationOrderRepo.save(operationOrder);
  }
}
