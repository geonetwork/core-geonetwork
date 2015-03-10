package org.fao.geonet.services.metadata.format.groovy;

import groovy.lang.Closure;
import groovy.util.slurpersupport.GPathResult;

import java.util.regex.Pattern;

/**
 * @author Jesse on 11/26/2014.
 */
public class SkipElementPattern extends SkipElement {
    private final Pattern pattern;

    public SkipElementPattern(Pattern name, int priority, Closure childSelector) {
        super(priority, childSelector);
        this.pattern = name;
        this.name = pattern.toString();
    }

    @Override
    public boolean select(TransformationContext context, GPathResult result) {
        return pattern.matcher(result.name()).matches();
    }
}
