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
    private final Closure select;

    @SuppressWarnings("unchecked")
    public SorterFunctionSelect(int priority, Closure select, Comparator comparator) {
        super(priority, comparator);
        this.select = select;
    }

    @Override
    public boolean select(TransformationContext context, GPathResult parentElement) {
        return (Boolean) this.select.call(parentElement);
    }

    @Override
    protected String extraToString() {
        return "";
    }
}
