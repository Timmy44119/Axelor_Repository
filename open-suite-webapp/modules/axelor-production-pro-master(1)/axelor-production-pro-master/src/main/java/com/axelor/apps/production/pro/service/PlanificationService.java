package com.axelor.apps.production.pro.service;

import com.axelor.apps.base.db.DayPlanning;
import com.axelor.apps.base.db.Partner;
import com.axelor.apps.base.db.Product;
import com.axelor.apps.base.db.WeeklyPlanning;
import com.axelor.apps.base.db.repo.DayPlanningRepository;
import com.axelor.apps.base.db.repo.WeeklyPlanningRepository;
import com.axelor.apps.maintenance.db.repo.MaintenanceRequestRepository;
import com.axelor.apps.production.db.BillOfMaterial;
import com.axelor.apps.production.db.Machine;
import com.axelor.apps.production.db.ManufOrder;
import com.axelor.apps.production.db.OperationOrder;
import com.axelor.apps.production.db.ProdHumanResource;
import com.axelor.apps.production.db.ProdProcess;
import com.axelor.apps.production.db.ProdProcessLine;
import com.axelor.apps.production.db.WorkCenter;
import com.axelor.apps.production.db.WorkCenterGroup;
import com.axelor.apps.production.db.repo.MachineRepository;
import com.axelor.apps.production.db.repo.OperationOrderRepository;
import com.axelor.apps.production.db.repo.ProdHumanResourceRepository;
import com.axelor.apps.production.db.repo.ProdProcessLineRepository;
import com.axelor.apps.production.db.repo.WorkCenterGroupRepository;
import com.axelor.apps.production.db.repo.WorkCenterRepository;
import com.axelor.apps.production.service.app.AppProductionService;
import com.axelor.apps.tool.ObjectTool;
import com.axelor.db.Query;
import com.axelor.db.mapper.Mapper;
import com.axelor.inject.Beans;
import com.axelor.script.GroovyScriptHelper;
import com.axelor.script.ScriptBindings;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import wslite.json.JSONException;
import wslite.json.JSONObject;

public class PlanificationService {

  @Inject WorkCenterRepository workCenterRepo;
  @Inject WorkCenterGroupRepository workCenterGroupRepo;
  @Inject OperationOrderRepository operationOrderRepo;
  @Inject WeeklyPlanningRepository weeklyPlanningRepo;
  @Inject DayPlanningRepository dayPlanningRepo;
  @Inject MaintenanceRequestRepository maintenanceRequestRepo;
  @Inject ProdHumanResourceRepository prodHumanResourceRepo;
  @Inject AppProductionService appProductionService;

  public static final String PLANNING_FILTER_LEFT = "left";
  public static final String PLANNING_FILTER_RIGHT = "right";
  public static final String PLANNING_FILTER_ALL = "all";

  public Map<String, Object> initStaticData(
      Long machineTypeId,
      Long machineId,
      Long stockLocationId,
      Long workCenterGroupId,
      Long employeeId)
      throws JSONException {
    Map<String, Object> responseData = new HashMap<String, Object>();
    responseData.put("manageChangeDuration", false);
    if (workCenterGroupId != null) {
      WorkCenterGroup wcg = Beans.get(WorkCenterGroupRepository.class).find(workCenterGroupId);
      responseData.put("workCenterGroupName", wcg.getName());
    }
    try {
      if (!(machineTypeId == null && machineId == null && stockLocationId == null)) {
        List<WorkCenterGroup> workCenterGroupModelList = this.getWorkCenterGroupModelList();
        responseData.put("workCenterGroupModelList", workCenterGroupModelList);
        List<Machine> machineList = Lists.newArrayList();
        machineList = this.getMachineList(machineTypeId, machineId, stockLocationId);
        if (!(machineList == null || machineList.size() == 0)) {
          responseData.put("machineList", machineList);
          List<Long> machineIds =
              machineList.stream().map(Machine::getId).collect(Collectors.toList());
          if (!(machineIds == null || machineIds.size() == 0)) {

            List<DayPlanning> dayPlanningList = this.getDayPlanningList(machineList);
            responseData.put("dayPlanningList", dayPlanningList);
          }
        }
      }
    } catch (Exception e) {
      responseData.put("machineList", Lists.newArrayList());
      responseData.put("dayPlanningList", Lists.newArrayList());
      responseData.put("equipmentMaintenanceList", Lists.newArrayList());
    }
    return responseData;
  }

  public Map<String, Object> initOperationData(
      List<Long> machineIds,
      List<Long> employeeIds,
      Long workCenterGroupId,
      String search,
      String secondSearch,
      String searchTypeSelect,
      Boolean planningEmployee)
      throws JSONException {
    Map<String, Object> responseData = new HashMap<String, Object>();

    List<OperationOrder> operationOrderList =
        this.getOperationOrderList(
            machineIds,
            employeeIds,
            workCenterGroupId,
            search,
            secondSearch,
            searchTypeSelect,
            planningEmployee);
    if (!(operationOrderList == null || operationOrderList.size() == 0)) {
      List<JSONObject> renderOperation = this.getJsonFromOperationOrder(operationOrderList);
      List<JSONObject> renderOperationWithoutDuplicates =
          renderOperation.stream()
              .filter(
                  ObjectTool.distinctByKey(
                      p -> {
                        try {
                          return p.get("id");
                        } catch (JSONException e) {
                          // TODO Auto-generated catch block
                          e.printStackTrace();
                        }
                        return p;
                      }))
              .collect(Collectors.toList());
      responseData.put("renderOperationOrder", renderOperationWithoutDuplicates);
    }
    return responseData;
  }

  public Map<String, Object> initData(
      Long machineTypeId,
      Long machineId,
      Long stockLocationId,
      Long workCenterGroupId,
      Long employeeId,
      String search,
      String secondSearch,
      String searchTypeSelect,
      Boolean planningEmployee)
      throws JSONException {
    Map<String, Object> responseData = new HashMap<String, Object>();
    responseData.put("manageChangeDuration", false);
    List<WorkCenterGroup> workCenterGroupModelList = this.getWorkCenterGroupModelList();
    responseData.put("workCenterGroupModelList", workCenterGroupModelList);
    List<Machine> machineList = Lists.newArrayList();
    try {
      if (!(machineTypeId == null && machineId == null && stockLocationId == null)) {
        machineList = this.getMachineList(machineTypeId, machineId, stockLocationId);
        if (!(machineList == null || machineList.size() == 0)) {
          responseData.put("machineList", machineList);
          List<Long> machineIds =
              machineList.stream().map(Machine::getId).collect(Collectors.toList());
          if (!(machineIds == null || machineIds.size() == 0)) {
            List<DayPlanning> dayPlanningList = this.getDayPlanningList(machineList);
            responseData.put("dayPlanningList", dayPlanningList);
            List<OperationOrder> operationOrderList =
                this.getOperationOrderList(
                    machineIds,
                    Lists.newArrayList(),
                    workCenterGroupId,
                    search,
                    secondSearch,
                    searchTypeSelect,
                    planningEmployee);
            if (!(operationOrderList == null || operationOrderList.size() == 0)) {
              List<JSONObject> renderOperation = this.getJsonFromOperationOrder(operationOrderList);
              List<JSONObject> renderOperationWithoutDuplicates =
                  renderOperation.stream()
                      .filter(
                          ObjectTool.distinctByKey(
                              p -> {
                                try {
                                  return p.get("id");
                                } catch (JSONException e) {
                                  // TODO Auto-generated catch block
                                  e.printStackTrace();
                                }
                                return p;
                              }))
                      .collect(Collectors.toList());
              responseData.put("renderOperationOrder", renderOperationWithoutDuplicates);
            }
          }
        }
      }
    } catch (Exception e) {
      responseData.put("machineList", Lists.newArrayList());
      responseData.put("operationOrderList", Lists.newArrayList());
      responseData.put("dayPlanningList", Lists.newArrayList());
      responseData.put("equipmentMaintenanceList", Lists.newArrayList());
      responseData.put("dayPlanningList", Lists.newArrayList());
      responseData.put("operationOrderList", Lists.newArrayList());
      responseData.put("renderOperationOrder", Lists.newArrayList());
    }
    return responseData;
  }

  public List<Machine> getMachineList(Long machineTypeId, Long machineId, Long stockLocationId) {

    String machineDomain =
        "self.workCenter IS NOT NULL AND (self.archived IS FALSE OR self.archived IS NULL OR self.archived IS NOT TRUE) AND ";
    if (stockLocationId != null) {
      machineDomain = machineDomain + "self.stockLocation.id = " + stockLocationId;
    } else if (machineTypeId != null) {
      machineDomain = machineDomain + "self.machineType.id = " + machineTypeId;
    } else if (machineId != null) {
      machineDomain = machineDomain + "self.id = " + machineId;
    }
    return Beans.get(MachineRepository.class).all().filter(machineDomain).fetch();
  }

  public List<WorkCenterGroup> getWorkCenterGroupModelList() {
    return workCenterGroupRepo
        .all()
        .filter(
            "self.template IS TRUE AND (self.archived IS FALSE OR self.archived IS NULL OR self.archived IS NOT TRUE)")
        .fetch();
  }

  public String getDefaultOperationOrderListDomain(
      List<Long> machineIds, List<Long> employeeIds, Boolean planningEmployee) {
    String domain =
        "(self.archived IS FALSE OR self.archived IS NULL OR self.archived IS NOT TRUE) "
            + "AND ( (unaccent(lower(self.manufOrder.manufOrderSeq)) like unaccent(lower(:search))) "
            + "OR (unaccent(lower(self.operationName)) like unaccent(lower(:search))) "
            + "OR (unaccent(lower(self.manufOrder.product.fullName)) like unaccent(lower(:search))) "
            + "OR (unaccent(lower(self.manufOrder.clientPartner.fullName)) like unaccent(lower(:search)))  "
            + "OR (unaccent(lower(self.manufOrder.productionOrderSet.productionOrderSeq)) like unaccent(lower(:search)))  "
            + "OR (unaccent(lower(self.manufOrder.saleOrderSet.saleOrderSeq)) like unaccent(lower(:search))) ) "
            + "AND ( (unaccent(lower(self.manufOrder.manufOrderSeq)) like unaccent(lower(:secondSearch))) "
            + "OR (unaccent(lower(self.operationName)) like unaccent(lower(:secondSearch))) "
            + "OR (unaccent(lower(self.manufOrder.product.fullName)) like unaccent(lower(:secondSearch))) "
            + "OR (unaccent(lower(self.manufOrder.clientPartner.fullName)) like unaccent(lower(:secondSearch)))  "
            + "OR (unaccent(lower(self.manufOrder.productionOrderSet.productionOrderSeq)) like unaccent(lower(:secondSearch)))  "
            + "OR (unaccent(lower(self.manufOrder.saleOrderSet.saleOrderSeq)) like unaccent(lower(:secondSearch))) )"
            + "AND self.prodProcessLine.workCenterGroup IS NOT NULL "
            + "AND self.statusSelect IN (1, 3, 4, 5, 6) ";
    if (machineIds != null && machineIds.size() > 0) {
      domain =
          domain
              + "AND self.prodProcessLine.workCenterGroup.workCenterSet.machine.id IN ( :machineIds ) "
              + "AND self.prodProcessLine.workCenterTypeSelect != 1 ";
    }
    return domain;
  }

  public List<OperationOrder> applyFilter(
      String domain,
      String searchTypeSelect,
      List<Long> machineIds,
      String search,
      String secondSearch,
      List<Long> prodProcessLineIds,
      List<Long> employeeIds,
      Boolean planningEmployee) {
    List<OperationOrder> operationOrderList = Lists.newArrayList();
    List<OperationOrder> operationOrderListNotFiltered = Lists.newArrayList();
    List<OperationOrder> operationOrderListFiltered = Lists.newArrayList();
    String domainNotFiltered = "";
    String domainFiltered = "";
    Query<OperationOrder> queryNotFiltered = null;
    Query<OperationOrder> queryFiltered = null;
    switch (searchTypeSelect) {
      case PLANNING_FILTER_LEFT: // We filter only the unplanned operation of the micro planning
        domainNotFiltered = domain + " AND self.isMicroPlanningDone IS TRUE ";
        queryNotFiltered =
            operationOrderRepo
                .all()
                .filter(domainNotFiltered)
                .bind("search", "%%")
                .bind("secondSearch", "%%")
                .bind("machineIds", machineIds);
        if (prodProcessLineIds != null) {
          queryNotFiltered.bind("prodProcessLineIds", prodProcessLineIds);
        }
        operationOrderListNotFiltered = queryNotFiltered.fetch();
        domainFiltered = domain + " AND self.isMicroPlanningDone IS FALSE ";
        queryFiltered =
            operationOrderRepo
                .all()
                .filter(domainFiltered)
                .bind("search", "%" + search + "%")
                .bind("secondSearch", "%" + secondSearch + "%")
                .bind("machineIds", machineIds);
        if (prodProcessLineIds != null) {
          queryFiltered.bind("prodProcessLineIds", prodProcessLineIds);
        }
        operationOrderListFiltered = queryFiltered.fetch();
        break;
      case PLANNING_FILTER_RIGHT: // We filter only the draft operation (left of the planning)
        domainNotFiltered = domain + " AND self.isMicroPlanningDone IS FALSE ";
        queryNotFiltered =
            operationOrderRepo
                .all()
                .filter(domainNotFiltered)
                .bind("search", "%%")
                .bind("secondSearch", "%%")
                .bind("machineIds", machineIds);
        if (prodProcessLineIds != null) {
          queryNotFiltered.bind("prodProcessLineIds", prodProcessLineIds);
        }
        operationOrderListNotFiltered = queryNotFiltered.fetch();

        domainFiltered = domain + " AND self.isMicroPlanningDone IS TRUE";
        queryFiltered =
            operationOrderRepo
                .all()
                .filter(domainFiltered)
                .bind("search", "%" + search + "%")
                .bind("secondSearch", "%" + secondSearch + "%")
                .bind("machineIds", machineIds);
        if (prodProcessLineIds != null) {
          queryFiltered.bind("prodProcessLineIds", prodProcessLineIds);
        }
        operationOrderListFiltered = queryFiltered.fetch();
        break;

      case PLANNING_FILTER_ALL: // We filter only the draft operation (left of the planning)
        Query<OperationOrder> query =
            operationOrderRepo
                .all()
                .filter(domain)
                .bind("search", "%" + search + "%")
                .bind("secondSearch", "%" + secondSearch + "%")
                .bind("machineIds", machineIds);
        if (prodProcessLineIds != null) {
          query.bind("prodProcessLineIds", prodProcessLineIds);
        }
        operationOrderListFiltered = query.fetch();
        break;
    }
    operationOrderList.addAll(operationOrderListNotFiltered);
    operationOrderList.addAll(operationOrderListFiltered);

    return operationOrderList;
  }

  public List<OperationOrder> getOperationOrderList(
      List<Long> machineIds,
      List<Long> employeeIds,
      Long workCenterGroupId,
      String search,
      String secondSearch,
      String searchTypeSelect,
      Boolean planningEmployee) {
    String domain =
        this.getDefaultOperationOrderListDomain(machineIds, employeeIds, planningEmployee);
    List<OperationOrder> operationOrderList = Lists.newArrayList();
    if (workCenterGroupId != null) {
      List<ProdProcessLine> prodProcessLineList =
          Beans.get(ProdProcessLineRepository.class)
              .all()
              .filter(
                  "(self.archived IS FALSE OR self.archived IS NULL OR self.archived IS NOT TRUE) AND self.workCenterGroup.workCenterGroupModel.id = ?1 and self.workCenterGroup IS NOT NULL",
                  workCenterGroupId)
              .fetch();
      if (prodProcessLineList != null && prodProcessLineList.size() != 0) {
        List<Long> prodProcessLineIds =
            prodProcessLineList.stream().map(ProdProcessLine::getId).collect(Collectors.toList());
        domain =
            "(self.archived IS FALSE OR self.archived IS NULL OR self.archived IS NOT TRUE) AND self.prodProcessLine.id IN (:prodProcessLineIds) AND "
                + domain;
        operationOrderList =
            applyFilter(
                domain,
                searchTypeSelect,
                machineIds,
                search,
                secondSearch,
                prodProcessLineIds,
                employeeIds,
                planningEmployee);
      } else {
        operationOrderList = Lists.newArrayList();
      }
    } else {
      operationOrderList =
          applyFilter(
              domain,
              searchTypeSelect,
              machineIds,
              search,
              secondSearch,
              null,
              employeeIds,
              planningEmployee);
    }
    return operationOrderList;
  }

  public List<DayPlanning> getDayPlanningList(List<Machine> machineList) {
    List<WeeklyPlanning> weeklyPlanningList =
        machineList.stream()
            .filter(m -> m.getWeeklyPlanning() != null)
            .map(Machine::getWeeklyPlanning)
            .collect(Collectors.toList());
    if (weeklyPlanningList != null && weeklyPlanningList.size() > 0) {
      List<Long> weeklyPlanningIds =
          weeklyPlanningList.stream().map(WeeklyPlanning::getId).collect(Collectors.toList());
      return dayPlanningRepo
          .all()
          .filter(
              "(self.archived IS FALSE OR self.archived IS NULL OR self.archived IS NOT TRUE) AND self.weeklyPlann.id IN (:weeklyPlanningIds)")
          .bind("weeklyPlanningIds", weeklyPlanningIds)
          .fetch();
    }
    return Lists.newArrayList();
  }

  public String getHeaderColor(OperationOrder oo) {
    String cardHeaderColorExpr = appProductionService.getAppProduction().getCardHeaderColor();
    ScriptBindings bindings = new ScriptBindings(Mapper.toMap(oo));
    if (!Strings.isNullOrEmpty(cardHeaderColorExpr)) {
      String headerColor = (String) new GroovyScriptHelper(bindings).eval(cardHeaderColorExpr);
      return headerColor;
    }
    return "";
  }

  public String getContentColor(OperationOrder oo) {
    String cardContentColorExpr = appProductionService.getAppProduction().getCardContentColor();
    ScriptBindings bindings = new ScriptBindings(Mapper.toMap(oo));
    if (!Strings.isNullOrEmpty(cardContentColorExpr)) {
      String contentColor = (String) new GroovyScriptHelper(bindings).eval(cardContentColorExpr);
      return contentColor;
    }
    return "";
  }

  public List<JSONObject> getJsonFromOperationOrder(List<OperationOrder> operationOrderList)
      throws JSONException {
    List<JSONObject> operationRenderList = Lists.newArrayList();
    for (OperationOrder oo : operationOrderList) {

      JSONObject data = new JSONObject();
      String headerColor = getHeaderColor(oo);
      String contentColor = getContentColor(oo);
      if (!Strings.isNullOrEmpty(headerColor)) {
        data.put("headerColor", headerColor);
      } else {
        data.put("headerColor", "white");
      }
      if (!Strings.isNullOrEmpty(contentColor)) {
        data.put("contentColor", contentColor);
      } else {
        data.put("contentColor", "white");
      }
      data.put("color", "white");
      data.put("id", oo.getId());
      ProdProcessLine ppl = oo.getProdProcessLine();
      data.put("prodProcessLine", ppl);
      data.put("outsourcing", ppl.getName());
      WorkCenter wc = oo.getWorkCenter();
      data.put("workCenter", wc);
      ManufOrder mo = oo.getManufOrder();
      if (mo != null) {
        data.put("manufOrder", mo);
        Product product = mo.getProduct();
        data.put("product", product);
        Partner clientPartner = mo.getClientPartner();
        data.put("clientPartner", clientPartner);
        data.put("qty", mo.getQty());
        BillOfMaterial bom = mo.getBillOfMaterial();
        if (bom != null) {
          data.put("billOfMaterial", bom);
          data.put("bom", bom.getFullName());
        }
        ProdProcess pp = mo.getProdProcess();
        if (pp != null) {
          data.put("prodProcessFull", pp);
          data.put("prodProcess", pp.getFullName());
        }

        data.put("orderType", mo.getTypeSelect());
        long maintenanceRequestCount =
            maintenanceRequestRepo.all().filter("self.manufOrder = ?1", mo).count();
        if (maintenanceRequestCount > 0) {
          data.put("isMaintained", true);
        } else {
          data.put("isMaintained", false);
        }
        if (mo.getSaleOrderSet() != null && mo.getSaleOrderSet().size() > 0) {
          data.put("saleOrder", mo.getSaleOrderSet().iterator().next());
        }
      }

      if (oo.getRealStartDateT() != null) {
        data.put("start_date", oo.getRealStartDateT());
      } else {
        LocalDateTime plannedStartDateT = oo.getPlannedStartDateT();
        data.put("start_date", plannedStartDateT);
      }
      if (oo.getRealEndDateT() != null) {
        data.put("end_date", oo.getRealEndDateT());
      } else {
        LocalDateTime plannedEndDateT = oo.getPlannedEndDateT();
        data.put("end_date", plannedEndDateT);
      }

      data.put("isMicroPlanningDone", oo.getIsMicroPlanningDone());
      data.put("text", oo.getOperationName());
      data.put("changeDuration", true);
      data.put("operationName", oo.getOperationName());

      data.put("realDuration", oo.getRealDuration());
      data.put("plannedDuration", oo.getPlannedDuration());
      data.put("statusSelect", oo.getStatusSelect());
      data.put("isPinned", oo.getIsPinned());
      data.put("priority", oo.getPriority());

      if (oo.getMachine() != null) {
        data.put("section_id", oo.getMachine().getId());
      } else if (oo.getWorkCenter() != null && oo.getWorkCenter().getMachine() != null) {
        data.put("section_id", oo.getWorkCenter().getMachine().getId());
      }

      data.put("textColor", "black");

      List<ProdHumanResource> prodHumanResourceList = oo.getProdHumanResourceList();
      String employees = null;
      int i = 0;
      for (ProdHumanResource prodHumanResource : prodHumanResourceList) {
        if (prodHumanResource.getEmployee() != null) {
          if (i == 0) {
            employees = prodHumanResource.getEmployee().getName();
          }
          if (i > 0) {
            employees = employees + " | " + prodHumanResource.getEmployee().getName();
          }
        }
        i++;
      }
      if (employees != null) {
        data.put("employees", employees);
      }

      if (mo.getTypeSelect() == 1
          || ((boolean) data.get("isMaintained") == true && mo.getTypeSelect() == 2)) {
        operationRenderList.add(data);
      }
    }

    return operationRenderList;
  }
}
