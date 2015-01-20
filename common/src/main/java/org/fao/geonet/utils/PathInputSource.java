package org.fao.geonet.utils;

import org.fao.geonet.Constants;
import org.xml.sax.InputSource;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * An input source backed by a java nio path.
 *
 * @author Jesse on 1/20/2015.
 */
public class PathInputSource extends InputSource {
    private final Path path;

    public PathInputSource(Path resource) {
        this.path = resource;
    }

    @Override
    public void setByteStream(InputStream byteStream) {
        throw new UnsupportedOperationException();
    }

    @Override
    public InputStream getByteStream() {
        try {
            return Files.newInputStream(this.path);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setCharacterStream(Reader characterStream) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Reader getCharacterStream() {
        try {
            final Charset cs;
            if (getEncoding() != null) {
                cs = Charset.forName(getEncoding());
            } else {
                cs = Constants.CHARSET;
            }
            return Files.newBufferedReader(this.path, cs);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
