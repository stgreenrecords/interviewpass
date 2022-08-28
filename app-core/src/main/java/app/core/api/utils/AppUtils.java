package app.core.api.utils;

import app.core.api.storage.SessionStorage;
import com.google.common.hash.Hashing;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Session;
import javax.jcr.SimpleCredentials;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Objects;
import java.util.Optional;
import java.util.zip.GZIPOutputStream;

@Component(
        service = AppUtils.class
)
public class AppUtils {

    private static final Logger LOG = LoggerFactory.getLogger(AppUtils.class);

    public static final String LOGIN_REQUEST_PARAM = "ipLogin";
    public static final String PASS_REQUEST_PARAM = "ipPass";

    @Reference
    private ResourceResolverFactory resolverFactory;

    private ResourceResolver resourceResolver;

    public static byte[] gzip(String data) throws Exception {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        GZIPOutputStream gzip = new GZIPOutputStream(byteArrayOutputStream);
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(gzip, StandardCharsets.UTF_8);
        outputStreamWriter.write(data);
        outputStreamWriter.close();
        return byteArrayOutputStream.toByteArray();
    }

    public ResourceResolver getResolver() {
        try {
            resourceResolver = resolverFactory.getAdministrativeResourceResolver(null);
        } catch (LoginException e) {
            e.printStackTrace();
        }
        return resourceResolver;
    }

    public static boolean isSessionValid(final SlingHttpServletRequest request){
        return Optional.ofNullable(request)
                .map(req -> SessionStorage.getSessionStorage().getSessionFromRequest(req))
                .map(Session::isLive)
                .isPresent();
    }

    public static void writeResponse(final Object responseObject, final SlingHttpServletResponse response) throws IOException {
        String responseJson = RestResourceUtil.toJson(responseObject);
        response.setContentType(Constants.RESPONSE_JSON_SETTING);
        response.getWriter().write(responseJson);
    }

    public static SimpleCredentials extractCredentialFromRequest(final SlingHttpServletRequest request){
        return new SimpleCredentials(request.getParameter(LOGIN_REQUEST_PARAM), DigestUtils.sha256Hex(request.getParameter(LOGIN_REQUEST_PARAM)).toCharArray());
    }


}
