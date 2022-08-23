package interviewpass.restfields;


import com.sling.api.builder.core.annotations.RestField;
import com.sling.api.builder.core.restfields.AbstractResFieldCore;
import interviewpass.models.UserModel;
import org.osgi.service.component.annotations.Component;


@Component(service = UserRestModel.class, immediate = true)
@RestField(servletExtension = "users", modelClass = UserModel.class, pathToResources = "/content/home/users", searchPropertyValue = "user")
public class UserRestModel extends AbstractResFieldCore{


}
