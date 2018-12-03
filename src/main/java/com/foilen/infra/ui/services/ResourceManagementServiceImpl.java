/*
    Foilen Infra UI
    https://github.com/foilen/foilen-infra-ui
    Copyright (c) 2017-2018 Foilen (http://foilen.com)

    The MIT License
    http://opensource.org/licenses/MIT

 */
package com.foilen.infra.ui.services;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.foilen.infra.plugin.core.system.common.changeexecution.ChangeExecutionLogic;
import com.foilen.infra.plugin.v1.core.context.ChangesContext;
import com.foilen.infra.plugin.v1.core.context.CommonServicesContext;
import com.foilen.infra.plugin.v1.core.context.internal.InternalServicesContext;
import com.foilen.infra.plugin.v1.core.exception.ResourceNotFromRepositoryException;
import com.foilen.infra.plugin.v1.core.resource.IPResourceDefinition;
import com.foilen.infra.plugin.v1.core.resource.IPResourceQuery;
import com.foilen.infra.plugin.v1.core.service.IPResourceService;
import com.foilen.infra.plugin.v1.core.service.internal.InternalChangeService;
import com.foilen.infra.plugin.v1.core.service.internal.InternalIPResourceService;
import com.foilen.infra.plugin.v1.model.resource.IPResource;
import com.foilen.infra.ui.InfraUiException;
import com.foilen.infra.ui.db.dao.PluginResourceColumnSearchDao;
import com.foilen.infra.ui.db.dao.PluginResourceDao;
import com.foilen.infra.ui.db.dao.PluginResourceLinkDao;
import com.foilen.infra.ui.db.dao.PluginResourceTagDao;
import com.foilen.infra.ui.db.domain.plugin.PluginResource;
import com.foilen.infra.ui.db.domain.plugin.PluginResourceColumnSearch;
import com.foilen.infra.ui.db.domain.plugin.PluginResourceLink;
import com.foilen.infra.ui.db.domain.plugin.PluginResourceTag;
import com.foilen.infra.ui.services.hook.AuditingChangeExecutionHook;
import com.foilen.infra.ui.services.hook.ReportingChangeExecutionHook;
import com.foilen.infra.ui.services.hook.UserDetailsChangeExecutionHook;
import com.foilen.mvc.ui.UiException;
import com.foilen.smalltools.exception.SmallToolsException;
import com.foilen.smalltools.reflection.ReflectionTools;
import com.foilen.smalltools.tools.AbstractBasics;
import com.foilen.smalltools.tools.AssertTools;
import com.foilen.smalltools.tools.JsonTools;
import com.foilen.smalltools.tuple.Tuple2;
import com.foilen.smalltools.tuple.Tuple3;
import com.google.common.base.Joiner;

@Service
@Transactional
public class ResourceManagementServiceImpl extends AbstractBasics implements InternalChangeService, InternalIPResourceService, IPResourceService, ResourceManagementService {

    @Autowired
    private AuditingService auditingService;
    @Autowired
    private CommonServicesContext commonServicesContext;
    @Autowired
    private InternalServicesContext internalServicesContext;
    @Autowired
    private IPResourceService ipResourceService;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private PluginResourceColumnSearchDao pluginResourceColumnSearchDao;
    @Autowired
    private PluginResourceDao pluginResourceDao;
    @Autowired
    private PluginResourceLinkDao pluginResourceLinkDao;
    @Autowired
    private PluginResourceTagDao pluginResourceTagDao;
    @Autowired
    private ReportService reportService;

    private Map<Class<? extends IPResource>, List<Class<?>>> allClassesByResourceClass = new HashMap<>();
    private Map<Class<? extends IPResource>, IPResourceDefinition> resourceDefinitionByResourceClass = new HashMap<>();
    private Map<String, IPResourceDefinition> resourceDefinitionByResourceType = new HashMap<>();

    @Value("${infraUi.infiniteLoopTimeoutInMs}")
    private long infiniteLoopTimeoutInMs = 120000;

    @SuppressWarnings("rawtypes")
    protected void addColumnSearch(List<PluginResourceColumnSearch> columnSearches, PluginResource pluginResource, String propertyName, Class<?> propertyType, Object propertyValue) {
        PluginResourceColumnSearch columnSearch = new PluginResourceColumnSearch(pluginResource, propertyName);
        if (propertyValue == null) {
            // Set nothing
        } else if (Boolean.class.isAssignableFrom(propertyType) || propertyType.equals(boolean.class)) {
            columnSearch.setBool((boolean) propertyValue);
        } else if (Date.class.isAssignableFrom(propertyType)) {
            columnSearch.setLongNumber(((Date) propertyValue).getTime());
        } else if (Double.class.isAssignableFrom(propertyType) || propertyType.equals(double.class)) {
            columnSearch.setDoubleNumber((Double) propertyValue);
        } else if (Float.class.isAssignableFrom(propertyType) || propertyType.equals(float.class)) {
            columnSearch.setFloatNumber((Float) propertyValue);
        } else if (propertyType.isEnum()) {
            columnSearch.setIntNumber(((Enum) propertyValue).ordinal());
        } else if (Integer.class.isAssignableFrom(propertyType) || propertyType.equals(int.class)) {
            columnSearch.setIntNumber((Integer) propertyValue);
        } else if (Long.class.isAssignableFrom(propertyType) || propertyType.equals(long.class)) {
            columnSearch.setLongNumber((Long) propertyValue);
        } else if (String.class.isAssignableFrom(propertyType)) {
            columnSearch.setText((String) propertyValue);
        } else {
            logger.error("Insertion: Unknown column type {} for type {} and property {}", propertyType.getName(), pluginResource.getType(), propertyName);
            return;
        }
        columnSearches.add(columnSearch);
    }

    @Override
    public void changesExecute(ChangesContext changes) {

        ChangeExecutionLogic changeExecutionLogic = new ChangeExecutionLogic(commonServicesContext, internalServicesContext);
        changeExecutionLogic.setInfiniteLoopTimeoutInMs(infiniteLoopTimeoutInMs);
        changeExecutionLogic.addHook(new UserDetailsChangeExecutionHook());
        changeExecutionLogic.addHook(new ReportingChangeExecutionHook(reportService));
        changeExecutionLogic.addHook(new AuditingChangeExecutionHook(auditingService));
        changeExecutionLogic.execute(changes);

    }

    @Override
    public <T extends IPResource> IPResourceQuery<T> createResourceQuery(Class<T> resourceClass) {
        List<IPResourceDefinition> resourceDefinitions = getResourceDefinitions(resourceClass);

        if (resourceDefinitions.isEmpty()) {
            throw new SmallToolsException("Resource class " + resourceClass.getName() + " is unknown");
        }

        return new IPResourceQuery<>(resourceDefinitions);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T extends IPResource> IPResourceQuery<T> createResourceQuery(String resourceType) {
        IPResourceDefinition resourceDefinition = resourceDefinitionByResourceType.get(resourceType);
        if (resourceDefinition == null) {
            throw new SmallToolsException("Resource type " + resourceType + " is unknown");
        }
        return (IPResourceQuery<T>) createResourceQuery(resourceDefinition.getResourceClass());
    }

    private String generateInParameters(int size) {
        String sql = "(";

        for (int i = 0; i < size; ++i) {
            if (i != 0) {
                sql += ",";
            }
            sql += "?";
        }

        sql += ")";
        return sql;
    }

    public long getInfiniteLoopTimeoutInMs() {
        return infiniteLoopTimeoutInMs;
    }

    @Override
    public IPResourceDefinition getResourceDefinition(Class<? extends IPResource> resourceClass) {
        return resourceDefinitionByResourceClass.get(resourceClass);
    }

    @Override
    public IPResourceDefinition getResourceDefinition(IPResource resource) {
        return resourceDefinitionByResourceClass.get(resource.getClass());
    }

    @Override
    public IPResourceDefinition getResourceDefinition(String resourceType) {
        return resourceDefinitionByResourceType.get(resourceType);
    }

    @Override
    public List<IPResourceDefinition> getResourceDefinitions() {
        return Collections.unmodifiableList(resourceDefinitionByResourceType.values().stream().collect(Collectors.toList()));
    }

    protected List<IPResourceDefinition> getResourceDefinitions(Class<? extends IPResource> resourceClass) {
        return allClassesByResourceClass.entrySet().stream() //
                .filter(it -> it.getValue().contains(resourceClass)) //
                .map(it -> resourceDefinitionByResourceClass.get(it.getKey())) //
                .filter(it -> it != null) //
                .collect(Collectors.toList());

    }

    @Override
    public void linkAdd(long fromResourceId, String linkType, long toResourceId) {
        PluginResource fromPluginResource = pluginResourceDao.findOne(fromResourceId);
        PluginResource toPluginResource = pluginResourceDao.findOne(toResourceId);
        pluginResourceLinkDao.save(new PluginResourceLink(fromPluginResource, linkType, toPluginResource));
    }

    @Override
    public boolean linkDelete(long fromResourceId, String linkType, long toResourceId) {
        return pluginResourceLinkDao.deleteByFromPluginResourceIdAndLinkTypeAndToPluginResourceId(fromResourceId, linkType, toResourceId) > 0;
    }

    @Override
    public boolean linkExists(long fromResourceId, String linkType, long toResourceId) {
        return pluginResourceLinkDao.findByFromPluginResourceIdAndLinkTypeAndToPluginResourceId(fromResourceId, linkType, toResourceId) != null;
    }

    @Override
    public boolean linkExistsByFromResourceAndLinkTypeAndToResource(IPResource fromResource, String linkType, IPResource toResource) {
        return pluginResourceLinkDao.countByFromPluginResourceIdAndLinkTypeAndToPluginResourceId(fromResource.getInternalId(), linkType, toResource.getInternalId()) > 0;
    }

    @Override
    public List<Tuple2<String, ? extends IPResource>> linkFindAllByFromResource(IPResource fromResource) {
        if (fromResource.getInternalId() == null) {
            throw new ResourceNotFromRepositoryException(fromResource);
        }
        return pluginResourceLinkDao.findAllByFromPluginResourceId(fromResource.getInternalId()).stream() //
                .map(it -> new Tuple2<>(it.getLinkType(), loadResource(it.getToPluginResource()))) //
                .collect(Collectors.toList());
    }

    @Override
    public List<Tuple2<String, ? extends IPResource>> linkFindAllByFromResource(long fromResourceId) {
        return pluginResourceLinkDao.findAllByFromPluginResourceId(fromResourceId).stream() //
                .map(it -> new Tuple2<>(it.getLinkType(), loadResource(it.getToPluginResource()))) //
                .collect(Collectors.toList());
    }

    @Override
    public List<? extends IPResource> linkFindAllByFromResourceAndLinkType(IPResource fromResource, String linkType) {
        if (fromResource.getInternalId() == null) {
            throw new ResourceNotFromRepositoryException(fromResource);
        }
        return pluginResourceLinkDao.findAllByFromPluginResourceIdAndLinkType(fromResource.getInternalId(), linkType).stream() //
                .map(it -> loadResource(it.getToPluginResource())) //
                .collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    @Override
    public <R extends IPResource> List<R> linkFindAllByFromResourceAndLinkTypeAndToResourceClass(IPResource fromResource, String linkType, Class<R> toResourceClass) {
        if (fromResource.getInternalId() == null) {
            throw new ResourceNotFromRepositoryException(fromResource);
        }
        List<IPResourceDefinition> ipResourceDefinitions = getResourceDefinitions(toResourceClass);
        List<String> toResourceTypes = ipResourceDefinitions.stream().map(it -> it.getResourceType()).collect(Collectors.toList());
        return pluginResourceLinkDao.findAllByFromPluginResourceIdAndLinkTypeAndToPluginResourceTypeIn(fromResource.getInternalId(), linkType, toResourceTypes).stream() //
                .map(it -> (R) loadResource(it.getToPluginResource())) //
                .collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    @Override
    public <R extends IPResource> List<R> linkFindAllByFromResourceClassAndLinkTypeAndToResource(Class<R> fromResourceClass, String linkType, IPResource toResource) {
        if (toResource.getInternalId() == null) {
            throw new ResourceNotFromRepositoryException(toResource);
        }
        List<IPResourceDefinition> ipResourceDefinitions = getResourceDefinitions(fromResourceClass);
        List<String> fromResourceTypes = ipResourceDefinitions.stream().map(it -> it.getResourceType()).collect(Collectors.toList());
        return pluginResourceLinkDao.findAllByFromPluginResourceTypeInAndLinkTypeAndToPluginResourceId(fromResourceTypes, linkType, toResource.getInternalId()).stream() //
                .map(it -> (R) loadResource(it.getFromPluginResource())) //
                .collect(Collectors.toList());
    }

    @Override
    public List<? extends IPResource> linkFindAllByLinkTypeAndToResource(String linkType, IPResource toResource) {
        if (toResource.getInternalId() == null) {
            throw new ResourceNotFromRepositoryException(toResource);
        }
        return pluginResourceLinkDao.findAllByLinkTypeAndToPluginResourceId(linkType, toResource.getInternalId()).stream() //
                .map(it -> loadResource(it.getFromPluginResource())) //
                .collect(Collectors.toList());
    }

    @Override
    public List<Tuple2<? extends IPResource, String>> linkFindAllByToResource(IPResource toResource) {
        if (toResource.getInternalId() == null) {
            throw new ResourceNotFromRepositoryException(toResource);
        }
        return pluginResourceLinkDao.findAllByToPluginResourceId(toResource.getInternalId()).stream() //
                .map(it -> new Tuple2<>(loadResource(it.getFromPluginResource()), it.getLinkType())) //
                .collect(Collectors.toList());
    }

    @Override
    public List<Tuple2<? extends IPResource, String>> linkFindAllByToResource(long toResourceId) {
        return pluginResourceLinkDao.findAllByToPluginResourceId(toResourceId).stream() //
                .map(it -> new Tuple2<>(loadResource(it.getFromPluginResource()), it.getLinkType())) //
                .collect(Collectors.toList());
    }

    @Override
    public List<Tuple3<IPResource, String, IPResource>> linkFindAllRelatedByResource(IPResource resource) {
        if (resource.getInternalId() == null) {
            throw new ResourceNotFromRepositoryException(resource);
        }
        return pluginResourceLinkDao.findAllByPluginResourceId(resource.getInternalId()).stream() //
                .map(it -> new Tuple3<>(loadResource(it.getFromPluginResource()), it.getLinkType(), loadResource(it.getToPluginResource()))) //
                .collect(Collectors.toList());
    }

    @Override
    public List<Tuple3<IPResource, String, IPResource>> linkFindAllRelatedByResource(Long internalResourceId) {
        return pluginResourceLinkDao.findAllByPluginResourceId(internalResourceId).stream() //
                .map(it -> new Tuple3<>(loadResource(it.getFromPluginResource()), it.getLinkType(), loadResource(it.getToPluginResource()))) //
                .collect(Collectors.toList());
    }

    protected IPResource loadResource(PluginResource pluginResource) {
        IPResourceDefinition resourceDefinition = getResourceDefinition(pluginResource.getType());
        if (resourceDefinition == null) {
            throw new UiException("Unknown resource definition for type " + pluginResource.getType());
        }
        return pluginResource.loadResource(resourceDefinition //
                .getResourceClass());
    }

    @Override
    public IPResource resourceAdd(IPResource resource) {
        String resourceType = ipResourceService.getResourceDefinition(resource).getResourceType();
        PluginResource pluginResource = pluginResourceDao.save(new PluginResource(resourceType, resource));
        IPResource finalResource = pluginResource.loadResource(resource.getClass());
        updateColumnSearches(finalResource);
        return finalResource;
    }

    @Override
    public void resourceAdd(IPResourceDefinition resourceDefinition) {
        resourceDefinitionByResourceClass.put(resourceDefinition.getResourceClass(), resourceDefinition);
        resourceDefinitionByResourceType.put(resourceDefinition.getResourceType(), resourceDefinition);

        allClassesByResourceClass.put(resourceDefinition.getResourceClass(), ReflectionTools.allTypes(resourceDefinition.getResourceClass()));
    }

    @Override
    public boolean resourceDelete(long resourceId) {
        pluginResourceColumnSearchDao.deleteByPluginResourceId(resourceId);
        pluginResourceTagDao.deleteByPluginResourceId(resourceId);
        pluginResourceLinkDao.deleteByPluginResourceId(resourceId);
        return pluginResourceDao.deleteById(resourceId) > 0;
    }

    @Override
    public <R extends IPResource, T extends IPResource> boolean resourceEqualsPk(R a, T b) {
        // nulls
        if (a == null) {
            return b == null;
        }
        if (b == null) {
            return false;
        }

        // Type
        Class<? extends IPResource> aClass = a.getClass();
        Class<? extends IPResource> bClass = b.getClass();
        if (!aClass.equals(bClass)) {
            return false;
        }

        // PK
        IPResourceDefinition resourceDefinition = getResourceDefinition(aClass);
        if (resourceDefinition == null) {
            return false;
        }
        for (String pkName : resourceDefinition.getPrimaryKeyProperties()) {
            Method pkGetter = resourceDefinition.getPropertyGetterMethod(pkName);
            Object aValue;
            Object bValue;
            try {
                aValue = pkGetter.invoke(a);
                bValue = pkGetter.invoke(b);
            } catch (Exception e) {
                return false;
            }

            // Nulls
            if (aValue == null) {
                if (bValue != null) {
                    return false;
                }
            } else {
                if (!aValue.equals(bValue)) {
                    return false;
                }
            }
        }

        return true;

    }

    @Override
    public <T extends IPResource> Optional<T> resourceFind(IPResourceQuery<T> query) {
        List<T> results = resourceFindAll(query);
        if (results.isEmpty()) {
            return Optional.empty();
        }
        AssertTools.assertTrue(results.size() <= 1, "There are more than one item matching the query");
        return Optional.of(results.get(0));
    }

    @Override
    public Optional<IPResource> resourceFind(long internalResourceId) {
        PluginResource pluginResource = pluginResourceDao.findOne(internalResourceId);
        if (pluginResource == null) {
            return Optional.empty();
        }
        return Optional.of(loadResource(pluginResource));
    }

    @Override
    public List<? extends IPResource> resourceFindAll() {
        return pluginResourceDao.findAll().stream() //
                .map(it -> loadResource(it)) //
                .collect(Collectors.toList());
    }

    @Override
    public <T extends IPResource> List<T> resourceFindAll(IPResourceQuery<T> query) {

        List<IPResourceDefinition> resourceDefinitions = query.getResourceDefinitions();

        String selectSql = "SELECT DISTINCT r.id, r.type, r.value_json";
        StringBuilder fromSql = new StringBuilder(" FROM plugin_resource r");

        // Add the restrictions
        List<String> restrictions = new ArrayList<>();
        List<Object> parameters = new ArrayList<>();

        // Right type
        String t = "?";
        for (int i = 1; i < resourceDefinitions.size(); ++i) {
            t += ",?";
        }
        restrictions.add("r.type IN (" + t + ")");
        for (IPResourceDefinition resourceDefinition : resourceDefinitions) {
            parameters.add(resourceDefinition.getResourceType());
        }

        // Right ids
        if (query.getIdsIn() != null) {
            restrictions.add("r.id IN " + generateInParameters(query.getIdsIn().size()));
            parameters.addAll(query.getIdsIn());
        }

        // Right editor
        if (query.getEditorsIn() != null) {
            restrictions.add("r.editor_name IN " + generateInParameters(query.getEditorsIn().size()));
            parameters.addAll(query.getEditorsIn());
        }

        int searchCount = 0;
        // Equals
        for (Entry<String, Object> entry : query.getPropertyEquals().entrySet()) {
            String propertyName = entry.getKey();
            Object propertyValue = entry.getValue();

            // Get the first property type with that name
            IPResourceDefinition mainResourceDefinition = resourceDefinitions.stream() //
                    .filter(it -> it.getPropertyType(propertyName) != null) //
                    .findFirst().get();
            Class<?> propertyType = mainResourceDefinition.getPropertyType(propertyName);
            List<String> resourceTypes = resourceDefinitions.stream().map(it -> it.getResourceType()).collect(Collectors.toList());

            if (Set.class.isAssignableFrom(propertyType)) {
                // All the values
                Collection<?> propertyValues = (Collection<?>) propertyValue;
                for (Object nextPropertyValue : propertyValues) {
                    if (nextPropertyValue != null) {
                        searchCount = resourceFindAllAppendSql( //
                                fromSql, restrictions, parameters, //
                                searchCount, //
                                resourceTypes, propertyName, nextPropertyValue.getClass(), nextPropertyValue);
                    }
                }

                // Need exact count
                restrictions.add("(SELECT COUNT(*) FROM plugin_resource_column_search prcs WHERE prcs.plugin_resource_id = r.id AND prcs.column_name = ?) = ?");
                parameters.add(propertyName);
                parameters.add(propertyValues.size());
            } else {
                // The value must be equal
                searchCount = resourceFindAllAppendSql( //
                        fromSql, restrictions, parameters, //
                        searchCount, //
                        resourceTypes, propertyName, propertyType, propertyValue);
            }
        }

        // Contains
        for (Entry<String, Object> entry : query.getPropertyContains().entrySet()) {
            String propertyName = entry.getKey();
            Object propertyValue = entry.getValue();

            // Get the first property type with that name
            IPResourceDefinition mainResourceDefinition = resourceDefinitions.stream() //
                    .filter(it -> it.getPropertyType(propertyName) != null) //
                    .findFirst().get();
            Class<?> propertyType = mainResourceDefinition.getPropertyType(propertyName);
            List<String> resourceTypes = resourceDefinitions.stream().map(it -> it.getResourceType()).collect(Collectors.toList());

            if (Set.class.isAssignableFrom(propertyType)) {
                // All the values
                Collection<?> propertyValues = (Collection<?>) propertyValue;
                for (Object nextPropertyValue : propertyValues) {
                    if (nextPropertyValue != null) {
                        searchCount = resourceFindAllAppendSql( //
                                fromSql, restrictions, parameters, //
                                searchCount, //
                                resourceTypes, propertyName, nextPropertyValue.getClass(), nextPropertyValue);
                    }
                }

            } else {
                return new ArrayList<>();
            }
        }

        // Like
        for (Entry<String, String> entry : query.getPropertyLike().entrySet()) {
            String propertyName = entry.getKey();
            String propertyValue = entry.getValue();

            // Get the first property type with that name
            IPResourceDefinition mainResourceDefinition = resourceDefinitions.stream() //
                    .filter(it -> it.getPropertyType(propertyName) != null) //
                    .findFirst().get();
            Class<?> propertyType = mainResourceDefinition.getPropertyType(propertyName);

            String searchAlias = "se" + searchCount++;
            String searchRestriction;
            if (Collection.class.isAssignableFrom(propertyType)) {
                searchRestriction = searchAlias + ".text like ?";
            } else if (String.class.isAssignableFrom(propertyType)) {
                searchRestriction = searchAlias + ".text like ?";
            } else {
                throw new InfraUiException("Property [" + propertyName + "] does not support querying like");
            }

            fromSql.append(", plugin_resource_column_search ").append(searchAlias);

            restrictions.add(searchAlias + ".column_name = ?");
            parameters.add(propertyName);
            restrictions.add(searchAlias + ".plugin_resource_id = r.id");

            restrictions.add(searchRestriction);
            parameters.add(propertyValue);

        }

        // Lesser
        for (Entry<String, Object> entry : query.getPropertyLesser().entrySet()) {
            String propertyName = entry.getKey();
            Object propertyValue = entry.getValue();

            // Get the first property type with that name
            IPResourceDefinition mainResourceDefinition = resourceDefinitions.stream() //
                    .filter(it -> it.getPropertyType(propertyName) != null) //
                    .findFirst().get();
            Class<?> propertyType = mainResourceDefinition.getPropertyType(propertyName);

            String searchAlias = "se" + searchCount++;
            String searchRestriction;
            if (propertyType.isEnum()) {
                searchRestriction = searchAlias + ".int_number < ?";
                propertyValue = ((Enum<?>) propertyValue).ordinal();
            } else if (Double.class.isAssignableFrom(propertyType) || propertyType.equals(double.class)) {
                searchRestriction = searchAlias + ".double_number < ?";
            } else if (Date.class.isAssignableFrom(propertyType)) {
                searchRestriction = searchAlias + ".long_number < ?";
                propertyValue = ((Date) propertyValue).getTime();
            } else if (Float.class.isAssignableFrom(propertyType) || propertyType.equals(float.class)) {
                searchRestriction = searchAlias + ".float_number < ?";
            } else if (Integer.class.isAssignableFrom(propertyType) || propertyType.equals(int.class)) {
                searchRestriction = searchAlias + ".int_number < ?";
            } else if (Long.class.isAssignableFrom(propertyType) || propertyType.equals(long.class)) {
                searchRestriction = searchAlias + ".long_number < ?";
            } else {
                throw new InfraUiException("Property [" + propertyName + "] does not support querying lesser");
            }

            fromSql.append(", plugin_resource_column_search ").append(searchAlias);

            restrictions.add(searchAlias + ".column_name = ?");
            parameters.add(propertyName);
            restrictions.add(searchAlias + ".plugin_resource_id = r.id");

            restrictions.add(searchRestriction);
            parameters.add(propertyValue);

        }

        // Lesser and equal
        for (Entry<String, Object> entry : query.getPropertyLesserAndEquals().entrySet()) {
            String propertyName = entry.getKey();
            Object propertyValue = entry.getValue();

            // Get the first property type with that name
            IPResourceDefinition mainResourceDefinition = resourceDefinitions.stream() //
                    .filter(it -> it.getPropertyType(propertyName) != null) //
                    .findFirst().get();
            Class<?> propertyType = mainResourceDefinition.getPropertyType(propertyName);

            String searchAlias = "se" + searchCount++;
            String searchRestriction;
            if (propertyType.isEnum()) {
                searchRestriction = searchAlias + ".int_number <= ?";
                propertyValue = ((Enum<?>) propertyValue).ordinal();
            } else if (Double.class.isAssignableFrom(propertyType) || propertyType.equals(double.class)) {
                searchRestriction = searchAlias + ".double_number <= ?";
            } else if (Date.class.isAssignableFrom(propertyType)) {
                searchRestriction = searchAlias + ".long_number <= ?";
                propertyValue = ((Date) propertyValue).getTime();
            } else if (Float.class.isAssignableFrom(propertyType) || propertyType.equals(float.class)) {
                searchRestriction = searchAlias + ".float_number <= ?";
            } else if (Integer.class.isAssignableFrom(propertyType) || propertyType.equals(int.class)) {
                searchRestriction = searchAlias + ".int_number <= ?";
            } else if (Long.class.isAssignableFrom(propertyType) || propertyType.equals(long.class)) {
                searchRestriction = searchAlias + ".long_number <= ?";
            } else {
                throw new InfraUiException("Property [" + propertyName + "] does not support querying lesser and equal");
            }

            fromSql.append(", plugin_resource_column_search ").append(searchAlias);

            restrictions.add(searchAlias + ".column_name = ?");
            parameters.add(propertyName);
            restrictions.add(searchAlias + ".plugin_resource_id = r.id");

            restrictions.add(searchRestriction);
            parameters.add(propertyValue);

        }

        // Greater
        for (Entry<String, Object> entry : query.getPropertyGreater().entrySet()) {
            String propertyName = entry.getKey();
            Object propertyValue = entry.getValue();

            // Get the first property type with that name
            IPResourceDefinition mainResourceDefinition = resourceDefinitions.stream() //
                    .filter(it -> it.getPropertyType(propertyName) != null) //
                    .findFirst().get();
            Class<?> propertyType = mainResourceDefinition.getPropertyType(propertyName);

            String searchAlias = "se" + searchCount++;
            String searchRestriction;
            if (propertyType.isEnum()) {
                searchRestriction = searchAlias + ".int_number > ?";
                propertyValue = ((Enum<?>) propertyValue).ordinal();
            } else if (Double.class.isAssignableFrom(propertyType) || propertyType.equals(double.class)) {
                searchRestriction = searchAlias + ".double_number > ?";
            } else if (Date.class.isAssignableFrom(propertyType)) {
                searchRestriction = searchAlias + ".long_number > ?";
                propertyValue = ((Date) propertyValue).getTime();
            } else if (Float.class.isAssignableFrom(propertyType) || propertyType.equals(float.class)) {
                searchRestriction = searchAlias + ".float_number > ?";
            } else if (Integer.class.isAssignableFrom(propertyType) || propertyType.equals(int.class)) {
                searchRestriction = searchAlias + ".int_number > ?";
            } else if (Long.class.isAssignableFrom(propertyType) || propertyType.equals(long.class)) {
                searchRestriction = searchAlias + ".long_number > ?";
            } else {
                throw new InfraUiException("Property [" + propertyName + "] does not support querying greater");
            }

            fromSql.append(", plugin_resource_column_search ").append(searchAlias);

            restrictions.add(searchAlias + ".column_name = ?");
            parameters.add(propertyName);
            restrictions.add(searchAlias + ".plugin_resource_id = r.id");

            restrictions.add(searchRestriction);
            parameters.add(propertyValue);

        }

        // Greater and equal
        for (Entry<String, Object> entry : query.getPropertyGreaterEquals().entrySet()) {
            String propertyName = entry.getKey();
            Object propertyValue = entry.getValue();

            // Get the first property type with that name
            IPResourceDefinition mainResourceDefinition = resourceDefinitions.stream() //
                    .filter(it -> it.getPropertyType(propertyName) != null) //
                    .findFirst().get();
            Class<?> propertyType = mainResourceDefinition.getPropertyType(propertyName);

            String searchAlias = "se" + searchCount++;
            String searchRestriction;
            if (propertyType.isEnum()) {
                searchRestriction = searchAlias + ".int_number >= ?";
                propertyValue = ((Enum<?>) propertyValue).ordinal();
            } else if (Double.class.isAssignableFrom(propertyType) || propertyType.equals(double.class)) {
                searchRestriction = searchAlias + ".double_number >= ?";
            } else if (Date.class.isAssignableFrom(propertyType)) {
                searchRestriction = searchAlias + ".long_number >= ?";
                propertyValue = ((Date) propertyValue).getTime();
            } else if (Float.class.isAssignableFrom(propertyType) || propertyType.equals(float.class)) {
                searchRestriction = searchAlias + ".float_number >= ?";
            } else if (Integer.class.isAssignableFrom(propertyType) || propertyType.equals(int.class)) {
                searchRestriction = searchAlias + ".int_number >= ?";
            } else if (Long.class.isAssignableFrom(propertyType) || propertyType.equals(long.class)) {
                searchRestriction = searchAlias + ".long_number >= ?";
            } else {
                throw new InfraUiException("Property [" + propertyName + "] does not support querying greater and equal");
            }

            fromSql.append(", plugin_resource_column_search ").append(searchAlias);

            restrictions.add(searchAlias + ".column_name = ?");
            parameters.add(propertyName);
            restrictions.add(searchAlias + ".plugin_resource_id = r.id");

            restrictions.add(searchRestriction);
            parameters.add(propertyValue);

        }

        // Tags and
        searchCount = 0;
        for (String tagName : query.getTagsAnd()) {

            String tagAlias = "t" + searchCount++;

            fromSql.append(", plugin_resource_tag ").append(tagAlias);

            restrictions.add(tagAlias + ".tag_name = ?");
            parameters.add(tagName);
            restrictions.add(tagAlias + ".plugin_resource_id = r.id");

        }

        // Tags or
        if (!query.getTagsOr().isEmpty()) {

            String tagAlias = "t" + searchCount++;

            fromSql.append(", plugin_resource_tag ").append(tagAlias);

            StringBuilder sb = new StringBuilder();
            sb.append("?");
            for (int i = 1; i < query.getTagsOr().size(); ++i) {
                sb.append(",");
                sb.append("?");
            }

            restrictions.add(tagAlias + ".tag_name IN (" + sb.toString() + ")");
            parameters.addAll(query.getTagsOr());
            restrictions.add(tagAlias + ".plugin_resource_id = r.id");

        }

        // Query the DB
        String sql = selectSql + fromSql.toString() + " WHERE " + Joiner.on(" AND ").join(restrictions) + " ORDER BY r.id";
        logger.debug("SQL: {} | params: {}", sql, parameters);
        pluginResourceColumnSearchDao.flush();
        List<T> resources = jdbcTemplate.query(sql, parameters.toArray(new Object[parameters.size()]), new RowMapper<T>() {
            @SuppressWarnings("unchecked")
            @Override
            public T mapRow(ResultSet rs, int rowNum) throws SQLException {
                IPResourceDefinition resourceDefinition = getResourceDefinition(rs.getString(2));
                if (resourceDefinition == null) {
                    return null;
                }
                T resource = (T) JsonTools.readFromString(rs.getString(3), resourceDefinition.getResourceClass());
                resource.setInternalId(rs.getLong(1));
                return resource;
            }
        });

        return resources;

    }

    protected int resourceFindAllAppendSql( //
            StringBuilder fromSql, List<String> restrictions, List<Object> parameters, //
            int searchCount, //
            List<String> resourceTypes, String propertyName, Class<?> propertyType, Object propertyValue) {
        String searchAlias = "se" + searchCount++;
        String searchRestriction;
        if (Boolean.class.isAssignableFrom(propertyType) || propertyType.equals(boolean.class)) {
            searchRestriction = searchAlias + ".bool = ?";
        } else if (Collection.class.isAssignableFrom(propertyType)) {
            searchRestriction = searchAlias + ".text = ?";
        } else if (propertyType.isEnum()) {
            searchRestriction = searchAlias + ".int_number = ?";
            Enum<?> propertyValueEnum = (Enum<?>) propertyValue;
            if (propertyValueEnum == null) {
                propertyValue = null;
            } else {
                propertyValue = propertyValueEnum.ordinal();
            }
        } else if (Double.class.isAssignableFrom(propertyType) || propertyType.equals(double.class)) {
            searchRestriction = searchAlias + ".double_number = ?";
        } else if (Date.class.isAssignableFrom(propertyType)) {
            searchRestriction = searchAlias + ".long_number = ?";
            propertyValue = ((Date) propertyValue).getTime();
        } else if (Float.class.isAssignableFrom(propertyType) || propertyType.equals(float.class)) {
            searchRestriction = searchAlias + ".float_number = ?";
        } else if (Integer.class.isAssignableFrom(propertyType) || propertyType.equals(int.class)) {
            searchRestriction = searchAlias + ".int_number = ?";
        } else if (Long.class.isAssignableFrom(propertyType) || propertyType.equals(long.class)) {
            searchRestriction = searchAlias + ".long_number = ?";
        } else if (String.class.isAssignableFrom(propertyType)) {
            searchRestriction = searchAlias + ".text = ?";
        } else {
            // Unknown type
            logger.error("Search: Unknown column type {} for types {} and property {}", propertyType.getName(), resourceTypes, propertyName);
            return searchCount;
        }

        fromSql.append(", plugin_resource_column_search ").append(searchAlias);

        restrictions.add(searchAlias + ".column_name = ?");
        parameters.add(propertyName);
        restrictions.add(searchAlias + ".plugin_resource_id = r.id");

        if (propertyValue == null) {
            restrictions.add(searchRestriction.replace(" = ?", " IS NULL"));
        } else {
            restrictions.add(searchRestriction);
            parameters.add(propertyValue);
        }

        return searchCount;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <R extends IPResource> Optional<R> resourceFindByPk(R resource) {
        return resourceFind(createResourceQuery((Class<R>) resource.getClass()) //
                .primaryKeyEquals(resource));
    }

    @Override
    public void resourceUpdate(IPResource previousResource, IPResource updatedResource) {
        PluginResource pluginResource = pluginResourceDao.findOne(previousResource.getInternalId());
        String resourceType = getResourceDefinition(updatedResource).getResourceType();
        pluginResource.store(resourceType, updatedResource);
        updateColumnSearches(updatedResource);
    }

    public void setInfiniteLoopTimeoutInMs(long infiniteLoopTimeoutInMs) {
        this.infiniteLoopTimeoutInMs = infiniteLoopTimeoutInMs;
    }

    @Override
    public void tagAdd(long resourceId, String tagName) {
        PluginResource pluginResource = pluginResourceDao.findOne(resourceId);
        pluginResourceTagDao.save(new PluginResourceTag(tagName, pluginResource));
    }

    @Override
    public boolean tagDelete(long resourceId, String tagName) {
        return pluginResourceTagDao.deleteByPluginResourceIdAndTagName(resourceId, tagName) > 0;
    }

    @Override
    public boolean tagExists(long resourceId, String tagName) {
        return pluginResourceTagDao.findByPluginResourceIdAndTagName(resourceId, tagName) != null;
    }

    @Override
    public Set<String> tagFindAllByResource(IPResource resource) {
        if (resource.getInternalId() == null) {
            throw new ResourceNotFromRepositoryException(resource);
        }
        return pluginResourceTagDao.findAllTagNameByPluginResourceId(resource.getInternalId());
    }

    private void updateColumnSearches(IPResource resource) {

        IPResourceDefinition resourceDefinition = getResourceDefinition(resource);
        AssertTools.assertNotNull(resourceDefinition, "Unknown resource of type [" + resource.getClass() + "]");
        Long pluginResourceId = resource.getInternalId();
        AssertTools.assertNotNull(pluginResourceId, "The internal id cannot be null");
        PluginResource pluginResource = pluginResourceDao.getOne(pluginResourceId);
        pluginResourceColumnSearchDao.deleteByPluginResourceId(pluginResourceId);

        List<PluginResourceColumnSearch> columnSearches = new ArrayList<>();
        for (String propertyName : resourceDefinition.getSearchableProperties()) {
            try {
                Object propertyValue = resourceDefinition.getPropertyGetterMethod(propertyName).invoke(resource);
                Class<?> propertyType = resourceDefinition.getPropertyType(propertyName);

                if (Set.class.isAssignableFrom(propertyType)) {
                    // Store each values of the set
                    if (propertyValue != null) {
                        Set<?> propertyValues = (Set<?>) propertyValue;
                        for (Object nextPropertyValue : propertyValues) {
                            if (nextPropertyValue != null) {
                                addColumnSearch(columnSearches, pluginResource, propertyName, nextPropertyValue.getClass(), nextPropertyValue);
                            }
                        }
                    }
                } else {
                    addColumnSearch(columnSearches, pluginResource, propertyName, propertyType, propertyValue);
                }

            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
                logger.error("Could not retrieve the property value of the resource", e);
            }
        }
        pluginResourceColumnSearchDao.save(columnSearches);
    }

    @Override
    public void updateColumnSearches(PluginResource pluginResource) {
        Class<? extends IPResource> resourceClass = getResourceDefinition(pluginResource.getType()).getResourceClass();
        IPResource resource = pluginResource.loadResource(resourceClass);
        updateColumnSearches(resource);
    }

}
