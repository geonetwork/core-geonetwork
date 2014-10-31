package org.fao.geonet;

import org.springframework.core.io.FileSystemResourceLoader;
import org.springframework.mock.web.MockServletContext;

/**
 * @author Jesse on 10/17/2014.
 */
public class GeonetMockServletContext extends MockServletContext {
    private String resourcePath = "";

    public GeonetMockServletContext() {
        super(new FileSystemResourceLoader());
    }

    public void setTestClass(Class testClass) {
        this.resourcePath = AbstractCoreIntegrationTest.getWebappDir(testClass);
        if (this.resourcePath.endsWith("/") || this.resourcePath.endsWith("\\")) {
            this.resourcePath = this.resourcePath.substring(0, this.resourcePath.length() - 1);
        }
    }

    @Override
    protected String getResourceLocation(String path) {
        return this.resourcePath + super.getResourceLocation(path);
    }
}
