package org.fao.geonet.services.metadata.format.groovy;

import groovy.lang.Closure;
import groovy.util.slurpersupport.GPathResult;

import java.util.Comparator;

/**
 * A sorter where the matcher is a function.
 *
 * @author Jesse on 10/20/2014.
 */
public class SorterFunctionSelect extends Sorter {
    private final Closure matcher;

    @SuppressWarnings("unchecked")
    public SorterFunctionSelect(Closure matcher, Comparator comparator) {
        super(comparator);
        this.matcher = matcher;
    }

    @Override
    public boolean select(TransformationContext context, GPathResult parentElement) {
        return (Boolean) this.matcher.call(parentElement);
    }
}
