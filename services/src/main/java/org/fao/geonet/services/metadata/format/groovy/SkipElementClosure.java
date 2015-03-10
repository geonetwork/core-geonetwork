package org.fao.geonet.services.metadata.format.groovy;

import groovy.lang.Closure;
import groovy.util.slurpersupport.GPathResult;

/**
 * @author Jesse on 11/26/2014.
 */
public class SkipElementClosure extends SkipElement {
    private final Closure closure;

    public SkipElementClosure(Closure closure, int priority, Closure childSelector) {
        super(priority, childSelector);
        this.closure = closure;
    }

    @Override
    public boolean select(TransformationContext context, GPathResult elem) {
        final int maximumNumberOfParameters = this.closure.getMaximumNumberOfParameters();
        switch (maximumNumberOfParameters) {
        case 0:
            return (boolean) this.closure.call();
        case 1:
            return (boolean) this.closure.call(elem);
        default:
            throw new IllegalArgumentException("Closure has too many parameters");
        }
    }
}
