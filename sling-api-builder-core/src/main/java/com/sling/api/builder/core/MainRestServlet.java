package com.sling.api.builder.core;

import com.sling.api.builder.core.restfields.RestFieldCore;
import com.sling.api.builder.core.utils.Constants;
import com.sling.api.builder.core.utils.RestResourceUtil;
import com.sling.api.builder.core.utils.ServletMappingStorage;
import org.apache.commons.lang3.StringUtils;

import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Component;

import javax.servlet.Servlet;
import javax.servlet.ServletException;
import java.io.IOException;
import java.util.Optional;
import java.util.function.Function;

@Component(
        service = Servlet.class,
        property = {
                "sling.servlet.paths=/bin/rest"
        }
)
public class MainRestServlet extends SlingAllMethodsServlet {

    @Override
    protected void doGet(final SlingHttpServletRequest request, final SlingHttpServletResponse response) throws ServletException, IOException {
        processMethod(request, response, restFieldCore -> restFieldCore.getObject(request));
    }

    @Override
    protected void doPost(final SlingHttpServletRequest request, final SlingHttpServletResponse response) throws ServletException, IOException {
        processMethod(request, response, restFieldCore -> restFieldCore.createObject(request));
    }

    @Override
    protected void doPut(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServletException, IOException {
        processMethod(request, response, restFieldCore -> restFieldCore.updateObject(request));
    }

    @Override
    protected void doDelete(SlingHttpServletRequest request, SlingHttpServletResponse response) throws ServletException, IOException {
        processMethod(request, response, restFieldCore -> restFieldCore.deleteObject(request));
    }

    private void processMethod(SlingHttpServletRequest request, SlingHttpServletResponse response,
                               Function<RestFieldCore, Object> method) throws IOException {
        writeResponse(
                Optional.of(processRequest(request))
                        .map(method)
                        .orElse(StringUtils.EMPTY), response);
    }

    private void writeResponse(Object responseObject, final SlingHttpServletResponse response) throws IOException {
        String responseJson = RestResourceUtil.toJson(responseObject);
        response.setContentType(Constants.RESPONSE_JSON_SETTING);
        response.getWriter().write(responseJson);
    }

    private RestFieldCore processRequest(SlingHttpServletRequest request) {
        return Optional.ofNullable(ServletMappingStorage.getPropertiesFromRequest(request))
                .map(properties -> {
                    BundleContext bundleContext = properties.getBundleContext();
                    return  (RestFieldCore) bundleContext.getService(properties.getServiceClass());
                })
                .orElse(null);
    }

}
