package app.core.api.storage;


import app.core.api.beans.ServletProperties;
import org.apache.sling.api.SlingHttpServletRequest;

import java.util.HashMap;
import java.util.Map;

public class ServletMappingStorage {

    private ServletMappingStorage() {
    }

    private static ServletMappingStorage servletMappingStorage;
    private final Map<String, ServletProperties> storage = new HashMap<>();

    public static ServletMappingStorage getInstance(){
        if (servletMappingStorage == null){
            servletMappingStorage = new ServletMappingStorage();
        }
        return servletMappingStorage;
    }

    public Map<String, ServletProperties> getServletsStorage() {
        return storage;
    }

    public ServletProperties getPropertiesFromRequest(final SlingHttpServletRequest request) {
        return storage.get(request.getRequestPathInfo().getExtension());
    }

}
