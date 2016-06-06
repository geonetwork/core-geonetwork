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

package org.fao.geonet.csw.common;

import org.fao.geonet.csw.common.exceptions.InvalidParameterValueEx;

/**
 * OGC 07-006 10.8.4.3:
 *
 * The resultType parameter may have the value “hits”, “results”, or “validate”; the value
 * determines whether the catalogue service returns just a summary of the result set, includes one
 * or more records from the result set, or validates the request message and processes it
 * asynchronously.
 *
 * If the resultType parameter is set to “hits”, the catalogue service shall return a
 * <GetRecordsResponse> element containing an empty <SearchResults> element that indicates the
 * estimated size of the result set. Optional attributes may or may not be set accordingly.
 *
 * If the resultType parameter is set to “results”, the catalogue service shall include any matching
 * records within the <SearchResults> element, up to the maximum number of records specified in the
 * request.
 *
 * If the resultType parameter is set to “validate”, the catalogue service shall validate the
 * request and return an <Acknowledgement> message if validation succeeds. An <ows:ExceptionReport>
 * element, as described in Subclause 10.3.7, is returned if validation fails. If the catalogue
 * supports asynchronous query processing, the acknowledgment response shall include a RequestId
 * element that may be subsequently used to retrieve the result set when processing is complete.
 *
 * OGC 07-045: Optional. One of “hits”, “results” or “validate”. Default value is “hits”.
 *
 * If the resultType parameter is set to “validate”, the catalogue service must validate the request
 * and return an <Acknowledgement> message if validation succeeds; a <ServiceExceptionReport> is
 * returned if validation fails.
 *
 * TODO so what to do if validation fails: follow 07-045 or 07-006 ? 07-045 I suppose.
 */
public enum ResultType {

    HITS("hits"),
    RESULTS("results"),
    // this is a GeoNetwork-specific value, not in the CSW specs
    RESULTS_WITH_SUMMARY("results_with_summary"),
    VALIDATE("validate");

    private String type;

    private ResultType(String type) {
        this.type = type;
    }

    public static ResultType parse(String type) throws InvalidParameterValueEx {
        if (type == null) {
            return HITS;
        }
        for (ResultType rtype : ResultType.values()) {
            if (type.equals(rtype.toString()))
                return rtype;
        }
        throw new InvalidParameterValueEx("resultType", type);
    }

    public String toString() {
        return type;
    }
}
