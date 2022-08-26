package interviewpass.apibuilder;

import interviewpass.apibuilder.utils.Constants;
import interviewpass.apibuilder.utils.RestResourceUtil;
import interviewpass.apibuilder.utils.ServletMappingStorage;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.SlingHttpServletResponse;
import org.apache.sling.api.servlets.SlingAllMethodsServlet;
import org.osgi.service.component.annotations.Component;

import javax.servlet.Servlet;
import java.io.IOException;
import java.util.StringJoiner;

@Component(
        service = Servlet.class,
        property = {
                "sling.servlet.paths=/bin/models"
        }
)
public class TestedModelServlet extends SlingAllMethodsServlet {

    @Override
    protected void doGet(final SlingHttpServletRequest request, final SlingHttpServletResponse response){
        StringJoiner modelsObject = new StringJoiner(",", "[", "]");
        ServletMappingStorage.getServletsStorage().forEach((key, value) -> {
            try {
                String responseJson = RestResourceUtil.toJson(value.getModelClass().getConstructor().newInstance());
                modelsObject.add("{\"className\":\"" + value.getModelClass().getSimpleName() + "\",\"extension\":\""+key+"\",\"fields\":" + responseJson + "}");
            } catch (Exception e) {
                e.printStackTrace();
            }
       });
        response.setContentType(Constants.RESPONSE_JSON_SETTING);
        try {
            response.getWriter().write(modelsObject.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
