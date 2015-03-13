package org.fao.geonet.services.region;

import javax.annotation.Nonnull;

/**
 * Represents how much zoom should be given around a geometry so that there is enough context to see where the geometry is.
 *
 * This is configured in web/webapp/WEB-INF/config-spring-geonetwork.xml in the regionGetMapExpandFactors bean.
 *
* @author Jesse on 3/13/2015.
*/
public final class ExpandFactor implements Comparable<ExpandFactor>{
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
