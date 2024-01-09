/*
 * Copyright (C) 2001-2023 Food and Agriculture Organization of the
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

package org.fao.geonet.kernel.oaipmh;

import org.apache.commons.lang.StringUtils;
import org.fao.oaipmh.OaiPmh;

import java.util.HashMap;
import java.util.Map;

public class OaiPmhParams {
    private String verb;
    private String metadataPrefix;
    private String identifier;
    private String set;
    private String from;
    private String until;
    private String resumptionToken;

    public String getVerb() {
        return verb;
    }

    public void setVerb(String verb) {
        this.verb = verb;
    }

    public String getMetadataPrefix() {
        return metadataPrefix;
    }

    public void setMetadataPrefix(String metadataPrefix) {
        this.metadataPrefix = metadataPrefix;
    }

    public String getIdentifier() {
        return identifier;
    }

    public void setIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String getSet() {
        return set;
    }

    public void setSet(String set) {
        this.set = set;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getUntil() {
        return until;
    }

    public void setUntil(String until) {
        this.until = until;
    }

    public String getResumptionToken() {
        return resumptionToken;
    }

    public void setResumptionToken(String resumptionToken) {
        this.resumptionToken = resumptionToken;
    }

    public Map<String, String> asMap() {
        Map<String, String> params = new HashMap<>();

        if (StringUtils.isNotEmpty(verb)) {
            params.put(OaiPmh.ParamNames.VERB, verb);
        }

        if (StringUtils.isNotEmpty(metadataPrefix)) {
            params.put(OaiPmh.ParamNames.METADATA_PREFIX, metadataPrefix);
        }

        if (StringUtils.isNotEmpty(identifier)) {
            params.put(OaiPmh.ParamNames.IDENTIFIER, identifier);
        }

        if (StringUtils.isNotEmpty(set)) {
            params.put(OaiPmh.ParamNames.SET, set);
        }

        if (StringUtils.isNotEmpty(from)) {
            params.put(OaiPmh.ParamNames.FROM, from);
        }

        if (StringUtils.isNotEmpty(until)) {
            params.put(OaiPmh.ParamNames.UNTIL, until);
        }

        if (StringUtils.isNotEmpty(resumptionToken)) {
            params.put(OaiPmh.ParamNames.RESUMPTION_TOKEN, resumptionToken);
        }

        return params;
    }
}
