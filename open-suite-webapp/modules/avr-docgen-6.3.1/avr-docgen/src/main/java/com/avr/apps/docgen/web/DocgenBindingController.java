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

import com.avr.apps.docgen.service.MetaFieldOverrideSerivce;
import com.avr.apps.docgen.common.ValidatorFields;
import com.avr.apps.docgen.web.MetaFieldValidate;
import com.avr.apps.docgen.db.DocgenBinding;
import com.avr.apps.docgen.db.TypeData;
import com.avr.apps.docgen.db.TypeTemplate;
import com.avr.apps.docgen.exception.IExceptionMessage;
import com.axelor.db.JPA;
import com.axelor.exception.AxelorException;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.meta.db.MetaModel;
import com.axelor.meta.db.repo.MetaSelectItemRepository;
import com.axelor.rpc.ActionRequest;
import com.axelor.rpc.ActionResponse;
import com.google.inject.Inject;
import com.google.inject.persist.Transactional;
import com.google.inject.servlet.RequestScoped;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.persistence.Query;

@RequestScoped
public class DocgenBindingController implements MetaFieldValidate {

    public static final String COLUMN_NAME = "keyBinding";
    private final List<String> RELATION_SHIP = new ArrayList<>();

    protected MetaFieldOverrideSerivce metaFieldOverrideSerivce;
    protected MetaSelectItemRepository metaSelectItemRepository;

    @Inject
    public DocgenBindingController(
        MetaFieldOverrideSerivce metaFieldOverrideSerivce,
        MetaSelectItemRepository metaSelectItemRepository
    ) {
        RELATION_SHIP.add("OneToMany");
        RELATION_SHIP.add("ManyToMany");
        this.metaFieldOverrideSerivce = metaFieldOverrideSerivce;
        this.metaSelectItemRepository = metaSelectItemRepository;
    }

    /**
     * @param request
     * @param response
     */
    public void selected(ActionRequest request, ActionResponse response) throws AxelorException {
        DocgenBinding docgenBinding = request.getContext().asType(DocgenBinding.class);
        if (docgenBinding.getMetaField() == null) throw new AxelorException(
            TraceBackRepository.CATEGORY_MISSING_FIELD,
            IExceptionMessage.NOT_METAFILE_SELECTED
        );
        metaFieldOverrideSerivce.selectedFields(
            new ValidatorFields(docgenBinding.getTargetField(), docgenBinding.getMetaField(), COLUMN_NAME, false),
            response,
            true
        );
        response.setValue(
            "typeData",
            Arrays
                .stream(TypeData.values())
                .filter(it -> it.getValue().equalsIgnoreCase(docgenBinding.getMetaField().getTypeName()))
                .findFirst()
                .orElse(TypeData.STANDARD)
                .getValue()
        );
    }

    /**
     * @param request
     * @param response
     */
    @Transactional(rollbackOn = { AxelorException.class, Exception.class })
    public void clear(ActionRequest request, ActionResponse response) {
        DocgenBinding docgenBinding = request.getContext().asType(DocgenBinding.class);
        metaFieldOverrideSerivce.clear(
            new ValidatorFields(docgenBinding.getTargetField(), docgenBinding.getMetaField(), COLUMN_NAME),
            response
        );
        response.setValue("query", "");
    }

    /**
     * @param request
     * @param response
     */
    public void getDomain(ActionRequest request, ActionResponse response) {
        DocgenBinding docgenBinding = request.getContext().asType(DocgenBinding.class);
        metaFieldOverrideSerivce.getDomain(
            new ValidatorFields(docgenBinding.getTargetField(), docgenBinding.getMetaField(), COLUMN_NAME),
            (MetaModel) request.getContext().getParent().get("metaModel"),
            response,
            false,
            RELATION_SHIP.toArray(new String[0])
        );
    }

    @Override
    public void checkFieldCorrectly(ActionRequest request, ActionResponse response) {
        DocgenBinding docgenBinding = request.getContext().asType(DocgenBinding.class);
        metaFieldOverrideSerivce.checkFieldCorrectly(docgenBinding.getMetaField(), response);
    }

    /**
     * @param request
     * @param response
     */
    public void onChangeTypeTemplate(ActionRequest request, ActionResponse response) {
        DocgenBinding docgenBinding = request.getContext().asType(DocgenBinding.class);
        clear(request, response);
        response.setValue(
            "$isValidate",
            Arrays.asList(TypeTemplate.QUERY, TypeTemplate.QUERY_NATIVE).contains(docgenBinding.getTypeTemplate())
        );
    }

    /**
     *
     * @param request
     * @param response
     */
    public void testQuery(ActionRequest request, ActionResponse response) {
        DocgenBinding docgenBinding = request.getContext().asType(DocgenBinding.class);

        String queryRequest = docgenBinding.getQuery();
        if(queryRequest.endsWith(";")) queryRequest = queryRequest.substring(queryRequest.length() - 1);

        Query query;
        if (docgenBinding.getTypeTemplate().equals(TypeTemplate.QUERY)) query =
            JPA.em().createQuery(queryRequest); else query = JPA.em().createNativeQuery(queryRequest);
        Object singleResult = query.setMaxResults(1).getSingleResult();
        response.setFlash(singleResult != null ? singleResult.toString() : "null");
    }
}
