package org.fao.geonet.utils.nio;

import org.fao.geonet.utils.IO;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.Path;

/**
 * It has been found that for StreamSource (and perhaps InputSource in some cases) both the reader and input stream methods are called
 * to see if they are non-null but only one of them is closed.  This class provides a solution that both
 * {@link org.fao.geonet.utils.nio.PathStreamSource} and {@link org.fao.geonet.utils.nio.PathInputSource} use to fix this problem.
 *
 * There are two obvious solutions to this problem of resource leaks:
 *
 * <ol>
 *     <li>Have the close method on both the input stream and the reader close both resources</li>
 *     <li>
 *         If one of the methods is called then return null from the other.  For example if getInputStream is called then getReader
 *         should return null (and vice-versa).  This solution is used in this class because it results in a 50% reduction of
 *         open resources.
 *     </li>
 * </ol>
 * @author Jesse on 1/26/2015.
 */
public class PathSourceMixin {
    private final Path path;
    private InputStream inputStream;
    private Reader reader;

    public PathSourceMixin(Path path) {
        this.path = path;
    }

    public InputStream getInputStream() {
        if (this.reader != null) {
            return null;
        }
        if (this.inputStream == null) {
            try {
                this.inputStream = IO.newInputStream(this.path);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        return this.inputStream;
    }

    public Reader getReader(Charset charset) {
        if (this.inputStream != null) {
            return null;
        }
        if (this.reader == null) {
            try{
                this.reader = IO.newBufferedReader(path, charset);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return this.reader;
    }
}
