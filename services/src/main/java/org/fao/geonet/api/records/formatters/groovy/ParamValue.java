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

/**
 * Represents the value of one of the parameters passed to the Format Service.
 *
 * @author Jesse on 10/17/2014.
 */
public class ParamValue {
    final String value;

    public ParamValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return this.value;
    }

    public boolean toBool() {
        String processedValue = this.value == null ? "false" : this.value;
        if (processedValue.equalsIgnoreCase("yes") || processedValue.equalsIgnoreCase("y") || processedValue.equals("1")) {
            return true;
        }
        return Boolean.parseBoolean(processedValue);
    }

    public int toInt() {
        return Integer.parseInt(this.value);
    }

    public Double toDouble() {
        return Double.parseDouble(this.value);
    }
}
