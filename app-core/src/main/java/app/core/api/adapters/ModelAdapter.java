package app.core.api.adapters;

import app.core.api.utils.ReflectionUtil;
import app.core.api.utils.RestResourceUtil;
import app.core.api.utils.SlingModelUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.adapter.AdapterFactory;
import org.apache.sling.api.resource.ModifiableValueMap;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.jetbrains.annotations.NotNull;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleListener;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.io.IOException;
import java.util.*;

@Component(
        service = AdapterFactory.class, immediate = true
)
public class ModelAdapter implements AdapterFactory {

    private static final Logger LOG = LoggerFactory.getLogger(ModelAdapter.class);

    private static final String API_MODELS_PACKAGE = "app.core.impl.models.api";

    @Reference
    private ConfigurationAdmin configAdmin;

    @Activate
    public void activate(ComponentContext componentContext) {
        Bundle bundle = componentContext.getBundleContext().getBundle();
        String propertyAdaptables = "adaptables";
        String propertyAdapters = "adapters";
        try {
            Configuration config = configAdmin.getConfiguration(
                    this.getClass().getName());
            final Dictionary props = Optional.ofNullable(config.getProperties()).orElse(new Hashtable<>());

            final String adaptablesOld = (String) props.get(propertyAdaptables);
            final String[] adaptersOld = (String[]) props.get(propertyAdapters);
            final String adaptables = SlingHttpServletRequest.class.getName();
            final String[] adapters = ReflectionUtil.getAllClassesFromPackage(bundle.getBundleContext().getBundle(), API_MODELS_PACKAGE);
            if (adaptersOld != null && adaptables.equals(adaptablesOld) && new HashSet<>(Arrays.asList(adapters)).containsAll(Arrays.asList(adaptersOld))) {
                return;
            }
            props.put(propertyAdaptables, adaptables);
            props.put(propertyAdapters, adapters);
            config.update(props);
        } catch (IOException e) {
            LOG.info(e.getMessage());
        }

    }

    private BundleListener addBundleListener(ComponentContext componentContext) {
        return bundleEvent -> {

        };
    }

    @Override
    public <AdapterType> AdapterType getAdapter(@NotNull Object adaptable, @NotNull Class<AdapterType> aClass) {
        if (adaptable instanceof SlingHttpServletRequest) {
            SlingHttpServletRequest request = (SlingHttpServletRequest) adaptable;
            return adaptRequestToModel(request, aClass);
        }
        return null;
    }

    private <AdapterType> AdapterType adaptRequestToModel(SlingHttpServletRequest request, Class<AdapterType> aClass) {
        final String id = RestResourceUtil.getId(request);
        final String path = request.getParameter("path");
        Resource modelResource = null;
        if (StringUtils.isNotEmpty(path)) {
            modelResource = SlingModelUtil.createModelResource(request, path, aClass);
            updateNewResource(modelResource);
        } else if (StringUtils.isNotEmpty(id)) {
            modelResource = RestResourceUtil.getResourceByID(request.getResourceResolver()).apply(id);
        }

        return (AdapterType) Optional.ofNullable(modelResource)
                .map(resource -> resource.adaptTo(aClass))
                .map(model -> setPropertyToModel(request, model))
                .orElse(null);
    }

    private void updateNewResource(Resource modelResource) {
        if (modelResource == null) {
            LOG.warn("Model resource is null");
            return;
        }
        final Node resourceNode = modelResource.adaptTo(Node.class);
        final ModifiableValueMap properties = modelResource.adaptTo(ModifiableValueMap.class);
        if (resourceNode == null || properties == null) {
            LOG.warn("Model node [] or properties [] is null", resourceNode, properties);
            return;
        }
        final String id = RestResourceUtil.generateId();
        properties.put(RestResourceUtil.REQUEST_PARAMETER_IP_RESOURCE_ID, id);
        try {
            resourceNode.addMixin(RestResourceUtil.NT_IP_RESOURCE_MIXIN);
            modelResource.getResourceResolver().commit();
        } catch (RepositoryException | PersistenceException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private Object setPropertyToModel(SlingHttpServletRequest request, Object model) {
        Enumeration parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            Optional.of((String) parameterNames.nextElement())
                    .ifPresent(parameter -> ReflectionUtil.setFieldValueDeep(parameter, request.getParameterValues(parameter), model));
        }
        return model;
    }

}
