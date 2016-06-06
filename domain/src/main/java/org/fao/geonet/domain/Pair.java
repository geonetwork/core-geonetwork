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

package org.fao.geonet.domain;

/**
 * Just a container of 2 elements. Good for returning 2 values.
 *
 * @author jesse
 */
public class Pair<R, L> {
    private R one;
    private L two;

    protected Pair() {
    }
    private Pair(R one, L two) {
        super();
        this.one = one;
        this.two = two;
    }

    public static <R, L> Pair<R, L> read(R one, L two) {
        return new Pair<R, L>(one, two);
    }

    public static <R, L> Pair<R, L> write(R one, L two) {
        return new Writeable<R, L>(one, two);
    }

    public R one() {
        return one;
    }

    public L two() {
        return two;
    }

    @Override
    public String toString() {
        return "[" + one + "," + two + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((one == null) ? 0 : one.hashCode());
        result = prime * result + ((two == null) ? 0 : two.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Pair<?, ?> other = (Pair<?, ?>) obj;
        if (one == null) {
            if (other.one != null)
                return false;
        } else if (!one.equals(other.one))
            return false;
        if (two == null) {
            if (other.two != null)
                return false;
        } else if (!two.equals(other.two))
            return false;
        return true;
    }

    public static class Writeable<R, L> extends Pair<R, L> {
        public Writeable(R one, L two) {
            super(one, two);
        }

        public Writeable<R, L> one(R newVal) {
            super.one = newVal;
            return this;
        }

        public Writeable<R, L> two(L newVal) {
            super.two = newVal;
            return this;
        }
    }


}
