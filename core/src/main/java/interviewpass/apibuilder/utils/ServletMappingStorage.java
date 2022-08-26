package interviewpass.apibuilder.utils;


import interviewpass.apibuilder.beans.ServletProperties;
import org.apache.sling.api.SlingHttpServletRequest;

import java.util.HashMap;
import java.util.Map;

public class ServletMappingStorage {

    private ServletMappingStorage() {
    }

    private static final Map<String, ServletProperties> SERVLETS_STORAGE = new HashMap();

    public static Map<String, ServletProperties> getServletsStorage() {
        return SERVLETS_STORAGE;
    }


    public static ServletProperties getPropertiesFromRequest(final SlingHttpServletRequest request) {
        return SERVLETS_STORAGE.get(request.getRequestPathInfo().getExtension());
    }

}
