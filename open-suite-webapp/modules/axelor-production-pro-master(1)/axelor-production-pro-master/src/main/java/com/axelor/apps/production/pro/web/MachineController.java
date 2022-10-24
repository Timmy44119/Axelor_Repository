package com.axelor.apps.production.pro.web;

import com.axelor.apps.production.db.WorkCenter;
import com.axelor.apps.production.db.WorkCenterGroup;
import com.axelor.apps.production.db.repo.WorkCenterGroupRepository;
import com.axelor.i18n.I18n;
import com.axelor.inject.Beans;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import java.util.List;
import java.util.stream.Collectors;

public class MachineController {

  private static final String PLANNING_PER_MACHINE_URL = "production-pro/planning-per-machine";
  private static final String PLANNING_PER_MACHINE = "Planning per machine";

  public void openStockLocationPlanning(ActionRequest request, ActionResponse response) {

    Object stockLocationId = request.getContext().get("id");

    response.setView(
        ActionView.define(I18n.get(PLANNING_PER_MACHINE))
            .add("html", PLANNING_PER_MACHINE_URL + "?stockLocationId=" + stockLocationId)
            .map());
  }

  public void openMachineTypePlanning(ActionRequest request, ActionResponse response) {

    Object machineTypeId = request.getContext().get("id");

    response.setView(
        ActionView.define(I18n.get(PLANNING_PER_MACHINE))
            .add("html", PLANNING_PER_MACHINE_URL + "?machineTypeId=" + machineTypeId)
            .map());
  }

  public void openMachinePlanning(ActionRequest request, ActionResponse response) {

    Object machineId = request.getContext().get("id");

    response.setView(
        ActionView.define(I18n.get(PLANNING_PER_MACHINE))
            .add("html", PLANNING_PER_MACHINE_URL + "?machineId=" + machineId)
            .map());
  }

  public void setWorkCenterDomain(ActionRequest request, ActionResponse response) {
    List<Long> ids = Lists.newArrayList();
    List<WorkCenterGroup> workCenterGroupModelList =
        Beans.get(WorkCenterGroupRepository.class).all().filter("self.template IS TRUE").fetch();
    for (WorkCenterGroup workCenterGroup : workCenterGroupModelList) {
      ids.addAll(
          workCenterGroup.getWorkCenterSet().stream()
              .map(WorkCenter::getId)
              .collect(Collectors.toList()));
    }
    response.setAttr("workCenter", "domain", "self.id in (" + Joiner.on(",").join(ids) + ")");
  }
}
