package com.sling.api.builder.core.resolver;

import com.sling.api.builder.core.restfields.RestFieldCore;
import org.apache.commons.lang3.StringUtils;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.apache.sling.commons.osgi.PropertiesUtil;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Modified;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Map;

@Component(
        service = ResolverProvider.class, immediate = true
)
public final class ResolverProvider {

    private static Logger LOG = LoggerFactory.getLogger(ResolverProvider.class);
    private static ResourceResolver RESOLVER;
    //@Property
    private static String SERVICE = "provider.resolver.service";

    @Reference
    private ResourceResolverFactory rrf;

    public static ResourceResolver getResolver() {
        return RESOLVER;
    }

    @Activate
    @Modified
    public void init(Map<String, Object> properties) {
        final String service = PropertiesUtil.toString(properties.get(SERVICE), "resolver");
        try {
               RESOLVER = rrf.getServiceResourceResolver(Collections.singletonMap(ResourceResolverFactory.SUBSERVICE, "resolver"));
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
        }
    }
}
