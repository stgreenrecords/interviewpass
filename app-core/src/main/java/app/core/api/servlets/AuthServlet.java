package app.core.api.servlets;

import app.core.api.services.ResolverProvider;
import app.core.api.storage.SessionStorage;
import app.core.api.utils.AppUtils;
import org.apache.jackrabbit.api.JackrabbitSession;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.jetbrains.annotations.NotNull;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.*;
import javax.servlet.Servlet;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component(
        service = Servlet.class,
        property = {
                "sling.servlet.paths=/bin/auth"
        }
)
public class AuthServlet extends SlingAllMethodsServlet {

    private final static Logger LOG = LoggerFactory.getLogger(ResolverProvider.class);

    @Reference
    private ResolverProvider resolverProvider;
    @Reference
    private Repository repository;

    @Override
    protected void doPost(final @NotNull SlingHttpServletRequest request, final @NotNull SlingHttpServletResponse response) throws ServletException, IOException {
        JackrabbitSession session = null;
        try {
            session = SessionStorage.getSessionStorage().getSessionFromRequest(request);
            if (session == null) {
                session = (JackrabbitSession) repository.login(AppUtils.extractCredentialFromRequest(request));
                SessionStorage.getSessionStorage().storage.put(request.getSession().getId(), session);
                response.setStatus(HttpServletResponse.SC_ACCEPTED);
            }
        } catch (RepositoryException e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            LOG.error("Unable to login {}", request.getRemoteUser());
        }
    }


}
