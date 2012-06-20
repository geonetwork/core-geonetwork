//=============================================================================
//===	Copyright (C) 2001-2007 Food and Agriculture Organization of the
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

package org.fao.geonet.services.monitoring.services;

import org.jdom.Element;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Class to store the services monitor information.  Builds an xml report with this information.
 * 
 */
public class ServiceMonitorReport {
    private final static String SERVICE_STATUS_OK = "ok";
    private final static String SERVICE_STATUS_ERROR = "error";

    // Report sections
    private Map<String, ArrayList<ServiceMonitorField>> sections;


    public ServiceMonitorReport() {
        sections = new HashMap<String, ArrayList<ServiceMonitorField>>();
        sections.put("_", new ArrayList<ServiceMonitorField>());

    }

    /**
     * Adds a new (field, value) to a section of the report
     * @param sectionName       Section (service) name
     * @param fieldName         Field to add
     * @param value             Value of the field
     */
    public void addField(String sectionName, String fieldName, String value) {
        ArrayList<ServiceMonitorField> fields;

        if (sections.containsKey(sectionName))
            fields = sections.get(sectionName);
        else
            fields = new ArrayList<ServiceMonitorField>();

        fields.add(new ServiceMonitorField(fieldName, value));
        sections.put(sectionName, fields);
    }

    /**
     * Adds status ok to a section of the report
     *
     * @param sectionName       Section (service) name
     * @param responseTime      Total time spend to check the service status
     */
    public void addStatusOk(String sectionName, float responseTime) {
        ArrayList<ServiceMonitorField> fields;

        if (sections.containsKey(sectionName))
            fields = sections.get(sectionName);
        else
            fields = new ArrayList<ServiceMonitorField>();

        fields.add(new ServiceMonitorField("status", SERVICE_STATUS_OK));
        fields.add(new ServiceMonitorField("responseTime", responseTime + ""));
    }

    /**
     * Adds status error  to a section of the report
     *
     * @param sectionName       Section (service) name
     * @param errorCode         Error code (Use HTTP status code)
     * @param errorDescription  Error description
     *
     */
    public void addStatusError(String sectionName, String errorCode, String errorDescription) {
        ArrayList<ServiceMonitorField> fields;

        if (sections.containsKey(sectionName))
            fields = sections.get(sectionName);
        else
            fields = new ArrayList<ServiceMonitorField>();

        fields.add(new ServiceMonitorField("status", SERVICE_STATUS_ERROR));
        fields.add(new ServiceMonitorField("errorCode", errorCode));
        fields.add(new ServiceMonitorField("errorDescription", errorDescription));

    }

    /**
     * Builds xml representation to return in Jeeves services
     *
     * @return
     */
    public Element buildXmlReport() {
        Element report = new Element("monitoringReport");

        for(String sectionName: sections.keySet()) {
            Element section = new Element(sectionName);

            for(ServiceMonitorField f: sections.get(sectionName)) {
                Element field = new Element(f.name);
                field.setText(f.value);

                section.addContent(field);
            }

            report.addContent(section);
        }

        return report;
    }


    private class ServiceMonitorField {
        String name;
        String value;

        ServiceMonitorField(String name, String value) {
            this.name = name;
            this.value = value;
        }

    }
}
