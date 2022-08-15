package com.sling.api.builder.core.binary;

import com.sling.api.builder.core.binary.impl.BinaryFile;
import com.sling.api.builder.core.binary.impl.Type;
import org.apache.sling.api.resource.ResourceResolver;

import java.util.List;
import java.util.Map;

public interface BinaryUploaderService {

    void updateRepositoryBinariesAndClose(ResourceResolver resourceResolver, String userID, Map<Type, List<BinaryFile>> files);

}
