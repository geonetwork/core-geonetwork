//=============================================================================
//===	Copyright (C) 2001-2021 Food and Agriculture Organization of the
//===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===	and United Nations Environment Programme (UNEP)
//===
//===	This program is free software; you can redistribute it and/or modify
//===	it under the terms of the GNU General Public License as published by
//===	the Free Software Foundation; either version 2 of the License, or (at
//===	your option) any later version.
//===
//===	This program is distributed in the hope that it will be useful, but
//===	WITHOUT ANY WARRANTY; without even the implied warranty of
//===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===	General Public License for more details.
//===
//===	You should have received a copy of the GNU General Public License
//===	along with this program; if not, write to the Free Software
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================
package org.fao.geonet.client.model;


import java.util.List;

/**
 * Harvest process status model.
 */
public class HarvestStatus {
    public String processID;
    public String url;
    public String longTermTag;
    public String state;
    public String createTimeUTC;
    public String lastUpdateUTC;
    public List<String> errorMessage;

    public List<List<String>> stackTraces;
    public List<EndpointStatus> endpoints;


    @Override
    public String toString() {
        return "HarvestStatus{" +
            "processID='" + processID + '\'' +
            ", url='" + url + '\'' +
            ", longTermTag='" + longTermTag + '\'' +
            ", state='" + state + '\'' +
            ", createTimeUTC='" + createTimeUTC + '\'' +
            ", lastUpdateUTC='" + lastUpdateUTC + '\'' +
            ", errorMessage=" + errorMessage +
            ", stackTraces=" + stackTraces +
            ", endpoints=" + endpoints +
            '}';
    }
}
