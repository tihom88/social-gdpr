package com.adobe.cq.social.gdpr.core.gdprService.impl;

import com.adobe.cq.social.gdpr.core.UserUgcCommons.ComponentEnum;
import com.adobe.cq.social.gdpr.core.factory.UserUgcComponentFactory;
import com.adobe.cq.social.gdpr.core.gdprService.GdprService;
import com.adobe.cq.social.forum.client.endpoints.ForumOperations;
import com.adobe.cq.social.journal.client.endpoints.JournalOperations;
import com.adobe.cq.social.scf.OperationException;
import com.adobe.cq.social.ugc.api.SearchResults;
import com.adobe.cq.social.ugc.api.UgcFilter;
import com.adobe.cq.social.ugc.api.UgcSearch;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.jackrabbit.api.security.user.Authorizable;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by mokatari on 10/16/17.
 */
@Component
@Service
public class GdprServiceImpl implements GdprService{
    private static final Logger log = LoggerFactory.getLogger(GdprServiceImpl.class);

    @Reference
    UserUgcComponentFactory userUgcComponentFactory;

    @Reference
    UgcSearch ugcSearch;

    @Reference
    protected ForumOperations forumOperations;

    @Reference
    private JournalOperations journalOperations;

    @Override
    public Map<ComponentEnum, SearchResults<Resource>> getUserUgc(ResourceResolver resourceResolver, List<ComponentEnum> componentEnumList, String userId) {

        Map<ComponentEnum, UgcFilter> ugcFilterList = getUserUgcFilters(componentEnumList, userId);
        Map<ComponentEnum, SearchResults<Resource>> resultsList = new HashMap<ComponentEnum, SearchResults<Resource>>();
        try {
            for(Map.Entry<ComponentEnum, UgcFilter> entry : ugcFilterList.entrySet()){
                // Max value need to be checked (MAX_VALUE can't be used, throwing out of range error )
                SearchResults<Resource>  results = ugcSearch.find(null, resourceResolver, entry.getValue(), 0, 100000, false);
                resultsList.put( entry.getKey(),results);
            }
        } catch (RepositoryException e) {
            e.printStackTrace();
        }
        return resultsList;
    }

    @Override
    public boolean deleteUserUgc(ResourceResolver resourceResolver, List<ComponentEnum> componentEnumList, String userId) throws OperationException {

        Map<ComponentEnum, SearchResults<Resource>> searchResults = getUserUgc(resourceResolver, componentEnumList, userId);

        final Session session = resourceResolver.adaptTo(Session.class);
        for(Map.Entry<ComponentEnum, SearchResults<Resource>> entry : searchResults.entrySet()){
            deleteResources(entry.getKey(), entry.getValue(), session);
            /*switch (entry.getKey()){
                case BLOG_COMMENT:
                    deleteResources(ComponentEnum.BLOG_COMMENT, entry.getValue(), session);
                    break;
                case BLOG_ENTRY:
                    deleteResources(ComponentEnum.BLOG_ENTRY, entry.getValue(), session);
                    break;
                default:
                    throw new RuntimeException("ComponentEnum not defined for fetching userContent");
            }*/


        }
        return true;
    }

    private void deleteResources(ComponentEnum componentEnum, SearchResults<Resource> resources, Session session) throws OperationException {
        for (Resource resource: resources.getResults()) {
            userUgcComponentFactory.getOperation(componentEnum).delete(resource, session);
        }
    }

    @Override
    public boolean deleteUserAccount(ResourceResolver resourceResolver, String userId) throws RepositoryException {
        Session session = resourceResolver.adaptTo(Session.class);

        UserManager userManager = resourceResolver.adaptTo(UserManager.class);
        boolean revertToAutoSave = false;
        try {
            Authorizable authorizable = userManager.getAuthorizable(userId);
            if (userManager.isAutoSave()) {
                userManager.autoSave(false);
                revertToAutoSave = true;
            }
            authorizable.remove();
            session.save();
        } catch ( RepositoryException e){
            throw new RepositoryException(e);
        }finally {
            if (revertToAutoSave){
                try {
                    userManager.autoSave(true);
                } catch (RepositoryException e) {
                    log.warn("Cannot revert autosave mode of user manager", e.getMessage());
                }
            }
        }
        return false;
    }

    private Map<ComponentEnum, UgcFilter> getUserUgcFilters(List<ComponentEnum> componentEnumList, String userId){
        Map <ComponentEnum, UgcFilter> componentEnumUgcFilterMap = new HashMap<ComponentEnum, UgcFilter>();
        for (ComponentEnum componentEnum: componentEnumList) {
            componentEnumUgcFilterMap.put(componentEnum, userUgcComponentFactory.getUserUgcFilter(componentEnum).getUgcFilter(userId));
        }
        return componentEnumUgcFilterMap;
    }
}
