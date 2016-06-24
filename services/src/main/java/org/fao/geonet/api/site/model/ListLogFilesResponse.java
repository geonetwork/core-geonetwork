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

package org.fao.geonet.api.site.model;

import java.util.Collection;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;

import com.fasterxml.jackson.annotation.JsonProperty;

import com.google.common.collect.Lists;

/**
 * @author Jesse on 4/9/2015.
 */
@XmlRootElement(name = "response")
@XmlAccessorType(XmlAccessType.FIELD)
public class ListLogFilesResponse {
    @XmlElement
    @JsonProperty
    private Collection<LogFileResponse> logFile = Lists.newArrayList();


    public ListLogFilesResponse() {
    }

    public ListLogFilesResponse(List<LogFileResponse> logFileList) throws TransformException, FactoryException {
        this.logFile.addAll(logFileList);

    }

    @XmlRootElement
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class LogFileResponse {
        @XmlAttribute
        @JsonProperty("file")
        private String file;
        @XmlAttribute
        @JsonProperty("name")
        private String name;

        public LogFileResponse() {
        }

        public LogFileResponse(String name, String file) {
            this.name = name;
            this.file = file;
        }
    }
}
