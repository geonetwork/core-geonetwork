package org.fao.geonet;

import org.fao.geonet.utils.IO;

import org.springframework.core.io.FileSystemResourceLoader;
import org.springframework.mock.web.MockServletContext;

import java.net.URISyntaxException;

import java.nio.file.Path;
import java.net.URL;
import java.nio.file.Paths;

/**
 * @author Jesse on 10/17/2014.
 */
public class GeonetMockServletContext extends MockServletContext {
    private Path resourcePath = IO.toPath("");

    public GeonetMockServletContext() {
        super(new FileSystemResourceLoader());
    }

    public void setTestClass(Class testClass) {
        this.resourcePath = getServletRelativePath(AbstractCoreIntegrationTest.getWebappDir(testClass));
    }

    @Override
    protected String getResourceLocation(String path) {
        final URL resource = Thread.currentThread().getContextClassLoader().getResource(path);
        if (resource != null) {
            try {
                return Paths.get(resource.toURI()).toString();
            } catch (URISyntaxException e) {
                throw new RuntimeException(e);
            }
        }
        String resourceLocation = super.getResourceLocation(path);
        if (resourceLocation.startsWith("/") || resourceLocation.startsWith("\\")) {
            resourceLocation = resourceLocation.substring(1);
        }
        return this.resourcePath.resolve(resourceLocation).toString();
    }

    private Path getServletRelativePath(Path webappDir) {
        Path servletRelativePath = IO.toPath("").toAbsolutePath();
        return servletRelativePath.relativize(webappDir.toAbsolutePath());
    }

}
