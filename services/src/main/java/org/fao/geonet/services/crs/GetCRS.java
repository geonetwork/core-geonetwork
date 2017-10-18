//=============================================================================
//===	Copyright (C) 2001-2005 Food and Agriculture Organization of the
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
//===	Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: GeoNetwork@fao.org
//==============================================================================

package org.fao.geonet.services.crs;

import jeeves.interfaces.Service;
import jeeves.server.ServiceConfig;
import jeeves.server.context.ServiceContext;

import org.fao.geonet.Util;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.utils.Log;
import org.geotools.referencing.ReferencingFactoryFinder;
import org.jdom.Element;
import org.opengis.metadata.Identifier;
import org.opengis.referencing.FactoryException;
import org.opengis.referencing.crs.CRSAuthorityFactory;
import org.opengis.referencing.crs.CoordinateReferenceSystem;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Set;

/**
 * Get a Coordinate Reference System and return an ISO19139 fragment.
 *
 * @author francois
 */
@Deprecated
public class GetCRS implements Service {
    protected static Element formatCRS(String authorityTitle, String authorityEdition,
                                       String authorityCodeSpace, String code, String description) {
        Element crs = new Element("crs");
        crs.addContent(new Element("code").setText(code));
        crs.addContent(new Element("authority")
            .setText(authorityTitle));
        crs.addContent(new Element("version")
            .setText(authorityEdition));
        crs.addContent(new Element("codeSpace")
            .setText(authorityCodeSpace));
        crs.addContent(new Element("description")
            .setText(description));
        return crs;
    }

    public void init(Path appPath, ServiceConfig params) throws Exception {
    }

    /**
     * Get CRS
     *
     * @param params Parameter "id" is a list of word separated by spaces.
     */
    public Element exec(Element params, ServiceContext context)
        throws Exception {

        String id = Util.getParam(params, Params.ID);

        Element crs = get(id);

        return crs;
    }

    /**
     * Get CRS by code. Return the first CRS found.
     *
     * @param crsId the CRS identifier to search for.
     * @return XML
     */
    private Element get(String crsId) {
        Element crsList = new Element("crsList");

        for (Object object : ReferencingFactoryFinder
            .getCRSAuthorityFactories(null)) {
            CRSAuthorityFactory factory = (CRSAuthorityFactory) object;

            try {
                Set<String> codes = factory
                    .getAuthorityCodes(CoordinateReferenceSystem.class);
                for (Object codeObj : codes) {
                    String code = (String) codeObj;

                    if (code.equals(crsId)) {

                        String authorityTitle = (factory.getAuthority()
                            .getTitle() == null ? "" : factory
                            .getAuthority().getTitle().toString());
                        String authorityEdition = (factory.getAuthority()
                            .getEdition() == null ? "" : factory
                            .getAuthority().getEdition().toString());

                        String authorityCodeSpace = "";
                        Collection<? extends Identifier> ids = factory
                            .getAuthority().getIdentifiers();
                        for (Identifier id : ids) {
                            authorityCodeSpace = id.getCode();
                        }

                        String description;
                        try {
                            description = factory.getDescriptionText(code)
                                .toString();
                        } catch (Exception e1) {
                            description = "-";
                        }
                        description += " (" + authorityCodeSpace + ":" + code
                            + ")";

                        Element crs = formatCRS(authorityTitle,
                            authorityEdition, authorityCodeSpace, code,
                            description);
                        crsList.addContent(crs);

                        return crsList;
                    }
                }
            } catch (FactoryException e) {
                Log.error(Geonet.GEONETWORK, "CRS Authority:" + e.getMessage(), e);
            }
        }
        return crsList;
    }

    /**
     * checks if all keywords in filter array are in input
     *
     * @param input  test string
     * @param filter array of keywords
     * @return true, if all keywords in filter are in the input, false otherwise
     */
    protected boolean matchesFilter(String input, String[] filter) {
        for (String match : filter) {
            if (!input.contains(match))
                return false;
        }
        return true;
    }
}
