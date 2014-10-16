package org.fao.geonet.services.metadata.format.groovy;

import groovy.lang.Closure;
import groovy.util.slurpersupport.GPathResult;

import java.util.regex.Pattern;

/**
 * A Handler where the function will be executed if the element's qualified name matches the pattern.
 *
 * @author Jesse on 10/15/2014.
 */
public class HandlerNameMatch extends Handler {
    private final Pattern nameMatcher;

    public HandlerNameMatch(Pattern nameMatcher, int priority, Closure handler) {
        super(priority, handler);
        this.nameMatcher = nameMatcher;
    }

    @Override
    public boolean canHandle(TransformationContext rootPath, GPathResult element) {
        final String qualifiedName = element.name();
        return nameMatcher.matcher(qualifiedName).matches();
    }
}
