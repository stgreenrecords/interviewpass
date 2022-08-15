package com.sling.api.builder.core.binary.impl;

import org.apache.sling.api.request.RequestParameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

public final class BinaryFileFactory {

    private static final Logger LOG = LoggerFactory.getLogger(BinaryFileFactory.class);

    private BinaryFileFactory() {
    }

    public static BinaryFile fromStream(InputStream in) {
        return create(in);
    }

    public static BinaryFile fromRequestParameter(RequestParameter requestParameter) {
        return Optional.ofNullable(requestParameter)
                .map(BinaryFileFactory::getRequestParameterInputStream)
                .map(BinaryFileFactory::create)
                .orElse(empty());
    }

    private static BinaryFile create(InputStream inputStream) {
        return Optional.ofNullable(inputStream)
                .map(BinaryFile::new)
                .orElseGet(BinaryFileFactory::empty);
    }

    private static BinaryFile empty() {
        return new BinaryFile(emptyStream());
    }

    private static InputStream emptyStream() {
        return new ByteArrayInputStream(new byte[0]);
    }

    private static InputStream getRequestParameterInputStream(RequestParameter requestParameter) {
        try {
            return requestParameter.getInputStream();
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
            return emptyStream();
        }
    }
}
