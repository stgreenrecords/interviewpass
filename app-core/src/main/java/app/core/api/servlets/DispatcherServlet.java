package app.core.api.servlets;

import app.core.api.restfields.RestFieldCore;
import app.core.api.utils.AppUtils;
import app.core.api.storage.ServletMappingStorage;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.jetbrains.annotations.NotNull;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Component;

import javax.servlet.Servlet;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import java.util.function.Function;

@Component(
        service = Servlet.class,
        property = {
                "sling.servlet.paths=/bin/rest"
        }
)
public class DispatcherServlet extends SlingAllMethodsServlet {

    @Override
    protected void doGet(final @NotNull SlingHttpServletRequest request, final @NotNull SlingHttpServletResponse response) throws IOException {
        processMethod(request, response, restFieldCore -> restFieldCore.getObject(request));
    }

    @Override
    protected void doPost(final @NotNull SlingHttpServletRequest request, final @NotNull SlingHttpServletResponse response) throws IOException {
        processMethod(request, response, restFieldCore -> restFieldCore.createObject(request));
    }

    @Override
    protected void doPut(final @NotNull SlingHttpServletRequest request, final @NotNull SlingHttpServletResponse response) throws IOException {
        processMethod(request, response, restFieldCore -> restFieldCore.updateObject(request));
    }

    @Override
    protected void doDelete(final @NotNull SlingHttpServletRequest request, final @NotNull SlingHttpServletResponse response) throws IOException {
        processMethod(request, response, restFieldCore -> restFieldCore.deleteObject(request));
    }

    private void processMethod(SlingHttpServletRequest request, SlingHttpServletResponse response,
                               Function<RestFieldCore, Object> method) throws IOException {
        boolean sessionValid = AppUtils.isSessionValid(request);
        if (!sessionValid) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        }
        AppUtils.writeResponse(
                Optional.ofNullable(processRequest(request))
                        .map(method)
                        .orElse(request.getResourceResolver().getUserID()), response);
    }


    private RestFieldCore processRequest(SlingHttpServletRequest request) {
        return Optional.ofNullable(ServletMappingStorage.getInstance().getPropertiesFromRequest(request))
                .map(properties -> {
                    BundleContext bundleContext = properties.getBundleContext();
                    return (RestFieldCore) bundleContext.getService(properties.getServiceClass());
                })
                .orElse(null);
    }

}
