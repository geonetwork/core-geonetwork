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

    private String rootPath;
    private String currentMode = Mode.DEFAULT;
    public final Handlers handlers;
    public final Functions functions;
    public final Environment env;

    public TransformationContext(Handlers handlers, Functions functions, Environment env) {
        this.handlers = handlers;
        this.functions = functions;
        this.env = env;
    }

    public void setThreadLocal() {
        context.set(this);
    }

    /**
     * The path from the root of the metadata document to the "root" element as selected by the roots selectors in
     * {@link org.fao.geonet.services.metadata.format.groovy.Handlers#roots}
     */
    public String getRootPath() {
        return rootPath;
    }

    public void setRootPath(String rootPath) {
        this.rootPath = rootPath;
    }

    /**
     * Get the id of the mode currently configured for processing.
     */
    public String getCurrentMode() {
        return currentMode;
    }

    public void setCurrentMode(String currentMode) {
        this.currentMode = currentMode;
    }

    public Handlers getHandlers() {
        return handlers;
    }

    public Functions getFunctions() {
        return functions;
    }

    public Environment getEnv() {
        return env;
    }
}
