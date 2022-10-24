/**
 * ***********************************
 *          AVR SOLUTIONS            *
 * ***********************************
 *
 *
 *
 * @author David
 * @date 11/03/2021
 * @time 17:09
 * @Update 11/03/2021
 * @version 1.0
 */

package com.avr.apps.docgen.web;

import com.avr.apps.docgen.common.utils.ObjectUtils;
import com.avr.apps.docgen.service.MetaFieldOverrideSerivce;
import com.avr.apps.docgen.common.Mapper;
import com.avr.apps.docgen.common.ValidatorFields;
import com.avr.apps.docgen.db.DocgenChildBinding;
import com.avr.apps.docgen.db.DocgenTemplate;
import com.avr.apps.docgen.db.TypeTemplate;
import com.avr.apps.docgen.exception.IExceptionMessage;
import com.axelor.db.JPA;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.meta.db.repo.MetaModelRepository;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import com.google.inject.servlet.RequestScoped;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import javax.persistence.Query;

@RequestScoped
public class DocgenChildBindingController implements MetaFieldValidate {

    private static final String COLUMN_NAME = "keyBinding";
    private final List<String> RELATION_SHIP = new ArrayList<>();

    @Inject
    MetaFieldOverrideSerivce metaFieldOverrideSerivce;

    @Inject
    MetaModelRepository metaModelRepository;

    public DocgenChildBindingController() {
        RELATION_SHIP.add("OneToMany");
        RELATION_SHIP.add("ManyToMany");
    }

    @Override
    public void selected(ActionRequest request, ActionResponse response) throws AxelorException {
        DocgenChildBinding docgenChildBinding = request.getContext().asType(DocgenChildBinding.class);
        if (docgenChildBinding.getMetaField() == null) throw new AxelorException(
            TraceBackRepository.CATEGORY_MISSING_FIELD,
            IExceptionMessage.NOT_METAFILE_SELECTED
        );
        metaFieldOverrideSerivce.selectedFields(
            new ValidatorFields(docgenChildBinding.getTargetField(), docgenChildBinding.getMetaField(), COLUMN_NAME),
            response,
            true,
            false
        );
        response.setValue(
            "metaModel",
            metaModelRepository
                .all()
                .filter(
                    "self.fullName = ?",
                    String.format(
                        "%s.%s",
                        docgenChildBinding.getMetaField().getPackageName(),
                        docgenChildBinding.getMetaField().getTypeName()
                    )
                )
                .fetchOne()
        );
    }

    @Override
    @Transactional(rollbackOn = { AxelorException.class, Exception.class })
    public void clear(ActionRequest request, ActionResponse response) {
        DocgenChildBinding docgenChildBinding = request.getContext().asType(DocgenChildBinding.class);
        metaFieldOverrideSerivce.clear(
            new ValidatorFields(docgenChildBinding.getTargetField(), docgenChildBinding.getMetaField(), COLUMN_NAME),
            response
        );
    }

    @Override
    public void getDomain(ActionRequest request, ActionResponse response) {
        DocgenChildBinding docgenChildBinding = request.getContext().asType(DocgenChildBinding.class);
        metaFieldOverrideSerivce.getDomain(
            new ValidatorFields(docgenChildBinding.getTargetField(), docgenChildBinding.getMetaField(), COLUMN_NAME),
            request.getContext().getParent().asType(DocgenTemplate.class).getMetaModel(),
            response,
            true,
            RELATION_SHIP.toArray(new String[0])
        );
    }

    public void getDomainFilter(ActionRequest request, ActionResponse response) {
        DocgenChildBinding docgenChildBinding = request.getContext().asType(DocgenChildBinding.class);
        metaFieldOverrideSerivce.getDomain(
            new ValidatorFields(null, docgenChildBinding.getMetaField(), COLUMN_NAME, "metaFieldOrder"),
            docgenChildBinding.getMetaModel(),
            response,
            false,
            RELATION_SHIP.toArray(new String[0])
        );
    }

    @Override
    public void checkFieldCorrectly(ActionRequest request, ActionResponse response) {
        DocgenChildBinding docgenChildBinding = request.getContext().asType(DocgenChildBinding.class);
        if (docgenChildBinding.getTypeTemplate().equals(TypeTemplate.FIELD)) {
            if (ObjectUtils.isEmpty(docgenChildBinding.getTargetField())) return;
            response.setValue("$isValidate", true);
            response.setAttr("selectedMetaField", "readonly", true);
            response.setAttr("metaField", "readonly", true);
        } else {
            response.setValue("$isValidate", true);
        }
    }

    public void onChangeTypeTemplate(ActionRequest request, ActionResponse response) {
        DocgenChildBinding docgenChildBinding = request.getContext().asType(DocgenChildBinding.class);
        clear(request, response);
        response.setValue(
            "$isValidate",
            Arrays.asList(TypeTemplate.QUERY, TypeTemplate.QUERY_NATIVE).contains(docgenChildBinding.getTypeTemplate())
        );
    }

    /**
     *
     * @param request
     * @param response
     */
    public void testQuery(ActionRequest request, ActionResponse response) throws ClassNotFoundException {
        DocgenChildBinding docgenChildBinding = request.getContext().asType(DocgenChildBinding.class);
        Class<?> aClass = Class.forName(docgenChildBinding.getMetaModel().getFullName());

        Query query;
        if (docgenChildBinding.getTypeTemplate().equals(TypeTemplate.QUERY)) query =
            JPA.em().createQuery(docgenChildBinding.getQuery(), aClass); else query =
            JPA.em().createNativeQuery(docgenChildBinding.getQuery(), aClass);

        List<?> multiResult = query.getResultList();
        response.setFlash(multiResult != null ? displayHelper(Mapper.toListOfMap(multiResult), true) : "null");
    }

    /**
     *
     * @param request
     * @param response
     */
    public void typeTemplateChange(ActionRequest request, ActionResponse response) {
        response.setValue(
            "$isValidate",
            Arrays
                .asList(TypeTemplate.QUERY, TypeTemplate.QUERY_NATIVE)
                .contains(request.getContext().asType(DocgenChildBinding.class).getTypeTemplate())
        );
        response.setValue("metaModel", null);
        response.setValue("targetField", null);
        response.setValue("docgenBindingList", new ArrayList<>());
        response.setValue("query", null);
    }

    public static String displayHelper(List<Map<String, Object>> list) {
        return displayHelper(list, false);
    }

    public static String displayHelper(List<Map<String, Object>> list, boolean newLineHtml) {
        List<String> strings = new ArrayList<>();
        list.forEach(el -> strings.add(displayHelper(el)));
        return String.join(newLineHtml ? "<br>" : "\n", strings);
    }

    public static String displayHelper(Map<String, Object> map) {
        StringBuilder sb = new StringBuilder(map.get("id").toString());

        if (map.containsKey("name")) {
            sb.append(String.format(" - %s", map.get("name").toString()));
        }

        if (map.containsKey("fullname")) {
            sb.append(String.format(" - %s", map.get("fullname").toString()));
        }

        if (map.containsKey("fullName")) {
            sb.append(String.format(" - %s", map.get("fullName").toString()));
        }

        if (map.containsKey("code")) {
            sb.append(String.format(" - %s", map.get("code").toString()));
        }

        return sb.toString();
    }
}
