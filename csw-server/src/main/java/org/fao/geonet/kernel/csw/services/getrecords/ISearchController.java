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

package org.fao.geonet.kernel.csw.services.getrecords;

import org.fao.geonet.csw.common.ElementSetName;
import org.fao.geonet.csw.common.ResultType;
import org.fao.geonet.csw.common.exceptions.CatalogException;
import org.fao.geonet.domain.Pair;
import org.jdom.Element;

import java.util.Set;

import jeeves.server.context.ServiceContext;

public interface ISearchController {
    /**
     * Performs the general search tasks.
     *
     * @param context                     Service context
     * @param startPos                    start position (if paged)
     * @param maxRecords                  max records to return
     * @param resultType                  requested ResultType
     * @param outSchema                   requested OutputSchema
     * @param setName                     requested ElementSetName
     * @param filterExpr                  requested FilterExpression
     * @param filterVersion               requested Filter version
     * @param request                     requested sorting
     * @param elemNames                   requested ElementNames
     * @param typeName                    requested typeName
     * @param maxHitsFromSummary          ?
     * @param cswServiceSpecificContraint specific contraint for specialized CSW services
     * @param strategy                    ElementNames strategy
     * @return result
     * @throws CatalogException hmm
     */
    public Pair<Element, Element> search(ServiceContext context, int startPos, int maxRecords,
                                         ResultType resultType, String outSchema, ElementSetName setName,
                                         Element filterExpr, String filterVersion, Element request,
                                         Set<String> elemNames, String typeName, int maxHitsFromSummary,
                                         String cswServiceSpecificContraint, String strategy) throws CatalogException;
}
