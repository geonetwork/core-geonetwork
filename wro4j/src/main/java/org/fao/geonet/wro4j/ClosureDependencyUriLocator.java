package org.fao.geonet.wro4j;

import org.fao.geonet.utils.IO;

import ro.isdc.wro.model.resource.Resource;
import ro.isdc.wro.model.resource.ResourceType;
import ro.isdc.wro.model.resource.locator.UriLocator;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

/**
 * Converts a resource to a closure goog.addDependency statement.
 * <p/>
 * Created by Jesse on 1/7/14.
 */
public class ClosureDependencyUriLocator implements UriLocator {

    public static final String URI_PREFIX = "closureDep://";
    public static final String URI_LOCATOR_ID = "closureDependencyURILocator";
    static final String PATH_TO_WEBAPP_BASE_FROM_CLOSURE_BASE_JS_FILE = "../../..";

    static Resource createClosureDepResource(ClosureRequireDependencyManager.Node dep) {
        Resource resource = new Resource();
        resource.setMinimize(false);
        resource.setType(ResourceType.JS);

        if (Files.exists(IO.toPath(dep.path.replace("file:/D:", "/D")))) {
            resource.setUri(IO.toPath(dep.path).toUri().toString());
        } else {
            StringBuilder path = new StringBuilder();
            final String[] parts = dep.path.split(":/+", 2);
            path.append(parts.length == 2 ? parts[1] : parts[0]);
            path.append("@@").append(dep.id);

            for (String dependencyId : dep.dependencyIds) {
                path.append("@@").append(dependencyId);
            }

            resource.setUri(URI_PREFIX + path);
        }
        return resource;
    }

    @Override
    public InputStream locate(String uri) throws IOException {
        StringBuilder javascript = new StringBuilder("goog.addDependency('");
        int state = 0;
        for (String part : uri.split("@@")) {
            switch (state) {
                case 0:
                    final String path = part.substring(URI_PREFIX.length());
                    javascript.append(PATH_TO_WEBAPP_BASE_FROM_CLOSURE_BASE_JS_FILE);
                    if (!path.startsWith("/")) {
                        javascript.append("/");
                    }
                    javascript.append(path).append("', ['");
                    break;
                case 1:
                    javascript.append(part).append("'], [");
                    break;
                default:
                    if (javascript.charAt(javascript.length() - 1) != '[') {
                        javascript.append(',');
                    }
                    javascript.append("'").append(part).append("'");
            }
            state++;
        }

        javascript.append("]);\n");

        return new ByteArrayInputStream(javascript.toString().getBytes("UTF-8"));
    }

    @Override
    public boolean accept(String uri) {
        return uri.startsWith(URI_PREFIX);
    }
}
