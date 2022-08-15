package com.sling.api.builder.core.utils;


import com.sling.api.builder.core.beans.ServletProperties;
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
