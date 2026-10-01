/*
 * =============================================================================
 * ===	Copyright (C) 2001-2026 Food and Agriculture Organization of the
 * ===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
 * ===	and United Nations Environment Programme (UNEP)
 * ===
 * ===	This program is free software; you can redistribute it and/or modify
 * ===	it under the terms of the GNU General Public License as published by
 * ===	the Free Software Foundation; either version 2 of the License, or (at
 * ===	your option) any later version.
 * ===
 * ===	This program is distributed in the hope that it will be useful, but
 * ===	WITHOUT ANY WARRANTY; without even the implied warranty of
 * ===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * ===	General Public License for more details.
 * ===
 * ===	You should have received a copy of the GNU General Public License
 * ===	along with this program; if not, write to the Free Software
 * ===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
 * ===
 * ===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
 * ===	Rome - Italy. email: geonetwork@osgeo.org
 * ==============================================================================
 */
package org.fao.geonet.resources;

import org.apache.opendal.Operator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.HashMap;
import java.util.Map;

public class OpenDALConfiguration {
    private static final Logger log = LoggerFactory.getLogger(OpenDALConfiguration.class);

    private Operator operator;
    private String scheme = "fs";
    private Map<String, String> options = new HashMap<>();

    public void setScheme(String scheme) {
        this.scheme = scheme;
    }

    public void setOptions(Map<String, String> options) {
        if (options != null) {
            options.values().removeIf(value -> value == null || value.isEmpty());
            this.options.putAll(options);
        }
    }

    @PostConstruct
    public void init() {
        log.info("Initializing OpenDAL operator with scheme: {}", scheme);
        this.operator = Operator.of(scheme, options);
    }

    @PreDestroy
    public void destroy() {
        if (operator != null) {
            operator.close();
        }
    }

    @Nonnull
    public Operator getOperator() {
        if (operator == null) {
            throw new IllegalStateException("OpenDAL Operator not initialized");
        }
        return operator;
    }
}
