package interviewpass.apibuilder.utils;

import com.google.common.collect.ImmutableList;
import interviewpass.apibuilder.binary.impl.BinaryFile;
import interviewpass.apibuilder.binary.impl.BinaryFileFactory;
import interviewpass.apibuilder.binary.impl.Type;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.sling.api.SlingHttpServletRequest;
import org.apache.sling.api.request.RequestParameter;

import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class BinaryFileUtil {

    private static final String MULTIPLE_PARAMETERS_PATTERN = "%s-%d";

    private BinaryFileUtil() {
    }

    public static EnumMap<Type, List<BinaryFile>> extractFromRequest(SlingHttpServletRequest request, Map<Type, Boolean> keys) {
        return keys.entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        extractBinaryFiles(request),
                        (oldValue, newValue) -> oldValue,
                        () -> new EnumMap<>(Type.class)));
    }

    private static Function<Map.Entry<Type, Boolean>, List<BinaryFile>> extractBinaryFiles(SlingHttpServletRequest request) {
        return entry -> {
            RequestParameter requestParameter = request.getRequestParameter(entry.getKey().getName());
            if (requestParameter != null && BooleanUtils.isFalse(entry.getValue())) {
                return Collections.singletonList(BinaryFileFactory.fromRequestParameter(requestParameter));
            }
            ImmutableList.Builder<BinaryFile> listBuilder = ImmutableList.builder();
            int index = 0;
            while ((requestParameter = getRequestParameter(request, entry, index)) != null) {
                listBuilder.add(BinaryFileFactory.fromRequestParameter(requestParameter));
                index++;
            }
            return listBuilder.build();
        };
    }

    private static RequestParameter getRequestParameter(SlingHttpServletRequest request, Map.Entry<Type, Boolean> entry,
                                                        int index) {
        return request.getRequestParameter(String.format(MULTIPLE_PARAMETERS_PATTERN, entry.getKey().getName(), index));
    }
}
