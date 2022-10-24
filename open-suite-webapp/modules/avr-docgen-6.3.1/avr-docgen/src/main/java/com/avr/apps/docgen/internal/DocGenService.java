package com.avr.apps.docgen.internal;

import com.avr.apps.docgen.builder.JsonBuilder;
import com.avr.apps.docgen.common.HttpRequest;
import com.avr.apps.docgen.common.Mapper;
import com.avr.apps.docgen.common.function.ThreeParamsFunction;
import com.avr.apps.docgen.common.utils.ObjectUtils;
import com.avr.apps.docgen.exception.AvrException;
import com.avr.apps.docgen.exception.IExceptionMessage;
import com.avr.apps.docgen.utils.DocGenType;
import com.axelor.apps.base.db.AppDocgen;
import com.axelor.db.Model;
import com.axelor.db.Query;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.invoke.MethodHandles;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.function.Supplier;

/**
 * The type Doc gen.
 */
final class DocGenService {

    private final String filename;
    private final DocGenType docGenType;
    private final MetaFile model;
    private final String url;
    private final String action;
    private final Logger log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    private String codeLang = "fr";
    private Map<String, Object> root = new HashMap<>();
    private final List<JsonBuilder.MapCreator> childs = new ArrayList<>();
    private String certName = null;

    /**
     * Instantiates a new Doc gen.
     *
     * @param filename   the filename
     * @param docGenType the doc gen type
     * @param model      the model
     * @param url
     * @param action
     * @throws AvrException the avr exception
     */
    private DocGenService(String filename, DocGenType docGenType, MetaFile model, String url, String action) throws AvrException {
        this.url = url;
        this.action = action;
        if (ObjectUtils.isEmpty(model)) throw new AvrException(TraceBackRepository.CATEGORY_NO_VALUE, "Model template not found");
        log.info("Template using {}", model.getFileName());
        this.filename = filename;
        this.docGenType = docGenType;
        this.model = model;
    }

    /**
     * Instantiates a new Doc gen.
     *
     * @param filename   the filename
     * @param docGenType the doc gen type
     * @param model      the model
     * @param url
     * @param action
     * @param codeLang   the code lang
     * @throws AvrException the avr exception
     */
    private DocGenService(String filename, DocGenType docGenType, MetaFile model, String url, String action, String codeLang)
        throws AvrException {
        this.url = url;
        this.action = action;
        if (ObjectUtils.isEmpty(model)) throw new AvrException(TraceBackRepository.CATEGORY_NO_VALUE, "Model template not found");
        log.info("Template using {}", model.getFileName());
        this.filename = filename;
        this.docGenType = docGenType;
        this.model = model;
        this.codeLang = codeLang;
    }

    /**
     * Add root doc gen.
     *
     * @param key   the key
     * @param value the value
     * @return the doc gen
     */
    public DocGenService addRoot(String key, Object value) {
        value = ObjectUtils.elementOrDefault(value, "");
        this.root.put(key, value.toString());
        return this;
    }

    /**
     * Add root doc gen.
     *
     * @param model the model
     * @return the doc gen
     */
    public DocGenService addRoot(Model model) {
        this.root = Mapper.toMapWithoutChilds(model, codeLang);
        return this;
    }

    /**
     * Add root map doc gen.
     *
     * @param map the map
     * @return the doc gen
     */
    public DocGenService addRootMap(Map<String, Object> map) {
        for (Map.Entry<String, Object> m : map.entrySet()) {
            addRoot(m.getKey(), m.getValue());
        }
        return this;
    }

    /**
     * Add root doc gen.
     *
     * @param map the map
     * @return the doc gen
     */
    public DocGenService addRoot(Map<String, Supplier<Object>> map) {
        for (Map.Entry<String, Supplier<Object>> m : map.entrySet()) {
            addRoot(m.getKey(), m.getValue());
        }
        return this;
    }

    /**
     * Add root doc gen.
     *
     * @param key   the key
     * @param value the value
     * @return the doc gen
     */
    public DocGenService addRoot(String key, Supplier<Object> value) {
        this.root.put(key, ObjectUtils.elementOrDefault(ObjectUtils.eval(value, null), "").toString());
        return this;
    }

    /**
     * Add root doc gen.
     *
     * @param key   the key
     * @param value the value
     * @return the doc gen
     */
    public DocGenService addRoot(String key, BigDecimal value) {
        value = ObjectUtils.elementOrDefault(value, BigDecimal.ZERO).setScale(2, RoundingMode.HALF_UP);
        this.root.put(key, value.toString());
        return this;
    }

    /**
     * Add root doc gen.
     *
     * @param key   the key
     * @param value the value
     * @param scale the scale
     * @return the doc gen
     */
    public DocGenService addRoot(String key, BigDecimal value, int scale) {
        value = ObjectUtils.elementOrDefault(value, BigDecimal.ZERO).setScale(scale, RoundingMode.HALF_UP);
        this.root.put(key, value.toString());
        return this;
    }

    /**
     * Add childs doc gen.
     *
     * @param <U>  the type parameter
     * @param key  the key
     * @param list the list
     * @param fn   the fn
     * @return the doc gen
     */
    public <U> DocGenService addChilds(
        String key,
        List<U> list,
        ThreeParamsFunction<JsonBuilder.MapCreator, U, JsonBuilder.MapCreator> fn
    ) {
        addRoot(String.format("%sCount", key), list.size());
        JsonBuilder jsonBuilder = new JsonBuilder();
        this.childs.add(jsonBuilder.addArrayOfObjectToMapCreator(key, list, fn));
        return this;
    }

    /**
     * Add childs doc gen.
     *
     * @param <U>  the type parameter
     * @param key  the key
     * @param list the list
     * @param fn   the fn
     * @return the doc gen
     */
    public <U> DocGenService addChilds(
        String key,
        Collection<U> list,
        ThreeParamsFunction<JsonBuilder.MapCreator, U, JsonBuilder.MapCreator> fn
    ) {
        addRoot(String.format("%sCount", key), list.size());
        JsonBuilder jsonBuilder = new JsonBuilder();
        this.childs.add(jsonBuilder.addArrayOfObjectToMapCreator(key, list, fn));
        return this;
    }

    /**
     * Add childs doc gen.
     *
     * @param <U>  the type parameter
     * @param key  the key
     * @param list the list
     * @return the doc gen
     */
    public <U extends Model> DocGenService addChilds(String key, List<U> list) {
        addRoot(String.format("%s.count", key), list.size());
        JsonBuilder jsonBuilder = new JsonBuilder();
        this.childs.add(jsonBuilder.addArrayOfObjectToMapCreator(key, list, codeLang));
        return this;
    }

    /**
     * Add childs doc gen.
     *
     * @param <U> the type parameter
     * @param key the key
     * @param set the set
     * @param fn  the fn
     * @return the doc gen
     */
    public <U> DocGenService addChilds(
        String key,
        Set<U> set,
        ThreeParamsFunction<JsonBuilder.MapCreator, U, JsonBuilder.MapCreator> fn
    ) {
        addRoot(String.format("%sCount", key), set.size());
        JsonBuilder jsonBuilder = new JsonBuilder();
        this.childs.add(jsonBuilder.addArrayOfObjectToMapCreator(key, set, fn));
        return this;
    }

    /**
     * Sets cert name.
     *
     * @param certName the cert name
     */
    public void setCertName(String certName) {
        this.certName = certName;
    }

    /**
     * Generate file.
     *
     * @return the file
     * @throws Exception the exception
     */
    public File generate() throws Exception {
        if (root.size() == 0 && childs.size() == 0) throw new AvrException(
            TraceBackRepository.CATEGORY_NO_VALUE,
            "root or childs is required"
        );

        JsonBuilder jsonBuilder = new JsonBuilder().addObject("filename", filename).addObject("type", docGenType.getValue());

        if (certName != null) jsonBuilder.addObject("certName", certName);

        if (ObjectUtils.notEmpty(root)) {
            JsonBuilder.MapCreator mc = new JsonBuilder.MapCreator();
            for (Map.Entry<String, Object> entry : root.entrySet()) {
                mc.addObject(entry.getKey(), entry.getValue());
            }
            jsonBuilder.addObject("root", mc);
        }

        if (ObjectUtils.notEmpty(childs)) {
            jsonBuilder.addObject("childs", childs);
        }

        log.debug("send at {} with json {}", String.format("%s/%s", url, action), jsonBuilder.build());

        return new HttpRequest()
            .postMutipartFromData(
                String.format("%s/%s", url, action),
                String.format("%s.%s", filename, docGenType.getValue()),
                formData -> formData.addFile("model", MetaFiles.getPath(model).toFile()).addValue("json", jsonBuilder.build())
            );
    }

    public static DocGenService get(String filename, DocGenType docGenType, MetaFile model) throws AvrException {
        AppDocgen APP_DOCGEN = Query.of(AppDocgen.class).fetchOne();
        if (APP_DOCGEN == null) throw new AvrException(
            TraceBackRepository.CATEGORY_NO_VALUE,
            IExceptionMessage.NO_APP_DOCGEN_CONFIG_FOUND
        );
        String url = APP_DOCGEN.getUrl();
        String action = APP_DOCGEN.getAction();
        return new DocGenService(filename, docGenType, model, url, action);
    }

    /**
     * Transform bool object.
     *
     * @param value the value
     * @return the object
     */
    Object transformBool(Object value) {
        if (value instanceof Boolean) {
            return (Boolean) value ? "X" : "Y";
        }
        return value;
    }
}
