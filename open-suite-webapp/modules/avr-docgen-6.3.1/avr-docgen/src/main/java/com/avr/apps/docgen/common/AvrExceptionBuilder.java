package com.avr.apps.docgen.common;

import com.avr.apps.docgen.exception.AvrException;
import com.axelor.common.ObjectUtils;
import com.axelor.db.Model;

import java.util.ArrayList;
import java.util.List;

public class AvrExceptionBuilder {

  private final List<String> errors = new ArrayList<>();
  public int category = 99;
  private Class<? extends Model> refClass = null;

  public AvrExceptionBuilder(int category) {
    this.category = category;
  }

  public AvrExceptionBuilder() {}

  public void setRefClass(Class<? extends Model> refClass) {
    this.refClass = refClass;
  }

  public void add(String message, Object... args) {
    errors.add(String.format(message, args));
  }

  public Boolean isEmpty() {
    return errors.isEmpty();
  }

  public Boolean isNotEmpty() {
    return !errors.isEmpty();
  }

  public int size() {
    return errors.size();
  }

  public String toString() {
    return String.join("<br>\n", errors);
  }

  public AvrException build() {
    if (ObjectUtils.notEmpty(refClass)) {
      return new AvrException(refClass, category, errors);
    }
    return new AvrException(category, errors);
  }
}
