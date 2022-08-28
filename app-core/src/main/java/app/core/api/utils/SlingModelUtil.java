package app.core.api.utils;

import app.core.api.annotations.RestField;
import app.core.api.beans.ServletProperties;
import app.core.api.restfields.RestFieldCore;
import app.core.api.services.ResolverProvider;
import app.core.api.storage.ServletMappingStorage;
import org.apache.commons.lang3.StringUtils;
import org.apache.jackrabbit.api.security.user.User;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.*;
import org.apache.sling.jcr.base.util.AccessControlUtil;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public final class SlingModelUtil {

    private static final Logger LOG = LoggerFactory.getLogger(SlingModelUtil.class);

    private SlingModelUtil() {
    }

    public static void updateModel(Object model) {
        final Class<?> modelClass = model.getClass();
        if (!modelClass.isAnnotationPresent(Model.class)) {
            return;
        }
        final Field[] fields = aggregateFields(modelClass);
        Arrays.stream(fields)
                .filter(field -> Resource.class.equals(field.getType()))
                .filter(ReflectionUtil.isAnnotatedWith(Self.class))
                .map(ReflectionUtil.getFiledValue(model))
                .findFirst()
                .map(Resource.class::cast)
                .ifPresent(updateResourceFields(model, fields));
    }

    private static Field[] aggregateFields(Class<?> modelClass) {
        return aggregateFieldsStream(modelClass).toArray(Field[]::new);
    }

    private static Stream<Field> aggregateFieldsStream(Class<?> modelClass) {
        final Stream<Field> fieldStream = Stream.of(modelClass.getDeclaredFields());
        final Class<?> superclass = modelClass.getSuperclass();
        if (superclass != null) {
            return Stream.concat(fieldStream, aggregateFieldsStream(superclass));
        }
        return fieldStream;
    }

    public static Resource createModelResource(SlingHttpServletRequest request, String path, Class aClass) {
        final String id = RestResourceUtil.generateId();
        final String resourceType = "";//ServletMapping.getResourceTypeFromRequest(request);
        Resource resource = "rep:User".equals(resourceType)
                ? createUser(request, id, path)
                : createResource(request.getResourceResolver(), path + "/" + id, resourceType);
        initResourceProperties(resource, id);
        updateModel(resource.adaptTo(aClass));
        return resource;
    }

    private static void initResourceProperties(Resource resource, String id) {
        final Optional<Resource> resourceOptional = Optional.ofNullable(resource);
        resourceOptional.map(res -> res.adaptTo(Node.class))
                .ifPresent(node -> {
                    try {
                        node.addMixin(RestResourceUtil.NT_IP_RESOURCE_MIXIN);
                    } catch (RepositoryException e) {
                        LOG.error(e.getMessage(), e);
                    }
                });
        resourceOptional.map(res -> res.adaptTo(ModifiableValueMap.class))
                .ifPresent(properties -> properties.put(RestResourceUtil.REQUEST_PARAMETER_IP_RESOURCE_ID, id));
    }

    public static Object createModel(SlingHttpServletRequest request, Class modelClass) {
        final String path = request.getParameter("path");
        if (StringUtils.isEmpty(path)) {
            return null;
        }
        final String id = RestResourceUtil.generateId();
        ServletProperties servletProperties = ServletMappingStorage.getInstance().getPropertiesFromRequest(request);
        final String resourceType = servletProperties.getJcrPrimaryType();
        final Resource resource = "rep:User".equals(resourceType)
                ? createUser(request, id, path)
                : createResource(request.getResourceResolver(), path + "/" + id, resourceType);

        final Object model = resource.adaptTo(modelClass);
        updateModel(model);
        return model;
    }

    private static Resource createUser(SlingHttpServletRequest request, String id, String path) {
        final ResourceResolver resolver = request.getResourceResolver();

        return Optional.ofNullable(resolver.adaptTo(Session.class))
                .map(session -> {
                    try {
                        return AccessControlUtil.getUserManager(session);
                    } catch (RepositoryException e) {
                        LOG.error(e.getMessage(), e);
                        return null;
                    }
                })
                .map(getUserResourcePath(id, path))
                .map(resolver::getResource)
                .orElse(null);
    }

    private static Function<UserManager, String> getUserResourcePath(String id, String path) {
        return userManager -> {
            try {
                final String userPath = path + "/" + StringUtils.substring(id, 0, 2);
                final User user = userManager.createUser(id, id, () -> id, userPath);
                return ""/*user.getPath()*/;
            } catch (RepositoryException e) {
                LOG.error(e.getMessage(), e);
                return null;
            }
        };
    }

    private static Resource createResource(ResourceResolver resourceResolver, String path, String resourceType) {
        try {
            return ResourceUtil.getOrCreateResource(resourceResolver, path, resourceType, null, true);
        } catch (PersistenceException e) {
            LOG.error(e.getMessage(), e);
            return null;
        }
    }

    public static <M> void updateModel(SlingHttpServletRequest request, Class<M> modelClass, Function<M, M> updateAction) {
        Optional.ofNullable(request.adaptTo(modelClass))
                .map(updateAction)
                .ifPresent(SlingModelUtil::updateModel);
    }

    private static Consumer<Resource> updateResourceFields(Object model, Field[] fields) {
        return resource -> {
            Map<String, Object> modelValues = Arrays.stream(fields)
                    .filter(ReflectionUtil.isNotAnnotatedWith(Self.class))
                    .filter(ReflectionUtil.isAnnotatedWith(ValueMapValue.class))
                    .filter(field -> ReflectionUtil.getFiledValue(model).apply(field) != null)
                    .collect(Collectors.toMap(ReflectionUtil::getNamedValue, ReflectionUtil.getFiledValue(model), (v1, v2) -> v1));
            if (modelValues.isEmpty()) {
                return;
            }
            final ResourceResolver resolver = null/*ResolverProvider.getResolver()*/;
            Optional.ofNullable(resolver)
                    .map(resourceResolver -> resourceResolver.getResource(resource.getPath()))
                    .map(res -> res.adaptTo(ModifiableValueMap.class))
                    .ifPresent(properties -> {
                        modelValues.forEach((k, v) -> properties.remove(k));
                        properties.putAll(modelValues);
                    });
            try {
                resolver.commit();
            } catch (PersistenceException e) {
                LOG.error("Error occurred while saving data [{}], for [{}]", modelValues, resource, e);
            }
        };
    }

    public static Resource getBaseUserModelResourceFromChildResources(Resource childResource) {
        ValueMap valueMap = childResource.getValueMap();
        if (valueMap.containsKey(RestResourceUtil.REQUEST_PARAMETER_RESOURCE_TYPE)) {
            String resourceType = valueMap.get(RestResourceUtil.REQUEST_PARAMETER_RESOURCE_TYPE, String.class);
            if (RestResourceUtil.IP_RESOURCE_TYPE_USER.equals(resourceType)) {
                return childResource;
            } else {
                return getBaseUserModelResourceFromChildResources(Objects.requireNonNull(childResource.getParent()));
            }
        }
        return getBaseUserModelResourceFromChildResources(Objects.requireNonNull(childResource.getParent()));
    }
}
