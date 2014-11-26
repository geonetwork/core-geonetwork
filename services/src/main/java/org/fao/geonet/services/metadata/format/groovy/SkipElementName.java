package org.fao.geonet.services.metadata.format.groovy;

import groovy.lang.Closure;
import groovy.util.slurpersupport.GPathResult;

/**
 * @author Jesse on 11/26/2014.
 */
public class SkipElementName extends SkipElement {
    private final String elName;

    public SkipElementName(String name, int priority, Closure childSelector) {
        super(priority, childSelector);
        this.elName = name;
    }

    @Override
    public boolean select(TransformationContext context, GPathResult result) {
        return result.name().equals(elName);
    }
}
