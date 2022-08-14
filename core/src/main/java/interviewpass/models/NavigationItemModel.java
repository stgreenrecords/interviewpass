package interviewpass.models;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.models.annotations.DefaultInjectionStrategy;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.Self;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Model(adaptables = Resource.class, defaultInjectionStrategy = DefaultInjectionStrategy.OPTIONAL)
public class NavigationItemModel {

    private static final Logger LOGGER = LoggerFactory.getLogger(NavigationItemModel.class);

    @ValueMapValue(name = "jcr:title")
    private String title;

    @ValueMapValue
    private String hideFromNavigation;

    public String getTitle() {
        return title;
    }

    @Self
    private Resource resource;

    public String getHideFromNavigation() {
        return hideFromNavigation;
    }

    public String getPath() {
        return resource.getPath();
    }
}
