package app.core.api.storage;

import org.apache.jackrabbit.api.JackrabbitSession;
import org.apache.sling.api.SlingHttpServletRequest;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class SessionStorage {

    private static final String SESSION_COOKIE = "interview_session_id";

    public final Map<String, JackrabbitSession> storage = new HashMap<>();

    private static SessionStorage sessionStorage;

    private SessionStorage(){
    }

    public static SessionStorage getSessionStorage(){
        if (sessionStorage == null){
            sessionStorage = new SessionStorage();
        }
        return sessionStorage;
    }

    public JackrabbitSession getSessionFromRequest(SlingHttpServletRequest request){
        return Optional.ofNullable(request.getSession())
                .map(HttpSession::getId)
                .filter(storage::containsKey)
                .map(storage::get)
                .filter(JackrabbitSession::isLive)
                .orElse(null);
    }
}
