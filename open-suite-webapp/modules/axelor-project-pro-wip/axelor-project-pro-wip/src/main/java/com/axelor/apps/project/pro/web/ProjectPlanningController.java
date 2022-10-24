package com.axelor.apps.project.pro.web;

import com.axelor.i18n.I18n;
import com.axelor.meta.schema.actions.ActionView;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.axelor.rpc.Context;
import java.util.Collection;
import java.util.Map;

public class ProjectPlanningController {

  public void showTimeline(ActionRequest request, ActionResponse response) {

    Context context = request.getContext();

    Collection<Map<String, Object>> users =
        (Collection<Map<String, Object>>) context.get("userSet");

    if (users == null || users.isEmpty()) {
      response.setError(I18n.get("Please select users"));
      return;
    }

    String userIds = "";
    for (Map<String, Object> user : users) {
      if (userIds.isEmpty()) {
        userIds = user.get("id").toString();
      } else {
        userIds += "," + user.get("id").toString();
      }
    }

    response.setView(
        ActionView.define(I18n.get("TimeLine"))
            .add("html", "project-pro/scheduler?userIds=" + userIds)
            .domain("self.user.id in (:userIds)")
            .context("userIds", userIds)
            .map());
  }
}
