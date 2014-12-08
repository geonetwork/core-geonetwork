package org.fao.geonet.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * A file handler for urls that are created by {@link org.fao.geonet.utils.IO#toURL(java.net.URI)} and represent a url to
 * a file.
 *
* @author Jesse on 11/18/2014.
*/
class FileSystemSpecificStreamHandler extends URLStreamHandler {
    @Override
    protected URLConnection openConnection(URL u) throws IOException {
        try {
            final Path path = IO.toPath(new URI(u.toExternalForm().replace('\\', '/')));
            return new URLConnection(u) {

                @Override
                public void connect() throws IOException {
                    // nothing to do
                }

                @Override
                public InputStream getInputStream() throws IOException {
                    return Files.newInputStream(path);
                }

                @Override
                public int getContentLength() {
                    try {
                        final long size = Files.size(path);
                        if (size > Integer.MAX_VALUE) {
                            throw new AssertionError(path  + " size is too large for the getContentLength method.  " +
                                                     "It is greater than Integer.MAX_VALUE: " + size);
                        }
                        return (int) size;
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }

                @Override
                public String getContentType() {
                    try {
                        return Files.probeContentType(path);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }

                @Override
                public long getLastModified() {
                    try {
                        return Files.getLastModifiedTime(path).toMillis();
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }

                @Override
                public OutputStream getOutputStream() throws IOException {
                    return Files.newOutputStream(path);
                }
            };
        } catch (URISyntaxException e1) {
            throw new Error(e1);
        }
    }
}
