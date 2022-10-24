package com.avr.apps.docgen.web;

import com.axelor.exception.AxelorException;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;

public interface MetaFieldValidate {
	void selected(ActionRequest request, ActionResponse response) throws AxelorException;
	void clear(ActionRequest request, ActionResponse response) throws IllegalAccessException;
	void getDomain(ActionRequest request, ActionResponse response);
	void checkFieldCorrectly(ActionRequest request, ActionResponse response);
}
