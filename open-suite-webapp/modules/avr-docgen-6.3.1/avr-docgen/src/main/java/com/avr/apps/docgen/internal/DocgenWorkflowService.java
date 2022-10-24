package com.avr.apps.docgen.internal;

import com.avr.apps.docgen.common.AvrExceptionBuilder;
import com.avr.apps.docgen.common.DateFormatter;
import com.avr.apps.docgen.common.I18n;
import com.avr.apps.docgen.common.utils.HtmlUtils;
import com.avr.apps.docgen.common.utils.ObjectUtils;
import com.avr.apps.docgen.db.*;
import com.avr.apps.docgen.exception.AvrException;
import com.avr.apps.docgen.repository.DocgenTemplateDocgenRepository;
import com.avr.apps.docgen.service.interfaces.TypeDataService;
import com.avr.apps.docgen.utils.DocGenType;
import com.axelor.apps.base.db.Partner;
import com.axelor.db.JPA;
import com.axelor.db.mapper.Mapper;
import com.axelor.db.mapper.Property;
import com.axelor.db.mapper.PropertyType;
import com.axelor.exception.db.repo.TraceBackRepository;
import com.axelor.meta.MetaFiles;
import com.axelor.meta.db.MetaField;
import com.axelor.meta.db.MetaFile;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import javax.persistence.Query;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The type Docgen template service.
 */
@Singleton
public final class DocgenWorkflowService {

    /**
     * The New line converter.
     */
    static final String NEW_LINE_CONVERTER = "/newline";
    private static final List<String> LIST_TYPE = Arrays.asList(ArrayList.class.getName(), List.class.getName());

    private Logger log = LoggerFactory.getLogger(DocgenWorkflowService.class);

    /**
     * The Docgen repository.
     */
    private final DocgenTemplateDocgenRepository docgenRepository;

    /**
     * The Type data service.
     */
    private final TypeDataService typeDataService;

    private final AvrExceptionBuilder errors = new AvrExceptionBuilder();

    @Inject
    public DocgenWorkflowService(DocgenTemplateDocgenRepository docgenRepository, TypeDataService typeDataService) {
        this.docgenRepository = docgenRepository;
        this.typeDataService = typeDataService;
    }

    /**
     * Gets docgen template.
     *
     * @param subType the sub type
     * @return the docgen template
     */
    public DocgenTemplate getDocgenTemplate(DocgenSubType subType) {
        return docgenRepository.findTemplateReport(subType);
    }

    /**
     * Computed meta file language meta file.
     *
     * @param docgenTemplate the docgen template
     * @param partner        the partner
     * @return the meta file
     * @throws AvrException the avr exception
     */
    public MetaFile computedMetaFileLanguage(DocgenTemplate docgenTemplate, Partner partner) throws AvrException {
        MetaFile metaFile = docgenTemplate
                .getModelTemplateList()
                .stream()
                .filter(it -> it.getLanguage().equals(partner.getLanguage()))
                .findFirst()
                .map(DocgenModel::getMetaFile)
                .orElse(docgenTemplate.getModelDefault());
        if (ObjectUtils.isEmpty(metaFile)) {
            throw new AvrException(TraceBackRepository.CATEGORY_NO_VALUE, "Model is not found");
        }
        return metaFile;
    }

    /**
     * Generate document file.
     *
     * @param bean           the bean
     * @param partner        the partner
     * @param sequence       the sequence
     * @param metaFile       the meta file
     * @param docgenTemplate the docgen template
     * @param docGenType     the doc gen type
     * @return the file
     * @throws Exception the exception
     */
    public File generateDocument(
            Object bean,
            Partner partner,
            String sequence,
            MetaFile metaFile,
            DocgenTemplate docgenTemplate,
            DocGenType docGenType
    ) throws Exception {
        DocGenService docGen = DocGenService.get(generateTitle(docgenTemplate, partner, sequence), docGenType, metaFile);
        computedRootDataBinding(docGen, bean, partner, docgenTemplate.getDocgenBindingRootList());
        computedChildDataBinding(docGen, bean, partner, docgenTemplate.getDocgenBindingChildList());
        return docGen.generate();
    }

    /**
     * Generate title string.
     *
     * @param docgenTemplate the docgen template
     * @param partner        the partner
     * @param sequence       the sequence
     * @return the string
     */
    public String generateTitle(DocgenTemplate docgenTemplate, Partner partner, String sequence) {
        String patternTitle = docgenTemplate.getPatternTitle();
        LocalDateTime now = LocalDateTime.now();
        String lang = ObjectUtils.eval(() -> partner.getLanguage().getCode(), null);
        return validateTitle(
                patternTitle
                        .replace("%TR", I18n.get(docgenTemplate.getName(), lang))
                        .replace("%CN", I18n.get(ObjectUtils.eval(() -> partner.getName(), ""), lang))
                        .replace("%CFN", I18n.get(ObjectUtils.eval(() -> partner.getFullName(), ""), lang))
                        .replace("%S", ObjectUtils.eval(() -> sequence, ""))
                        .replace("%D", String.valueOf(now.getDayOfMonth()))
                        .replace(
                                "%M",
                                now.getMonthValue() < 10 ? String.format("0%s", now.getMonthValue()) : String.valueOf(now.getMonthValue())
                        )
                        .replace("%Y", String.valueOf(now.getYear()))
                        .replace("%h", String.valueOf(now.getHour()))
                        .replace("%m", String.valueOf(now.getMinute()))
                        .replace("%s", String.valueOf(now.getSecond()))
        );
    }

    /**
     * Validate title string.
     *
     * @param title the title
     * @return the string
     */
    public String validateTitle(String title) {
        return title
                .replace('/', '-')
                .replace('\\', '-')
                .replace('<', ' ')
                .replace('>', ' ')
                .replace('"', ' ')
                .replace('|', ' ')
                .replace('*', ' ')
                .replace(':', ' ');
    }

    private void computedChildDataBinding(
            DocGenService docGen,
            Object bean,
            Partner partner,
            List<DocgenChildBinding> docgenChildBindingList
    ) {
        docgenChildBindingList.forEach(
                docgenChildBinding -> {
                    Collection<?> dataCollection;
                    if (docgenChildBinding.getTypeTemplate().equals(TypeTemplate.FIELD)) {
                        dataCollection = getDataCollection(bean, docgenChildBinding);
                    } else {
                        dataCollection = getQuery(bean, docgenChildBinding);
                    }

                    docGen.addRoot(String.format("%sCount", docgenChildBinding.getKeyBinding()), dataCollection.size());
                    docGen.addChilds(
                            docgenChildBinding.getKeyBinding(),
                            dataCollection,
                            (mapCreator, subBean) -> {
                                docgenChildBinding
                                        .getDocgenBindingList()
                                        .forEach(
                                                docgenBinding -> {
                                                    if (docgenBinding.getTypeTemplate().equals(TypeTemplate.FIELD)) {
                                                        mapCreator.addObject(
                                                                typeDataService.getKeyByType(
                                                                        docgenBinding.getKeyBinding(),
                                                                        docgenBinding.getTypeData()
                                                                ),
                                                                () -> {
                                                                    Pair<Object, Property> data = getData(subBean, docgenBinding.getTargetField());
                                                                    return validateData(
                                                                            data.getKey(),
                                                                            docgenBinding,
                                                                            ObjectUtils.eval(() -> partner.getLanguage().getCode(), "fr"),
                                                                            data.getValue().isTranslatable()
                                                                    );
                                                                }
                                                        );
                                                    } else {
                                                        mapCreator.addObject(
                                                                typeDataService.getKeyByType(
                                                                        docgenBinding.getKeyBinding(),
                                                                        docgenBinding.getTypeData()
                                                                ),
                                                                () ->
                                                                        getDataFromQuery(
                                                                                subBean,
                                                                                docgenBinding.getQuery(),
                                                                                docgenBinding,
                                                                                ObjectUtils.eval(() -> partner.getLanguage().getCode(), "fr")
                                                                        )
                                                        );
                                                    }
                                                }
                                        );
                                return mapCreator;
                            }
                    );
                }
        );
    }

    private void computedRootDataBinding(
            DocGenService docGen,
            Object bean,
            Partner partner,
            List<DocgenBinding> docgenRootBindingList
    ) {
        docgenRootBindingList.forEach(
                docgenBinding -> computedTypeTemplateAndIncrementedData(docGen, bean, partner, docgenBinding)
        );
    }

    private void computedTypeTemplateAndIncrementedData(
            DocGenService docGen,
            Object bean,
            Partner partner,
            DocgenBinding docgenBinding
    ) {
        if (docgenBinding.getTypeTemplate().equals(TypeTemplate.FIELD)) {
            log.debug("Bean start with date {}", bean);
            Pair<Object, Property> data = getData(bean, docgenBinding.getTargetField());
            docGen.addRoot(
                    typeDataService.getKeyByType(docgenBinding.getKeyBinding(), docgenBinding.getTypeData()),
                    validateData(
                            data.getKey(),
                            docgenBinding,
                            ObjectUtils.eval(() -> partner.getLanguage().getCode(), "fr"),
                            data.getValue().isTranslatable()
                    )
            );
        } else {
            docGen.addRoot(
                    typeDataService.getKeyByType(docgenBinding.getKeyBinding(), docgenBinding.getTypeData()),
                    getDataFromQuery(
                            bean,
                            docgenBinding.getQuery(),
                            docgenBinding,
                            ObjectUtils.eval(() -> partner.getLanguage().getCode(), "fr")
                    )
            );
        }
    }

    private Object validateData(Object value, DocgenBinding docgenBinding, String codeLang, boolean isTranslatable) {
        if (docgenBinding.getTypeData().equals(TypeData.STANDARD)) {
            final Object val = joiningResultIfNecessary(
                    docgenBinding.getIsJoiningResult(),
                    docgenBinding.getJoiningSeparator(),
                    value
            );
            if (isTranslatable) {
                return ObjectUtils.eval(
                        () -> I18n.get(HtmlUtils.html2text(escaperData(val)).replace(NEW_LINE_CONVERTER, "\n"), codeLang),
                        null
                );
            }
            return ObjectUtils.eval(() -> HtmlUtils.html2text(escaperData(val)).replace(NEW_LINE_CONVERTER, "\n"), null);
        } else if (docgenBinding.getTypeData().equals(TypeData.LOCALDATE)) {
            log.debug("date before transform {}", value);
            String dt = DateFormatter.transformDate((LocalDate) value, codeLang);
            log.debug("date after transform {}", dt);
            return ObjectUtils.eval(() -> dt, null);
        } else if (docgenBinding.getTypeData().equals(TypeData.BIGDECIMAL)) {
            return ObjectUtils.eval(
                    () -> ((BigDecimal) value).setScale(docgenBinding.getBigDecimalScale(), RoundingMode.HALF_UP).toString(),
                    null
            );
        } else if (docgenBinding.getTypeData().equals(TypeData.PICTURE)) {
            try {
                if (value == null) return null;
                if (value.getClass().getTypeName().equals(byte.class.getName())) {
                    return new String((byte[]) value, StandardCharsets.US_ASCII);
                } else {
                    Path path = MetaFiles.getPath(value.toString());
                    byte[] encoded = Base64.encodeBase64(FileUtils.readFileToByteArray(path.toFile()));
                    return new String(encoded, StandardCharsets.US_ASCII);
                }
            } catch (IOException e) {
                throw new NullPointerException(e.getMessage());
            }
        } else if (docgenBinding.getTypeData().equals(TypeData.HTML)) {
            return htmlReturnObjectWithConditionTranslable(isTranslatable, value.toString(), codeLang, value.toString());
        } else if (docgenBinding.getTypeData().equals(TypeData.HTML_IMAGE)) {
            return htmlReturnObjectWithConditionTranslable(isTranslatable, value.toString(), codeLang, value.toString());
        } else {
            if (docgenBinding.getHasDateOnlyReturning()) return ObjectUtils.eval(
                    () -> DateFormatter.transformDate(((LocalDateTime) value).toLocalDate(), codeLang),
                    null
            ); else return ObjectUtils.eval(() -> DateFormatter.transformDate((LocalDateTime) value, codeLang), null);
        }
    }

    private String htmlReturnObjectWithConditionTranslable(boolean isTranslatable, String value, String codeLang, String value1) {
        if (isTranslatable) {
            return ObjectUtils.eval(() -> I18n.get(value, codeLang), null);
        }
        return ObjectUtils.eval(() -> value1, null);
    }

    @SuppressWarnings("unchecked")
    private Object joiningResultIfNecessary(Boolean isJoiningResult, String separator, Object value) {
        if (!isJoiningResult || !LIST_TYPE.contains(value.getClass().getTypeName())) return value;
        return String.join(separator.replace('_', ' ').replace(NEW_LINE_CONVERTER, "\n"), (List<String>) value);
    }

    private Object getDataFromQuery(Object bean, String query, DocgenBinding docgenBinding, String codeLang) {
        Query q = null;
        try {
            q = generateQuery(bean, query, docgenBinding.getTypeTemplate());
            Preconditions.checkNotNull(q, String.format("query is null : %s", q));
            Object result = getOneOrMany(q, docgenBinding);
            if (ObjectUtils.isEmpty(result)) return null;
            return validateData(result, docgenBinding, codeLang, true);
        } catch (Exception e) {
            String arguments = "";
            if (q != null) {
                final Query qTmp = q;
                arguments =
                        q
                                .getParameters()
                                .stream()
                                .map(it -> String.format("%s : %s", it.getName(), qTmp.getParameterValue(it)))
                                .collect(Collectors.joining(","));
            }

            if (
                    e.getCause() != null &&
                            e.getCause().getCause() != null &&
                            !Objects.equals(e.getCause().getCause().getMessage(), e.getMessage())
            ) errors.add(
                    "%s - %s \"%s\" with argument { %s }",
                    e.getMessage(),
                    e.getCause().getCause().getMessage(),
                    query,
                    arguments
            ); else errors.add("%s \"%s\"", e.getMessage(), query);
            return "";
        }
    }

    private Object getOneOrMany(Query q, DocgenBinding docgenBinding) {
        if (docgenBinding.getTypeData().equals(TypeData.STANDARD) && docgenBinding.getIsJoiningResult()) {
            return q.getResultList();
        } else {
            return q.setMaxResults(1).getSingleResult();
        }
    }

    private Pair<Object, Property> getData(Object bean, String fields) {
        String[] fds = fields.split(Pattern.quote("."));
        Object value = bean;
        Property propertyValue = null;
        for (String field : fds) {
            if (ObjectUtils.isEmpty(value)) {
                value = null;
                break;
            }
            Mapper mapper = Mapper.of(value.getClass());
            Preconditions.checkNotNull(mapper, String.format("mapper empty for class %s", value.getClass().getName()));
            Property property = mapper.getProperty(field);
            Preconditions.checkNotNull(property, String.format("property empty for field %s", field));
            value = property.get(value);
            propertyValue = property;
        }

        log.debug("field '{}' has value '{}'", fields, value);
        return Pair.of(value, propertyValue);
    }

    private Collection<?> getDataCollection(Object bean, DocgenChildBinding docgenChildBinding) {
        String fields = docgenChildBinding.getTargetField();
        boolean isOrder = docgenChildBinding.getIsOrderData();
        boolean isAscending = docgenChildBinding.getIsAscending();
        MetaField metaFileFilter = docgenChildBinding.getMetaFieldOrder();
        String[] fds = fields.split(Pattern.quote("."));
        Object value = bean;
        try {
            for (String field : fds) {
                Mapper mapper = Mapper.of(value.getClass());
                Preconditions.checkArgument(
                        mapper != null,
                        String.format("mapper empty for class %s", value.getClass().getName())
                );
                value = mapper.get(value, field);
            }

            if (isOrder && ObjectUtils.notEmpty(value)) {
                Object firstElement = ((Collection<?>) value).iterator().next();
                Mapper mapper = Mapper.of(firstElement.getClass());
                Property property = mapper.getProperty(metaFileFilter.getName());

                Comparator<Object> comparator;
                if (!isAscending) {
                    comparator = (o1, o2) -> comparator(property, o1, o2);
                } else {
                    comparator = (o1, o2) -> comparator(property, o2, o1);
                }

                return ((Collection<?>) value).stream().sorted(comparator).collect(Collectors.toList());
            }

            return (Collection<?>) value;
        } catch (Exception e) {
            throw new NullPointerException(e.getMessage());
        }
    }

    private Collection<?> getQuery(Object bean, DocgenChildBinding docgenChildBinding) {
        try {
            Class<?> aClass = Class.forName(docgenChildBinding.getMetaModel().getFullName());
            Query query = generateQuery(bean, docgenChildBinding.getQuery(), aClass, docgenChildBinding.getTypeTemplate());
            Preconditions.checkNotNull(query, "query is null");
            return query.getResultList();
        } catch (Exception e) {
            throw new NullPointerException(e.getMessage());
        }
    }

    private Query generateQuery(Object bean, String query, TypeTemplate typeTemplate) {
        return generateQuery(bean, query, null, typeTemplate);
    }

    private Query generateQuery(Object bean, String query, Class<?> clazz, TypeTemplate typeTemplate) {
        query = inlineQuery(query);
        List<String> groups = findParameter(query);
        for (String group : groups) {
            query = query.replace(group, group.replace(".", "_"));
        }

        Query q = createQuery(query, clazz, typeTemplate);

        for (String group : groups) {
            Object value;
            value = getData(bean, group.substring(1));
            if (value == null) {
                throw new NullPointerException(String.format("value empty for %s for query %s", String.join(".", groups), query));
            }
            q.setParameter(group.substring(1).replace(".", "_"), ((ImmutablePair) value).getKey() );
        }

        return q;
    }

    private List<String> findParameter(String query) {
        List<String> groups = new ArrayList<>();
        Pattern pattern = Pattern.compile("(:\\w[^ |,|\\n|;|)]*)", Pattern.MULTILINE);
        Matcher matcher = pattern.matcher(query);
        while (matcher.find()) {
            for (int i = 1; i <= matcher.groupCount(); i++) {
                groups.add(matcher.group(i));
            }
        }
        return groups;
    }

    private String inlineQuery(String query) {
        return query
                .replaceAll("E'\\n'", NEW_LINE_CONVERTER)
                .replaceAll("\\n", " ")
                .replaceAll("\\r", " ")
                .replaceAll(";$", "")
                .replaceAll(NEW_LINE_CONVERTER, "E'\\n'");
    }

    private boolean isNumbericWithScale(PropertyType propertyType) {
        return propertyType.equals(PropertyType.DECIMAL) || propertyType.equals(PropertyType.DOUBLE);
    }

    private Query createQuery(String query, Class<?> clazz, TypeTemplate typeTemplate) {
        Query q;
        if (typeTemplate.equals(TypeTemplate.QUERY)) {
            if (clazz == null) {
                q = JPA.em().createQuery(query);
            } else {
                q = JPA.em().createQuery(query, clazz);
            }
        } else if (typeTemplate.equals(TypeTemplate.QUERY_NATIVE)) {
            if (clazz == null) {
                q = JPA.em().createNativeQuery(query);
            } else {
                q = JPA.em().createNativeQuery(query, clazz);
            }
        } else {
            throw new NullPointerException(String.format("type de template %s unknown"));
        }
        return q;
    }

    private String escaperData(Object value) {
        String val = value.toString();
        val = val.replace("\n", NEW_LINE_CONVERTER);
        val = val.replace("\\n", NEW_LINE_CONVERTER);
        val = val.replace("\"", "''");
        return val;
    }

    private int comparator(Property property, Object o1, Object o2) {
        Object field1 = property.get(o1);
        Object field2 = property.get(o2);
        if (isNumbericWithScale(property.getType())) {
            return new BigDecimal(field2.toString()).compareTo(new BigDecimal(field1.toString()));
        } else if (property.getType().equals(PropertyType.INTEGER)) {
            return new Integer(field2.toString()).compareTo(new Integer(field1.toString()));
        } else {
            return field2.toString().compareTo(field1.toString());
        }
    }
}
