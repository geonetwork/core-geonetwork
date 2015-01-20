package org.fao.geonet.utils;

import org.fao.geonet.Constants;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.xml.transform.stream.StreamSource;

/**
 * @author Jesse on 1/20/2015.
 */
public class PathStreamSource extends StreamSource {
    private final Path path;

    public PathStreamSource(Path path) {
        this.path = path;
    }

    @Override
    public void setInputStream(InputStream inputStream) {
        throw new UnsupportedOperationException();
    }

    @Override
    public InputStream getInputStream() {
        try {
            return Files.newInputStream(this.path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setReader(Reader reader) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Reader getReader() {
        try {
            return Files.newBufferedReader(this.path, Constants.CHARSET);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
