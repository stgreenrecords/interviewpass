package interviewpass.apibuilder.binary.impl;

import java.io.IOException;
import java.io.InputStream;

public class BinaryFile implements AutoCloseable {

    private final InputStream in;

    BinaryFile(InputStream in) {
        this.in = in;
    }

    @Override
    public void close() throws IOException {
        if (in != null) {
            in.close();
        }
    }

    public InputStream getIn() {
        return in;
    }
}
