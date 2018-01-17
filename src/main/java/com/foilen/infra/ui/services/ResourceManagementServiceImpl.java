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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.foilen.infra.plugin.v1.core.context.ChangesContext;
import com.foilen.infra.plugin.v1.core.context.CommonServicesContext;
import com.foilen.infra.plugin.v1.core.context.UpdateEventContext;
import com.foilen.infra.plugin.v1.core.eventhandler.UpdateEventHandler;
import com.foilen.infra.plugin.v1.core.exception.InfiniteUpdateLoop;
import com.foilen.infra.plugin.v1.core.exception.ProblemException;
import com.foilen.infra.plugin.v1.core.exception.ResourceNotFoundException;
import com.foilen.infra.plugin.v1.core.exception.ResourceNotFromRepositoryException;
import com.foilen.infra.plugin.v1.core.exception.ResourcePrimaryKeyCollisionException;
import com.foilen.infra.plugin.v1.core.resource.IPResourceDefinition;
import com.foilen.infra.plugin.v1.core.resource.IPResourceQuery;
import com.foilen.infra.plugin.v1.core.service.IPPluginService;
import com.foilen.infra.plugin.v1.core.service.IPResourceService;
import com.foilen.infra.plugin.v1.core.service.internal.InternalChangeService;
import com.foilen.infra.plugin.v1.core.service.internal.InternalIPResourceService;
import com.foilen.infra.plugin.v1.model.resource.IPResource;
import com.foilen.infra.ui.InfraUiException;
import com.foilen.infra.ui.db.dao.PluginResourceColumnSearchDao;
import com.foilen.infra.ui.db.dao.PluginResourceDao;
import com.foilen.infra.ui.db.dao.PluginResourceLinkDao;
import com.foilen.infra.ui.db.dao.PluginResourceTagDao;
import com.foilen.infra.ui.db.domain.audit.AuditUserType;
import com.foilen.infra.ui.db.domain.plugin.PluginResource;
import com.foilen.infra.ui.db.domain.plugin.PluginResourceColumnSearch;
import com.foilen.infra.ui.db.domain.plugin.PluginResourceLink;
import com.foilen.infra.ui.db.domain.plugin.PluginResourceTag;
import com.foilen.login.spring.client.security.FoilenAuthentication;
import com.foilen.smalltools.JavaEnvironmentValues;
import com.foilen.smalltools.tools.AbstractBasics;
import com.foilen.smalltools.tools.AssertTools;
import com.foilen.smalltools.tools.JsonTools;
import com.foilen.smalltools.tools.SecureRandomTools;
import com.foilen.smalltools.tools.ThreadNameStateTool;
import com.foilen.smalltools.tools.ThreadTools;
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
    private IPPluginService ipPluginService;
    @Autowired
    private SecurityService securityService;

    private Map<Class<? extends IPResource>, IPResourceDefinition> resourceDefinitionByResourceClass = new HashMap<>();
    private Map<String, IPResourceDefinition> resourceDefinitionByResourceType = new HashMap<>();

    private AtomicLong txCounter = new AtomicLong();
    private String baseTxId = JavaEnvironmentValues.getHostName() + "/" + SecureRandomTools.randomHexString(5) + "/";

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

    private void applyChanges(String txId, AuditUserType userType, String userName, boolean explicitChange, ChangesContext changes, Queue<IPResource> addedResources,
            Queue<IPResource> updatedResourcesPrevious, Queue<IPResource> deletedResources, Map<Long, List<Tuple3<IPResource, String, IPResource>>> deletedResourcePreviousLinksByResourceId,
            Set<Long> removedResourcesInThisTransaction) {

        logger.debug("State before applying changes. Has {} updates, {} deletions, {} addition", updatedResourcesPrevious.size(), deletedResources.size(), addedResources.size());
        logger.debug("[APPLY] Resources: has {} updates, {} deletions, {} addition ; Links: has {} deletions, {} addition ; Tags: has {} deletions, {} addition", //
                changes.getResourcesToUpdate().size(), changes.getResourcesToDelete().size(), changes.getResourcesToAdd().size(), //
                changes.getLinksToDelete().size(), changes.getLinksToAdd().size(), //
                changes.getTagsToDelete().size(), changes.getTagsToAdd().size() //
        );

        // Delete
        Set<Long> updatedIds = new HashSet<>();
        for (Long id : changes.getResourcesToDelete()) {

            if (removedResourcesInThisTransaction.add(id)) {
                logger.debug("[APPLY] Delete resource {}", id);
            } else {
                logger.debug("[APPLY] Delete resource {}. Already deleted in this transaction. Skipping", id);

                continue;
            }

            List<Tuple3<IPResource, String, IPResource>> deletedResourcePreviousLinks = new ArrayList<>();
            deletedResourcePreviousLinksByResourceId.put(id, deletedResourcePreviousLinks);

            IPResource resource = resourceFind(id).get();
            auditingService.resourceDelete(txId, explicitChange, userType, userName, resource);

            deletedResources.add(resource);
            deletedResourcePreviousLinks.addAll(linkFindAllRelatedByResource(id));
            Set<Long> idsToUpdate = new HashSet<>();
            idsToUpdate.addAll(linkFindAllByFromResource(resource).stream().map(it -> it.getB().getInternalId()).collect(Collectors.toList()));
            idsToUpdate.addAll(linkFindAllByToResource(resource).stream().map(it -> it.getA().getInternalId()).collect(Collectors.toList()));
            markAllTransientLinkedResourcesToUpdate(updatedIds, idsToUpdate);
            pluginResourceColumnSearchDao.deleteByPluginResourceId(id);
            pluginResourceTagDao.deleteByPluginResourceId(id);
            pluginResourceLinkDao.deleteByPluginResourceId(id);
            pluginResourceDao.delete(id);
        }
        for (Tuple3<IPResource, String, IPResource> link : changes.getLinksToDelete()) {
            logger.debug("[APPLY] Delete link {}", link);
            Optional<IPResource> fromResource = resourceFindByPk(link.getA());
            Optional<IPResource> toResource = resourceFindByPk(link.getC());
            if (fromResource.isPresent() && toResource.isPresent()) {
                Long fromId = fromResource.get().getInternalId();
                Long toId = toResource.get().getInternalId();
                String linkType = link.getB();
                if (pluginResourceLinkDao.deleteByFromPluginResourceIdAndLinkTypeAndToPluginResourceId(fromId, linkType, toId) > 0) {
                    auditingService.linkDelete(txId, explicitChange, userType, userName, fromResource.get(), linkType, toResource.get());
                    markAllTransientLinkedResourcesToUpdate(updatedIds, Arrays.asList(fromId, toId));
                }
            }
        }
        for (Tuple2<IPResource, String> tag : changes.getTagsToDelete()) {
            logger.debug("[APPLY] Delete tag {}", tag);
            Optional<IPResource> resource = resourceFindByPk(tag.getA());
            if (resource.isPresent()) {
                Long internalId = resource.get().getInternalId();
                String tagName = tag.getB();
                if (pluginResourceTagDao.deleteByPluginResourceIdAndTagName(internalId, tagName) > 0) {
                    auditingService.tagDelete(txId, explicitChange, userType, userName, resource.get(), tagName);
                    updatedIds.add(internalId);
                }
            }
        }

        // Add
        for (IPResource resource : changes.getResourcesToAdd()) {
            logger.debug("[APPLY] Add resource {}", resource);
            // Check if already exists
            if (resourceFindByPk(resource).isPresent()) {
                throw new ResourcePrimaryKeyCollisionException();
            }

            auditingService.resourceAdd(txId, explicitChange, userType, userName, resource);

            String resourceType = getResourceDefinition(resource).getResourceType();
            PluginResource pluginResource = pluginResourceDao.save(new PluginResource(resourceType, resource));
            addedResources.add(pluginResource.loadResource(resource.getClass()));
            resource.setInternalId(pluginResource.getId());
            updateColumnSearches(resource);

            // Add the direct links for update notification
            Set<Long> idsToUpdate = new HashSet<>();
            idsToUpdate.addAll(linkFindAllByFromResource(resource).stream().map(it -> it.getB().getInternalId()).collect(Collectors.toList()));
            idsToUpdate.addAll(linkFindAllByToResource(resource).stream().map(it -> it.getA().getInternalId()).collect(Collectors.toList()));
            markAllTransientLinkedResourcesToUpdate(updatedIds, idsToUpdate);

        }
        for (Tuple3<IPResource, String, IPResource> link : changes.getLinksToAdd()) {
            logger.debug("[APPLY] Add link {}", link);
            Optional<IPResource> fromResource = resourceFindByPk(link.getA());
            if (!fromResource.isPresent()) {
                throw new ResourceNotFoundException(link.getA());
            }
            Optional<IPResource> toResource = resourceFindByPk(link.getC());
            if (!toResource.isPresent()) {
                throw new ResourceNotFoundException(link.getC());
            }

            // Add if not present
            Long fromId = fromResource.get().getInternalId();
            Long toId = toResource.get().getInternalId();
            String linkType = link.getB();
            if (pluginResourceLinkDao.findByFromPluginResourceIdAndLinkTypeAndToPluginResourceId(fromId, linkType, toId) == null) {
                // Add
                PluginResource fromPluginResource = pluginResourceDao.findOne(fromId);
                PluginResource toPluginResource = pluginResourceDao.findOne(toId);
                pluginResourceLinkDao.save(new PluginResourceLink(fromPluginResource, linkType, toPluginResource));
                auditingService.linkAdd(txId, explicitChange, userType, userName, fromResource.get(), linkType, toResource.get());
                markAllTransientLinkedResourcesToUpdate(updatedIds, Arrays.asList(fromId, toId));
            }

        }
        for (Tuple2<IPResource, String> tag : changes.getTagsToAdd()) {
            logger.debug("[APPLY] Add tag {}", tag);
            Optional<IPResource> resource = resourceFindByPk(tag.getA());
            if (!resource.isPresent()) {
                throw new ResourceNotFoundException(tag.getA());
            }

            Long pluginResourceId = resource.get().getInternalId();
            // Add if not present
            String tagName = tag.getB();
            PluginResourceTag pluginResourceTag = pluginResourceTagDao.findByPluginResourceIdAndTagName(pluginResourceId, tagName);
            if (pluginResourceTag == null) {
                // Add
                PluginResource pluginResource = pluginResourceDao.findOne(pluginResourceId);
                pluginResourceTagDao.save(new PluginResourceTag(tagName, pluginResource));
                auditingService.tagAdd(txId, explicitChange, userType, userName, loadResource(pluginResource), tagName);
                updatedIds.add(pluginResourceId);
            }
        }
        updatedIds.removeAll(removedResourcesInThisTransaction);
        updatedResourcesPrevious.addAll(updatedIds.stream().map(it -> resourceFind(it).get()).collect(Collectors.toList()));

        Set<Long> updatedIdSeconds = new HashSet<>();
        // Update
        for (Tuple2<Long, IPResource> update : changes.getResourcesToUpdate()) {

            logger.debug("[APPLY] Update resource {}", update);

            // Get the previous resource
            Optional<IPResource> previousResourceOptional = resourceFind(update.getA());
            if (!previousResourceOptional.isPresent()) {
                throw new ResourceNotFoundException(update.getA());
            }
            IPResource previousResource = previousResourceOptional.get();
            if (updatedIds.add(previousResource.getInternalId())) {
                updatedResourcesPrevious.add(previousResource);
            }

            // Get the next resource (might not exists)
            IPResource updatedResource = update.getB();
            Optional<IPResource> nextResourceOptional = resourceFindByPk(updatedResource);
            if (nextResourceOptional.isPresent()) {
                // Check if not the same resource
                IPResource nextResource = nextResourceOptional.get();
                if (!previousResource.getInternalId().equals(nextResource.getInternalId())) {
                    throw new ResourcePrimaryKeyCollisionException();
                }
            }

            // Update the resource
            updatedResource.setInternalId(update.getA());
            PluginResource pluginResource = pluginResourceDao.findOne(previousResource.getInternalId());
            String resourceType = getResourceDefinition(updatedResource).getResourceType();
            pluginResource.store(resourceType, updatedResource);
            auditingService.resourceUpdate(txId, explicitChange, userType, userName, previousResource, updatedResource);
            updateColumnSearches(updatedResource);

            // Add the direct links for update notification
            updatedResourcesPrevious.addAll(linkFindAllByFromResource(updatedResource).stream().map(it -> it.getB()).collect(Collectors.toList()));
            updatedResourcesPrevious.addAll(linkFindAllByToResource(updatedResource).stream().map(it -> it.getA()).collect(Collectors.toList()));
            // Add all the transient managed resources links for update notification
            markAllTransientLinkedResourcesToUpdate(updatedIdSeconds, Arrays.asList(updatedResource.getInternalId()));
        }

        updatedIdSeconds.removeAll(updatedIds);
        updatedResourcesPrevious.addAll(updatedIdSeconds.stream().map(it -> resourceFind(it).get()).collect(Collectors.toList()));

        // Cleanup lists
        removedResourcesInThisTransaction.addAll(deletedResources.stream().map(IPResource::getInternalId).collect(Collectors.toSet()));
        updatedResourcesPrevious.removeIf(it -> removedResourcesInThisTransaction.contains(it.getInternalId()));

        changes.clear();
        logger.debug("State after applying changes. Has {} updates, {} deletions, {} addition", updatedResourcesPrevious.size(), deletedResources.size(), addedResources.size());
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Override
    public void changesExecute(ChangesContext changes) {

        String txId = genTxId();

        AuditUserType userType;
        String userName;
        boolean explicitChange = true;

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            userType = AuditUserType.SYSTEM;
            userName = null;
        } else if (auth instanceof FoilenAuthentication) {
            userType = AuditUserType.USER;
            userName = auth.getName();
        } else {
            userType = AuditUserType.API;
            userName = auth.getName();
        }

        ThreadNameStateTool threadNameStateTool = ThreadTools.nameThread() //
                .appendText(" - txId:") //
                .appendText(txId) //
                .appendText(" - user:") //
                .appendText(userType.name());

        if (userName != null) {
            threadNameStateTool.appendText("/") //
                    .appendText(userName);
        }

        threadNameStateTool.change();

        logger.info("----- [changesExecute] Begin -----");

        try {

            Queue<IPResource> addedResources = new LinkedBlockingQueue<>();
            Queue<IPResource> deletedResources = new LinkedBlockingQueue<>();
            Map<Long, List<Tuple3<IPResource, String, IPResource>>> deletedResourcePreviousLinksByResourceId = new LinkedHashMap<>();
            Queue<IPResource> updatedResourcesPrevious = new LinkedBlockingQueue<>();
            Set<Long> removedResourcesInThisTransaction = new LinkedHashSet<>();

            long globalLoopCount = 0;
            Map<Class<?>, List<UpdateEventContext>> updateEventContextsByResourceType = ipPluginService.getUpdateEvents().stream() //
                    .collect(Collectors.groupingBy(it -> it.getUpdateEventHandler().supportedClass()));

            // Apply the changes
            applyChanges(txId, userType, userName, explicitChange, changes, addedResources, updatedResourcesPrevious, deletedResources, deletedResourcePreviousLinksByResourceId,
                    removedResourcesInThisTransaction);
            explicitChange = false;

            while ((!addedResources.isEmpty() || !updatedResourcesPrevious.isEmpty() || !deletedResources.isEmpty()) && globalLoopCount < 100) {
                ++globalLoopCount;

                logger.debug("Update events loop {}. Has {} updates, {} deletions, {} addition", globalLoopCount, updatedResourcesPrevious.size(), deletedResources.size(), addedResources.size());

                // Process all updates
                IPResource itemPrevious;
                while ((itemPrevious = updatedResourcesPrevious.poll()) != null) {
                    Optional<IPResource> currentResourceOptional = resourceFind(itemPrevious.getInternalId());
                    if (!currentResourceOptional.isPresent()) {
                        throw new ResourceNotFoundException(itemPrevious);
                    }
                    IPResource currentResource = currentResourceOptional.get();
                    List<UpdateEventContext> eventContexts = updateEventContextsByResourceType.get(itemPrevious.getClass());
                    if (eventContexts != null) {
                        logger.debug("[UPDATE EVENT] Processing {} updated handlers", eventContexts.size());
                        for (UpdateEventContext eventContext : eventContexts) {
                            logger.debug("[UPDATE EVENT] Processing {} updated handler", eventContext.getUpdateHandlerName());
                            UpdateEventHandler updateEventHandler = eventContext.getUpdateEventHandler();
                            updateEventHandler.updateHandler(commonServicesContext, changes, itemPrevious, currentResource);
                            applyChanges(txId, userType, userName, explicitChange, changes, addedResources, updatedResourcesPrevious, deletedResources, deletedResourcePreviousLinksByResourceId,
                                    removedResourcesInThisTransaction);
                        }
                    }
                }

                // Process all deletes
                IPResource item;
                while ((item = deletedResources.poll()) != null) {
                    List<UpdateEventContext> eventContexts = updateEventContextsByResourceType.get(item.getClass());
                    if (eventContexts != null) {
                        logger.debug("[UPDATE EVENT] Processing {} deleted handlers", eventContexts.size());
                        for (UpdateEventContext eventContext : eventContexts) {
                            logger.debug("[UPDATE EVENT] Processing {} deleted handler", eventContext.getUpdateHandlerName());
                            UpdateEventHandler updateEventHandler = eventContext.getUpdateEventHandler();
                            updateEventHandler.deleteHandler(commonServicesContext, changes, item, deletedResourcePreviousLinksByResourceId.get(item.getInternalId()));
                            applyChanges(txId, userType, userName, explicitChange, changes, addedResources, updatedResourcesPrevious, deletedResources, deletedResourcePreviousLinksByResourceId,
                                    removedResourcesInThisTransaction);
                        }
                    }
                }

                // Process all adds
                while ((item = addedResources.poll()) != null) {
                    List<UpdateEventContext> eventContexts = updateEventContextsByResourceType.get(item.getClass());
                    if (eventContexts != null) {
                        logger.debug("[UPDATE EVENT] Processing {} added handlers", eventContexts.size());
                        for (UpdateEventContext eventContext : eventContexts) {
                            logger.debug("[UPDATE EVENT] Processing {} added handler", eventContext.getUpdateHandlerName());
                            UpdateEventHandler updateEventHandler = eventContext.getUpdateEventHandler();
                            updateEventHandler.addHandler(commonServicesContext, changes, item);
                            applyChanges(txId, userType, userName, explicitChange, changes, addedResources, updatedResourcesPrevious, deletedResources, deletedResourcePreviousLinksByResourceId,
                                    removedResourcesInThisTransaction);
                        }
                    }
                }

                // Apply any pending changes
                applyChanges(txId, userType, userName, explicitChange, changes, addedResources, updatedResourcesPrevious, deletedResources, deletedResourcePreviousLinksByResourceId,
                        removedResourcesInThisTransaction);

            }

            if (!addedResources.isEmpty() || !updatedResourcesPrevious.isEmpty() || !deletedResources.isEmpty()) {
                throw new InfiniteUpdateLoop("Iterated " + globalLoopCount + " times and there are always changes");
            }

            // Complete the transaction
            logger.info("===== [changesExecute] Completed =====");

        } catch (RuntimeException e) {
            // Rollback the transaction
            logger.error("===== [changesExecute] Problem while executing the changes. Rolling back transaction =====", e);
            throw e;
        } finally {
            threadNameStateTool.revert();
        }

    }

    @Override
    public <T extends IPResource> IPResourceQuery<T> createResourceQuery(Class<T> resourceClass) {
        IPResourceDefinition resourceDefinition = getResourceDefinition(resourceClass);
        if (resourceDefinition == null) {
            throw new ProblemException("Resource class " + resourceClass + " is unknown");
        }
        return new IPResourceQuery<>(resourceDefinition);
    }

    @Override
    public <T extends IPResource> IPResourceQuery<T> createResourceQuery(String resourceType) {
        IPResourceDefinition resourceDefinition = resourceDefinitionByResourceType.get(resourceType);
        if (resourceDefinition == null) {
            throw new ProblemException("Resource type " + resourceType + " is unknown");
        }
        return new IPResourceQuery<>(resourceDefinition);
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

    private String genTxId() {
        return baseTxId + txCounter.getAndIncrement();
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

    protected List<Tuple2<String, ? extends IPResource>> linkFindAllByFromResource(long fromResourceId) {
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
        String toResourceType = getResourceDefinition(toResourceClass).getResourceType();
        return pluginResourceLinkDao.findAllByFromPluginResourceIdAndLinkTypeAndToPluginResourceType(fromResource.getInternalId(), linkType, toResourceType).stream() //
                .map(it -> (R) loadResource(it.getToPluginResource())) //
                .collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    @Override
    public <R extends IPResource> List<R> linkFindAllByFromResourceClassAndLinkTypeAndToResource(Class<R> fromResourceClass, String linkType, IPResource toResource) {
        if (toResource.getInternalId() == null) {
            throw new ResourceNotFromRepositoryException(toResource);
        }
        String fromResourceType = getResourceDefinition(fromResourceClass).getResourceType();
        return pluginResourceLinkDao.findAllByFromPluginResourceTypeAndLinkTypeAndToPluginResourceId(fromResourceType, linkType, toResource.getInternalId()).stream() //
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

    protected List<Tuple2<? extends IPResource, String>> linkFindAllByToResource(long toResourceId) {
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
        return pluginResource.loadResource(getResourceDefinition(pluginResource.getType()) //
                .getResourceClass());
    }

    private void markAllTransientLinkedResourcesToUpdate(Set<Long> updatedIds, Collection<Long> ids) {
        Set<Long> transientProcessedIds = new HashSet<>();
        markAllTransientLinkedResourcesToUpdate(updatedIds, transientProcessedIds, ids);
    }

    private void markAllTransientLinkedResourcesToUpdate(Set<Long> updatedIds, Set<Long> transientProcessedIds, Collection<Long> ids) {

        updatedIds.addAll(ids);

        for (Long id : ids) {
            if (!transientProcessedIds.add(id)) {
                continue;
            }

            List<Tuple2<String, ? extends IPResource>> resourcesTo = linkFindAllByFromResource(id);
            markAllTransientLinkedResourcesToUpdate(updatedIds, transientProcessedIds, resourcesTo.stream().map(it -> it.getB().getInternalId()).collect(Collectors.toSet()));
            List<Tuple2<? extends IPResource, String>> resourcesFrom = linkFindAllByToResource(id);
            markAllTransientLinkedResourcesToUpdate(updatedIds, transientProcessedIds, resourcesFrom.stream().map(it -> it.getA().getInternalId()).collect(Collectors.toSet()));
        }

    }

    @Override
    public void resourceAdd(IPResourceDefinition resourceDefinition) {
        resourceDefinitionByResourceClass.put(resourceDefinition.getResourceClass(), resourceDefinition);
        resourceDefinitionByResourceType.put(resourceDefinition.getResourceType(), resourceDefinition);
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
        if (!securityService.isAdmin()) {
            return new ArrayList<>();
        }

        IPResourceDefinition resourceDefinition = query.getResourceDefinition();

        String selectSql = "SELECT DISTINCT r.id, r.value_json";
        StringBuilder fromSql = new StringBuilder(" FROM plugin_resource r");

        // Add the restrictions
        List<String> restrictions = new ArrayList<>();
        List<Object> parameters = new ArrayList<>();

        // Right type
        String resourceType = query.getResourceDefinition().getResourceType();
        restrictions.add("r.type = ?");
        parameters.add(resourceType);

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

            Class<?> propertyType = resourceDefinition.getPropertyType(propertyName);

            if (Set.class.isAssignableFrom(propertyType)) {
                // All the values
                Collection<?> propertyValues = (Collection<?>) propertyValue;
                for (Object nextPropertyValue : propertyValues) {
                    if (nextPropertyValue != null) {
                        searchCount = resourceFindAllAppendSql( //
                                fromSql, restrictions, parameters, //
                                searchCount, //
                                resourceType, propertyName, nextPropertyValue.getClass(), nextPropertyValue);
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
                        resourceType, propertyName, propertyType, propertyValue);
            }
        }

        // Contains
        for (Entry<String, Object> entry : query.getPropertyContains().entrySet()) {
            String propertyName = entry.getKey();
            Object propertyValue = entry.getValue();

            Class<?> propertyType = resourceDefinition.getPropertyType(propertyName);

            if (Set.class.isAssignableFrom(propertyType)) {
                // All the values
                Collection<?> propertyValues = (Collection<?>) propertyValue;
                for (Object nextPropertyValue : propertyValues) {
                    if (nextPropertyValue != null) {
                        searchCount = resourceFindAllAppendSql( //
                                fromSql, restrictions, parameters, //
                                searchCount, //
                                resourceType, propertyName, nextPropertyValue.getClass(), nextPropertyValue);
                    }
                }

            } else {
                return new ArrayList<>();
            }
        }

        // Like
        for (

        Entry<String, String> entry : query.getPropertyLike().entrySet()) {
            String propertyName = entry.getKey();
            String propertyValue = entry.getValue();

            Class<?> propertyType = resourceDefinition.getPropertyType(propertyName);

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

            Class<?> propertyType = resourceDefinition.getPropertyType(propertyName);

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

            Class<?> propertyType = resourceDefinition.getPropertyType(propertyName);

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

            Class<?> propertyType = resourceDefinition.getPropertyType(propertyName);

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

            Class<?> propertyType = resourceDefinition.getPropertyType(propertyName);

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
                IPResourceDefinition resourceDefinition = getResourceDefinition(resourceType);
                if (resourceDefinition == null) {
                    return null;
                }
                T resource = (T) JsonTools.readFromString(rs.getString(2), resourceDefinition.getResourceClass());
                resource.setInternalId(rs.getLong(1));
                return resource;
            }
        });

        return resources;

    }

    protected int resourceFindAllAppendSql( //
            StringBuilder fromSql, List<String> restrictions, List<Object> parameters, //
            int searchCount, //
            String resourceType, String propertyName, Class<?> propertyType, Object propertyValue) {
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
            logger.error("Search: Unknown column type {} for type {} and property {}", propertyType.getName(), resourceType, propertyName);
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
