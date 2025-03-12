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

package org.fao.geonet.api.registries;

import org.fao.geonet.api.registries.model.Crs;
import org.fao.geonet.api.registries.model.CrsType;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.utils.Log;
import org.geotools.referencing.ReferencingFactoryFinder;
import org.geotools.api.metadata.Identifier;
import org.geotools.api.referencing.FactoryException;
import org.geotools.api.referencing.IdentifiedObject;
import org.geotools.api.referencing.crs.CRSAuthorityFactory;
import org.geotools.api.referencing.crs.CoordinateReferenceSystem;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Utilities to search CRS
 */
public class CrsUtils {


    /**
     * filters all CRS Names from all available CRS authorities
     *
     * @param filters array of keywords
     * @param crsType type of CRS to search for
     * @param rows    maximum number of results
     * @return XML with all CRS Names which contain all the filters keywords
     */
    public static List<Crs> search(String[] filters,
                                   CrsType crsType,
                                   int rows) {
        List<Crs> crsList = new ArrayList<>();
        int i = 0;

        Class<? extends IdentifiedObject> crsTypeClass =
            crsType != null ? crsType.getClazz() : CoordinateReferenceSystem.class;


        for (Object object : ReferencingFactoryFinder
            .getCRSAuthorityFactories(null)) {
            CRSAuthorityFactory factory = (CRSAuthorityFactory) object;

            String authorityTitle =
                factory.getAuthority().getTitle() == null ?
                    "" : factory.getAuthority().getTitle().toString();
            String authorityEdition =
                factory.getAuthority().getEdition() == null ?
                    "" : factory.getAuthority().getEdition().toString();

            String authorityCodeSpace = "";
            Collection<? extends Identifier> ids = factory.getAuthority()
                .getIdentifiers();
            for (Identifier id : ids) {
                authorityCodeSpace = id.getCode();
                // FIXME : Which one to choose when many ?
            }

            try {
                Set<String> codes = factory.getAuthorityCodes(crsTypeClass);
                for (Object codeObj : codes) {
                    String code = (String) codeObj;

                    String description;
                    try {
                        description = factory.getDescriptionText(code)
                            .toString();
                    } catch (Exception e1) {
                        description = "-";
                    }
                    description += " (" + authorityCodeSpace + ":" + code + ")";

                    if (matchesFilter(description, filters)) {
                        crsList.add(
                            new Crs(code, authorityTitle,
                                authorityEdition, authorityCodeSpace,
                                description));
                        if (++i >= rows)
                            return crsList;
                    }
                }
            } catch (FactoryException e) {
                Log.error(Geonet.GEONETWORK, e.getMessage(), e);
            }
        }
        return crsList;
    }


    public static Crs getById(String crsId) {
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

                        return new Crs(code, authorityTitle,
                            authorityEdition, authorityCodeSpace,
                            description);
                    }
                }
            } catch (FactoryException e) {
            }
        }
        return null;
    }


    /**
     * Checks if all keywords in filter array are in input (case insensitive search)
     *
     * @param input   test string
     * @param filters array of keywords
     * @return true, if all keywords in filter are in the input, false otherwise
     */
    static protected boolean matchesFilter(String input, String[] filters) {
        String upperCasedInput = input.toUpperCase();
        for (String match : filters) {
            if (!upperCasedInput.contains(match.toUpperCase()))
                return false;
        }
        return true;
    }
}
