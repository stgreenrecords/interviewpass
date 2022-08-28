package app.core.impl.restfields;


import app.core.api.annotations.RestField;
import app.core.api.restfields.AbstractResFieldCore;
import app.core.impl.models.api.User;
import org.osgi.service.component.annotations.Component;


@Component(service = UsersRestField.class, immediate = true)
@RestField(servletExtension = "users", modelClass = User.class, pathToResources = "/data/users", searchPropertyValue = "user", jcrPrimaryType = "rep:User")
public class UsersRestField extends AbstractResFieldCore {


}
