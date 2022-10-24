package com.axelor.apps.production.pro.web;

import com.axelor.apps.production.pro.service.PlanificationService;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.common.collect.Lists;
import com.google.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import wslite.json.JSONException;

public class PlanificationController {

  @Inject PlanificationService service;

  public void initData(ActionRequest request, ActionResponse response) throws JSONException {
    Map<String, Object> data = request.getData();
    Long machineTypeId = null;
    Long machineId = null;
    Long employeeId = null;
    Long stockLocationId = null;
    Long workCenterGroupId = null;
    String search = "";
    String secondSearch = "";
    String searchTypeSelect = "left";
    Boolean planningEmployee = false;
    if (data.get("machineTypeId") != null) {
      machineTypeId = Long.decode((String) data.get("machineTypeId"));
    }
    if (data.get("machineId") != null) {
      machineId = Long.decode((String) data.get("machineId"));
    }
    if (data.get("employeeId") != null) {
      employeeId = Long.decode((String) data.get("employeeId"));
    }
    if (data.get("planningEmployee") != null) {
      if (data.get("planningEmployee") instanceof String) {
        planningEmployee = Boolean.parseBoolean((String) data.get("planningEmployee"));
      } else {
        planningEmployee = (Boolean) data.get("planningEmployee");
      }
    }
    if (data.get("search") != null) {
      search = (String) data.get("search");
    }
    if (data.get("secondSearch") != null) {
      secondSearch = (String) data.get("secondSearch");
    }
    if (data.get("searchTypeSelect") != null) {
      searchTypeSelect = (String) data.get("searchTypeSelect");
    }
    if (data.get("workCenterGroupId") != null) {
      workCenterGroupId = Long.valueOf((String) data.get("workCenterGroupId"));
    }
    if (data.get("stockLocationId") != null) {
      stockLocationId = Long.decode((String) data.get("stockLocationId"));
    }

    Map<String, Object> responseData =
        service.initData(
            machineTypeId,
            machineId,
            stockLocationId,
            workCenterGroupId,
            employeeId,
            search,
            secondSearch,
            searchTypeSelect,
            planningEmployee);

    response.setData(responseData);
  }

  public void initStaticData(ActionRequest request, ActionResponse response) throws JSONException {
    Map<String, Object> data = request.getData();
    Long machineTypeId = null;
    Long machineId = null;
    Long employeeId = null;
    Long stockLocationId = null;
    Long workCenterGroupId = null;
    if (data.get("machineTypeId") != null) {
      machineTypeId = Long.decode((String) data.get("machineTypeId"));
    }
    if (data.get("machineId") != null) {
      machineId = Long.decode((String) data.get("machineId"));
    }
    if (data.get("employeeId") != null) {
      employeeId = Long.decode((String) data.get("employeeId"));
    }
    if (data.get("stockLocationId") != null) {
      stockLocationId = Long.decode((String) data.get("stockLocationId"));
    }
    if (data.get("workCenterGroupId") != null) {
      workCenterGroupId = Long.decode((String) data.get("workCenterGroupId"));
    }

    Map<String, Object> responseData =
        service.initStaticData(
            machineTypeId, machineId, stockLocationId, workCenterGroupId, employeeId);

    response.setData(responseData);
  }

  public void initOperationData(ActionRequest request, ActionResponse response)
      throws JSONException {
    Map<String, Object> data = request.getData();
    Long workCenterGroupId = null;
    String search = "";
    String secondSearch = "";
    String searchTypeSelect = "left";
    List<Long> machineIds = Lists.newArrayList();
    List<Long> employeeIds = Lists.newArrayList();
    Boolean planningEmployee = false;

    if (data.get("machineIds") != null) {
      List<Integer> objects = (List<Integer>) data.get("machineIds");
      if (objects != null && objects.size() > 0) {
        machineIds =
            objects.stream().mapToLong(Integer::longValue).boxed().collect(Collectors.toList());
      }
    }

    if (data.get("employeeIds") != null) {
      List<Integer> objects = (List<Integer>) data.get("employeeIds");
      if (objects != null && objects.size() > 0) {
        employeeIds =
            objects.stream().mapToLong(Integer::longValue).boxed().collect(Collectors.toList());
      }
    }

    if (data.get("search") != null) {
      search = (String) data.get("search");
    }
    if (data.get("planningEmployee") != null) {
      if (data.get("planningEmployee") instanceof String) {
        planningEmployee = Boolean.parseBoolean((String) data.get("planningEmployee"));
      } else {
        planningEmployee = (Boolean) data.get("planningEmployee");
      }
    }
    if (data.get("secondSearch") != null) {
      secondSearch = (String) data.get("secondSearch");
    }
    if (data.get("searchTypeSelect") != null) {
      searchTypeSelect = (String) data.get("searchTypeSelect");
    }
    if (data.get("workCenterGroupId") != null) {
      if (data.get("workCenterGroupId") instanceof Integer) {
        workCenterGroupId = Long.valueOf((Integer) data.get("workCenterGroupId"));
      } else {
        workCenterGroupId = Long.valueOf((String) data.get("workCenterGroupId"));
      }
    }

    Map<String, Object> responseData =
        service.initOperationData(
            machineIds,
            employeeIds,
            workCenterGroupId,
            search,
            secondSearch,
            searchTypeSelect,
            planningEmployee);

    response.setData(responseData);
  }
}
