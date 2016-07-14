/*
 * Copyright (C) 2001-2016 Food and Agriculture Organization of the
 * United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * and United Nations Environment Programme (UNEP)
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or (at
 * your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 *
 * Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * Rome - Italy. email: geonetwork@osgeo.org
 */

package org.fao.geonet.api.records.formatters.groovy;

import groovy.util.slurpersupport.GPathResult;

import java.util.Comparator;

/**
 * A strategy for controlling the order that the transformed data of an element is added to the
 * resulting document.  A Sorter applies to a parent element and sorts all the child (and associated
 * data) elements of that element.
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
