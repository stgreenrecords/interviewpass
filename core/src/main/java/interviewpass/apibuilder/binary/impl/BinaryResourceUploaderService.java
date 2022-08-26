package interviewpass.apibuilder.binary.impl;

import interviewpass.apibuilder.binary.BinaryUploaderService;
import interviewpass.apibuilder.utils.RestResourceUtil;
import org.apache.jackrabbit.JcrConstants;
import org.apache.jackrabbit.commons.JcrUtils;
import org.apache.sling.api.resource.PersistenceException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceUtil;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jcr.Node;
import javax.jcr.RepositoryException;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Component(
        service = BinaryResourceUploaderService.class, immediate = true
)
public class BinaryResourceUploaderService implements BinaryUploaderService {

    private static final Logger LOG = LoggerFactory.getLogger(BinaryResourceUploaderService.class);
    private static final String FILE_NAME = "file";

    @Override
    public void updateRepositoryBinariesAndClose(ResourceResolver resourceResolver, String userID, Map<Type, List<BinaryFile>> files) {
        final Resource resource = RestResourceUtil.getUserResourcePath(resourceResolver, userID)
                .orElse(null);
        if (resource == null) {
            return;
        }
        files.entrySet().forEach(save(resource));
        try {
            resourceResolver.commit();
        } catch (PersistenceException e) {
            LOG.error(e.getMessage(), e);
        }
    }

    private Consumer<Map.Entry<Type, List<BinaryFile>>> save(Resource resource) {
        return entry -> {
            final ResourceResolver resourceResolver = resource.getResourceResolver();
            final String path = resource.getPath() + "/" + entry.getKey().getRelPath();
            try {
                final Resource fileResourceStore = ResourceUtil.getOrCreateResource(
                        resourceResolver,
                        path,
                        JcrConstants.NT_UNSTRUCTURED,
                        JcrConstants.NT_UNSTRUCTURED,
                        false);
                entry.getValue().forEach(saveFile(entry.getKey(), fileResourceStore));
            } catch (PersistenceException e) {
                LOG.error(e.getMessage(), e);
            }
        };
    }

    private Consumer<BinaryFile> saveFile(Type type, Resource fileResourceStore) {
        return binaryFile -> {
            try (BinaryFile binary = binaryFile) {
                processSingletonValues(type, fileResourceStore);
                final String fileName = ResourceUtil.createUniqueChildName(fileResourceStore, FILE_NAME);
                final Node store = fileResourceStore.adaptTo(Node.class);
                if (store == null) {
                    LOG.warn("Resource node to save is null {}", fileResourceStore);
                    return;
                }
                JcrUtils.putFile(store, fileName, "image/jpeg", binary.getIn());
            } catch (RepositoryException | IOException e) {
                LOG.error(e.getMessage(), e);
            }
        };
    }

    private void processSingletonValues(Type type, Resource fileResourceStore) throws PersistenceException {
        final ResourceResolver resourceResolver = fileResourceStore.getResourceResolver();
        final Resource fileResource = fileResourceStore.getChild(FILE_NAME);
        if (type.isSingleton() && fileResource != null) {
            resourceResolver.delete(fileResource);
        }
    }
}
