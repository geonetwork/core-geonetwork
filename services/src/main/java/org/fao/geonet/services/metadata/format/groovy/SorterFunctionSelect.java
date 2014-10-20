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
    public SorterFunctionSelect(Closure select, Comparator comparator) {
        super(comparator);
        this.select = select;
    }

    @Override
    public boolean select(TransformationContext context, GPathResult parentElement) {
        return (Boolean) this.select.call(parentElement);
    }
}
