package interviewpass.restfields;

import com.sling.api.builder.core.annotations.RestField;
import com.sling.api.builder.core.restfields.AbstractResFieldCore;
import interviewpass.models.ProductModel;
import org.osgi.service.component.annotations.Component;

@Component(service = ProductRestField.class, immediate = true)
@RestField(servletExtension = "products", modelClass = ProductModel.class, pathToResources = "/content/api-builder-demo-store/products", searchPropertyValue = "product")
public class ProductRestField extends AbstractResFieldCore {
}
