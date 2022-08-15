package com.sling.api.builder.core.beans;

import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

public class ServletProperties {

    private Class<?> modelClass;
    private ServiceReference serviceClass;
    private String pathToResources;
    private String jcrPrimaryType;
    private String searchPropertyName;
    private String searchPropertyValue;
    private BundleContext bundleContext;

    public ServletProperties(Class<?> modelClass, ServiceReference serviceClass, String pathToResources, String jcrPrimaryType, String searchPropertyName, String searchPropertyValue, BundleContext bundleContext) {
        this.modelClass = modelClass;
        this.serviceClass = serviceClass;
        this.pathToResources = pathToResources;
        this.jcrPrimaryType = jcrPrimaryType;
        this.searchPropertyName = searchPropertyName;
        this.searchPropertyValue = searchPropertyValue;
        this.bundleContext = bundleContext;
    }

    public Class<?> getModelClass() {
        return modelClass;
    }

    public void setModelClass(Class<?> modelClass) {
        this.modelClass = modelClass;
    }

    public ServiceReference getServiceClass() {
        return serviceClass;
    }

    public void setServiceClass(ServiceReference serviceClass) {
        this.serviceClass = serviceClass;
    }

    public String getPathToResources() {
        return pathToResources;
    }

    public void setPathToResources(String pathToResources) {
        this.pathToResources = pathToResources;
    }

    public String getJcrPrimaryType() {
        return jcrPrimaryType;
    }

    public void setJcrPrimaryType(String jcrPrimaryType) {
        this.jcrPrimaryType = jcrPrimaryType;
    }

    public String getSearchPropertyName() {
        return searchPropertyName;
    }

    public void setSearchPropertyName(String searchPropertyName) {
        this.searchPropertyName = searchPropertyName;
    }

    public String getSearchPropertyValue() {
        return searchPropertyValue;
    }

    public void setSearchPropertyValue(String searchPropertyValue) {
        this.searchPropertyValue = searchPropertyValue;
    }

    public BundleContext getBundleContext() {
        return bundleContext;
    }

    public void setBundleContext(BundleContext bundleContext) {
        this.bundleContext = bundleContext;
    }
}
