package org.fao.geonet.services.metadata.format.groovy;

/**
 * Contains properties like the current rootPath that can be used in handlers.
 *
 * @author Jesse on 10/16/2014.
 */
public class TransformationContext {
    private static ThreadLocal<TransformationContext> context = new InheritableThreadLocal<TransformationContext>();

    public static TransformationContext getContext() {
        return context.get();
    }

    public void setThreadLocal() {
        context.set(this);
    }

    /**
     * The path from the root of the metadata document to the "root" element as selected by the roots selectors in
     * {@link org.fao.geonet.services.metadata.format.groovy.Handlers#roots}
     */
    private String rootPath;

    public String getRootPath() {
        return rootPath;
    }

    public void setRootPath(String rootPath) {
        this.rootPath = rootPath;
    }
}
