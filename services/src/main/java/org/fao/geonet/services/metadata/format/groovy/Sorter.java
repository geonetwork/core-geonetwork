package org.fao.geonet.services.metadata.format.groovy;

import groovy.util.slurpersupport.GPathResult;

import java.util.Comparator;

/**
 * A strategy for controlling the order that the transformed data of an element is added to the resulting document.  A Sorter
 * applies to a parent element and sorts all the child (and associated data) elements of that element.
 *
 * @author Jesse on 10/20/2014.
 */
public abstract class Sorter extends Selectable implements Comparator<GPathResult>, Comparable<Sorter> {
    private final Comparator<GPathResult> comparator;

    protected Sorter(int priority, Comparator<GPathResult> comparator) {
        super(priority);
        this.comparator = comparator;
    }

    /**
     * This is essentially the same as a Comparator's compare method.
     */
    public int compare(GPathResult el1, GPathResult el2) {
        return this.comparator.compare(el1, el2);
    }

    @Override
    public int compareTo(Sorter o) {
        return Integer.compare(o.priority, this.priority);
    }
}
