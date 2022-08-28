package app.core.api.services;

import app.core.api.storage.SessionStorage;
import com.google.common.collect.ImmutableMap;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Session;

@Component(
        service = ResolverProvider.class, immediate = true
)
public final class ResolverProvider {

    private static final Logger LOG = LoggerFactory.getLogger(ResolverProvider.class);

    @Reference
    private ResourceResolverFactory rrf;

    public ResourceResolver getResolver(final SlingHttpServletRequest request) {
        ResourceResolver resourceResolver = null;
        try {
            Session session = SessionStorage.getSessionStorage().getSessionFromRequest(request);
            resourceResolver = rrf.getResourceResolver(ImmutableMap.of("user.jcr.session", session));
        } catch (LoginException e) {
            LOG.error("Unable to get resolver from session");
        }
        return resourceResolver;
    }

}
