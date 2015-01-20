package org.fao.geonet.services.metadata.format.groovy;

import groovy.lang.Closure;
import groovy.util.slurpersupport.GPathResult;

import java.util.regex.Pattern;

/**
 * A Handler where the function will be executed if the element's qualified name matches the pattern.
 *
 * @author Jesse on 10/15/2014.
 */
public class HandlerNameSelect extends Handler {
    private final Pattern namePattern;

    public HandlerNameSelect(Pattern namePattern, int priority, Closure handler) {
        super(priority, handler);
        this.namePattern = namePattern;
        setName(namePattern.toString());
    }

    @Override
    public boolean select(TransformationContext rootPath, GPathResult element) {
        final String qualifiedName = element.name();
        return namePattern.matcher(qualifiedName).matches();
    }

    @Override
    protected String handlerExtraToString() {
        return ", namePattern ~= /" + namePattern + "/";
    }
}
