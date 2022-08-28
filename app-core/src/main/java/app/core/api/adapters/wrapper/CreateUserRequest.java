package app.core.api.adapters.wrapper;

import app.core.api.utils.RestResourceUtil;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.wrappers.SlingHttpServletRequestWrapper;

public class CreateUserRequest extends SlingHttpServletRequestWrapper implements SlingHttpServletRequest {

    private String id;

    public CreateUserRequest(SlingHttpServletRequest wrappedRequest) {
        super(wrappedRequest);
    }

    private CreateUserRequest(SlingHttpServletRequest wrappedRequest, String id) {
        super(wrappedRequest);
        this.id = id;
    }

    public static CreateUserRequest of(SlingHttpServletRequest wrappedRequest, String id) {
        return new CreateUserRequest(wrappedRequest, id);
    }

    @Override
    public String getParameter(String name) {
        if (RestResourceUtil.REQUEST_PARAMETER_ID.equals(name)) {
            return id;
        }
        return super.getParameter(name);
    }
}
