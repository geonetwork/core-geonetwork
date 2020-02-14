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

import org.jdom.Element;
import org.jdom.Namespace;
import org.springframework.context.ApplicationContext;

import java.util.List;

/**
 * A strategy object that evaluates schematron criteria.
 *
 * Created by Jesse on 2/6/14.
 */
public interface SchematronCriteriaEvaluator {
    public abstract boolean accepts(ApplicationContext applicationContext, String value, int metadataId, Element metadata,
                                    List<Namespace> metadataNamespaces);

    // Used for metadata that is not in the catalogue, should be provided the groupOwner for the metadata
    // to evaluate the SchemaCriteriaType.GROUP
    public abstract boolean accepts(ApplicationContext applicationContext, String value, Element metadata,
                                    List<Namespace> metadataNamespaces,  Integer groupOwnerId);
}
