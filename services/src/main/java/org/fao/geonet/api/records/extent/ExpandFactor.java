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

package org.fao.geonet.api.records.extent;

import javax.annotation.Nonnull;

/**
 * Represents how much zoom should be given around a geometry so that there is enough context to see
 * where the geometry is.
 *
 * This is configured in web/webapp/WEB-INF/config-spring-geonetwork.xml in the
 * regionGetMapExpandFactors bean.
 *
 * @author Jesse on 3/13/2015.
 */
public final class ExpandFactor implements Comparable<ExpandFactor> {
    double proportion;
    double factor;

    public double getProportion() {
        return proportion;
    }

    public void setProportion(double proportion) {
        this.proportion = proportion;
    }

    public double getFactor() {
        return factor;
    }

    public void setFactor(double factor) {
        this.factor = factor;
    }

    @Override
    public int compareTo(@Nonnull ExpandFactor o) {
        return Double.compare(proportion, o.proportion);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ExpandFactor that = (ExpandFactor) o;

        if (Double.compare(that.factor, factor) != 0) return false;
        if (Double.compare(that.proportion, proportion) != 0) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(proportion);
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(factor);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}
