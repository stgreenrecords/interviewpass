package interviewpass.restfields;


import interviewpass.apibuilder.annotations.RestField;
import interviewpass.apibuilder.restfields.AbstractResFieldCore;
import interviewpass.models.Company;
import org.osgi.service.component.annotations.Component;


@Component(service = CompanyRestField.class, immediate = true)
@RestField(servletExtension = "companies", modelClass = Company.class, pathToResources = "/data/companies", searchPropertyValue = "company")
public class CompanyRestField extends AbstractResFieldCore{




}
