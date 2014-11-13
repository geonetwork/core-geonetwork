package org.fao.geonet;

import org.fao.geonet.utils.IO;
import org.springframework.core.io.FileSystemResourceLoader;
import org.springframework.mock.web.MockServletContext;

import java.nio.file.Path;

/**
 * @author Jesse on 10/17/2014.
 */
public class GeonetMockServletContext extends MockServletContext {
    private Path resourcePath = IO.toPath(".");

    public GeonetMockServletContext() {
        super(new FileSystemResourceLoader());
    }

    public void setTestClass(Class testClass) {
        this.resourcePath = AbstractCoreIntegrationTest.getWebappDir(testClass);
    }

    @Override
    protected String getResourceLocation(String path) {
        return this.resourcePath.resolve(super.getResourceLocation(path)).toString();
    }
}
