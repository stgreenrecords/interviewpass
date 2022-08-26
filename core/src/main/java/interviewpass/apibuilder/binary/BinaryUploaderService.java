package interviewpass.apibuilder.binary;

import interviewpass.apibuilder.binary.impl.BinaryFile;
import interviewpass.apibuilder.binary.impl.Type;
import org.apache.sling.api.resource.ResourceResolver;

import java.util.List;
import java.util.Map;

public interface BinaryUploaderService {

    void updateRepositoryBinariesAndClose(ResourceResolver resourceResolver, String userID, Map<Type, List<BinaryFile>> files);

}
