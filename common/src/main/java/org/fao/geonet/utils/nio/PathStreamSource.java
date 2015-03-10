package org.fao.geonet.utils.nio;

import org.fao.geonet.Constants;

import java.io.InputStream;
import java.io.Reader;
import java.nio.file.Path;
import javax.xml.transform.stream.StreamSource;

/**
 * @author Jesse on 1/20/2015.
 */
public class PathStreamSource extends StreamSource {
    private final PathSourceMixin pathSourceMixin;

    public PathStreamSource(Path path) {
        this.pathSourceMixin = new PathSourceMixin(path);
    }

    @Override
    public void setInputStream(InputStream inputStream) {
        throw new UnsupportedOperationException();
    }

    @Override
    public InputStream getInputStream() {
        return pathSourceMixin.getInputStream();
    }

    @Override
    public void setReader(Reader reader) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Reader getReader() {
        return this.pathSourceMixin.getReader(Constants.CHARSET);
    }
}
