package org.fao.geonet.services.metadata.format.groovy;

import groovy.lang.Closure;
import groovy.util.slurpersupport.GPathResult;

/**
 * A skip element is an element that shouldn't be processed but some of its children should be.  For
 * example a formatter for iso19139 may not want to show the informationInfo elements but does want to display its children.
 *
 * The match can be gmd:informationInfo and the childSelector can be the closure {it.children()}
 *
 * @author Jesse on 11/26/2014.
 */
public abstract class SkipElement extends Selectable {
    public Closure childSelector;

    public SkipElement(int priority, Closure childSelector) {
        super(priority);
        this.childSelector = childSelector;
    }

    @Override
    protected String extraToString() {
        return null;
    }

    public Closure getChildSelector() {
        return childSelector;
    }

    public GPathResult selectChildren(GPathResult elem) {
        final int maximumNumberOfParameters = this.childSelector.getMaximumNumberOfParameters();
        switch (maximumNumberOfParameters) {
            case 0:
                return (GPathResult) this.childSelector.call();
            case 1:
                return (GPathResult) this.childSelector.call(elem);
            default:
                throw new IllegalArgumentException("Closure has too many parameters");
        }
    }
}
