package org.fao.geonet.services.metadata.format.groovy;

import groovy.lang.Closure;
import groovy.util.slurpersupport.GPathResult;

import java.util.regex.Pattern;

/**
 * A Handler that is applied when the path of the element matches the path pattern of this handler.
 *
 * The path is separated by > characters
 *
 * @author Jesse on 10/15/2014.
 */
public class PathMatchingHandler extends Handler {
    private final Pattern pathMatcher;

    public PathMatchingHandler(Pattern pathMatcher, int priority, Closure handler) {
        super(priority, handler);
        this.pathMatcher = pathMatcher;
    }

    @Override
    public boolean canHandle(TransformationContext context, GPathResult element) {
        StringBuilder path = new StringBuilder(context.getRootPath());
        if (path.length() > 0) {
            path.append(">");
        }
        createPath(element, path);
        return this.pathMatcher.matcher(path.toString()).matches();
    }

}
