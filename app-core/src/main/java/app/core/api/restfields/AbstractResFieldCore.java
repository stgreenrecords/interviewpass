package app.core.api.restfields;

import app.core.api.annotations.RestField;
import app.core.api.beans.ServletProperties;
import app.core.api.utils.RestResourceUtil;
import app.core.api.storage.ServletMappingStorage;
import app.core.api.utils.SlingModelUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;

import javax.jcr.query.Query;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component(
        service = RestFieldCore.class
)
@RestField(servletExtension = "", modelClass = Object.class, pathToResources = "/data")
public abstract class AbstractResFieldCore implements RestFieldCore {

    private static final String DEFAULT_GET_QUERY = "SELECT * FROM [%s] AS resource WHERE ISDESCENDANTNODE([%s]) AND resource.[%s] = '%s'";

    @Modified
    @Activate
    protected void activate(ComponentContext componentContext) {
        Optional.ofNullable(this.getClass().getAnnotation(RestField.class))
                .filter(restField -> StringUtils.isNotEmpty(restField.servletExtension()))
                .ifPresent(restField -> ServletMappingStorage.getInstance().getServletsStorage().
                        put(restField.servletExtension(),
                                new ServletProperties(
                                        restField.modelClass(),
                                        componentContext.getServiceReference(),
                                        restField.pathToResources(),
                                        restField.jcrPrimaryType(),
                                        restField.searchPropertyName(),
                                        restField.searchPropertyValue(),
                                        componentContext.getBundleContext()))
                );
    }

    @Override
    public Object getObject(SlingHttpServletRequest request) {
        ServletProperties servletProperties = ServletMappingStorage.getInstance().getPropertiesFromRequest(request);
        Class<?> modelClass = Optional.ofNullable(servletProperties)
                .map(ServletProperties::getModelClass)
                .orElse(null);
        return Optional.of(request.getResourceResolver())
                .map(resolver -> resolver.findResources(formatDefaultQuery(servletProperties), Query.SQL))
                .map(RestResourceUtil::iteratorToOrderedStream)
                .orElse(Stream.empty())
                .map(resource -> resource.adaptTo(modelClass))
                .filter(Objects::nonNull)
                .limit(getLimit(request))
                .collect(Collectors.toList());
    }

    @Override
    public Object updateObject(SlingHttpServletRequest request) {
        return createOrUpdateModel(request, Optional.ofNullable(ServletMappingStorage.getInstance().getPropertiesFromRequest(request))
                .map(ServletProperties::getModelClass)
                .orElse(null));
    }

    @Override
    public Object createObject(SlingHttpServletRequest request) {
        return createOrUpdateModel(request, Optional.ofNullable(ServletMappingStorage.getInstance().getPropertiesFromRequest(request))
                .map(ServletProperties::getModelClass)
                .orElse(null));
    }

    @Override
    public Object deleteObject(SlingHttpServletRequest request) {
        return null;
    }

    private String formatDefaultQuery(ServletProperties servletProperties) {
        return String.format(DEFAULT_GET_QUERY,
                servletProperties.getJcrPrimaryType(),
                servletProperties.getPathToResources(),
                servletProperties.getSearchPropertyName(),
                servletProperties.getSearchPropertyValue());
    }

    public Object createOrUpdateModel(SlingHttpServletRequest request, Class modelClass) {
        final Object model = request.adaptTo(modelClass);
        if (model == null) {
            return null;
        }
        SlingModelUtil.updateModel(model);
        return model;
    }


}
