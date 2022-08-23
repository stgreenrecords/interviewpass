package com.sling.api.builder.core.utils;

import org.apache.sling.api.resource.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.servlet.http.HttpServletRequest;
import java.util.Iterator;
import java.util.Map;

public class ClosableResourceResolver implements AutoCloseable, ResourceResolver {

    private final ResourceResolver resourceResolver;

    private ClosableResourceResolver(ResourceResolver resourceResolver) {
        this.resourceResolver = resourceResolver;
    }

    @Deprecated
    public static ResourceResolver createAdministrative(ResourceResolverFactory factory, Map<String, Object> params) throws LoginException {
        return new ClosableResourceResolver(factory.getAdministrativeResourceResolver(params));
    }

    @Deprecated
    public static ResourceResolver createAdministrative(ResourceResolverFactory factory) throws LoginException {
        return createAdministrative(factory, null);
    }

    public static ResourceResolver create(ResourceResolverFactory factory, Map<String, Object> params) throws LoginException {
        return new ClosableResourceResolver(factory.getResourceResolver(params));
    }

    public static ResourceResolver creatService(ResourceResolverFactory factory, Map<String, Object> params) throws LoginException {
        return new ClosableResourceResolver(factory.getServiceResourceResolver(params));
    }

    @Override
    public @NotNull Map<String, Object> getPropertyMap() {
        return null;
    }

    @Override
    public void close() {
        if (resourceResolver != null && resourceResolver.isLive()) {
            resourceResolver.close();
        }
    }

    @Override
    public Resource resolve(HttpServletRequest httpServletRequest, String s) {
        return resourceResolver.resolve(httpServletRequest, s);
    }

    @Override
    public Resource resolve(String s) {
        return resourceResolver.resolve(s);
    }

    @Override
    @Deprecated
    public Resource resolve(HttpServletRequest httpServletRequest) {
        return resourceResolver.resolve(httpServletRequest);
    }

    @Override
    public String map(String s) {
        return resourceResolver.map(s);
    }

    @Override
    public String map(HttpServletRequest httpServletRequest, String s) {
        return resourceResolver.map(httpServletRequest, s);
    }

    @Override
    public Resource getResource(String s) {
        return resourceResolver.getResource(s);
    }

    @Override
    public Resource getResource(Resource resource, String s) {
        return resourceResolver.getResource(resource, s);
    }

    @Override
    public String[] getSearchPath() {
        return resourceResolver.getSearchPath();
    }

    @Override
    public Iterator<Resource> listChildren(Resource resource) {
        return resourceResolver.listChildren(resource);
    }

    @Override
    public Resource getParent(Resource resource) {
        return null;
    }

    @Override
    public Iterable<Resource> getChildren(Resource resource) {
        return resourceResolver.getChildren(resource);
    }

    @Override
    public Iterator<Resource> findResources(String s, String s1) {
        return resourceResolver.findResources(s, s1);
    }

    @Override
    public Iterator<Map<String, Object>> queryResources(String s, String s1) {
        return resourceResolver.queryResources(s, s1);
    }

    @Override
    public boolean hasChildren(Resource resource) {
        return resourceResolver.hasChildren(resource);
    }

    @Override
    public ResourceResolver clone(Map<String, Object> map) throws LoginException {
        return resourceResolver.clone(map);
    }

    @Override
    public boolean isLive() {
        return resourceResolver.isLive();
    }

    @Override
    public String getUserID() {
        return resourceResolver.getUserID();
    }

    @Override
    public Iterator<String> getAttributeNames() {
        return resourceResolver.getAttributeNames();
    }

    @Override
    public Object getAttribute(String s) {
        return resourceResolver.getAttribute(s);
    }

    @Override
    public void delete(Resource resource) throws PersistenceException {
        resourceResolver.delete(resource);
    }

    @Override
    public Resource create(Resource resource, String s, Map<String, Object> map) throws PersistenceException {
        return resourceResolver.create(resource, s, map);
    }

    @Override
    public boolean orderBefore(@NotNull Resource parent, @NotNull String name, @Nullable String followingSiblingName) throws UnsupportedOperationException, PersistenceException, IllegalArgumentException {
        return false;
    }

    @Override
    public void revert() {
        resourceResolver.revert();
    }

    @Override
    public void commit() throws PersistenceException {
        resourceResolver.commit();
    }

    @Override
    public boolean hasChanges() {
        return resourceResolver.hasChanges();
    }

    @Override
    public String getParentResourceType(Resource resource) {
        return resourceResolver.getParentResourceType(resource);
    }

    @Override
    public String getParentResourceType(String s) {
        return resourceResolver.getParentResourceType(s);
    }

    @Override
    public boolean isResourceType(Resource resource, String s) {
        return resourceResolver.isResourceType(resource, s);
    }

    @Override
    public void refresh() {
        resourceResolver.refresh();
    }

    @Override
    public Resource copy(String s, String s1) throws PersistenceException {
        return null;
    }

    @Override
    public Resource move(String s, String s1) throws PersistenceException {
        return null;
    }

    @Override
    public <AdapterType> AdapterType adaptTo(Class<AdapterType> aClass) {
        return resourceResolver.adaptTo(aClass);
    }
}
