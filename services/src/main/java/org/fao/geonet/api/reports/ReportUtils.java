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

package org.fao.geonet.api.reports;

import com.google.common.collect.ImmutableSet;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.Profile;
import org.fao.geonet.domain.ReservedGroup;
import org.fao.geonet.kernel.AccessManager;
import org.fao.geonet.kernel.search.LuceneSearcher;
import org.fao.geonet.utils.Log;

import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * Report utility methods.
 *
 * @author Jose García
 */
public final class ReportUtils {

    /**
     * Constructor.
     */
    private ReportUtils() {

    }


    /**
     * Obtains a list of valid groups to apply in filter query
     * taken from requested groups.
     *
     * @param context Service context.
     * @param groups Requested list of groups to filter.
     * @return List of valid groups to apply in filter query.
     * @throws Exception Exception retrieving the list of groups for the filter.
     */
    public static Set<Integer> groupsForFilter(
            final ServiceContext context,
            final List<Integer> groups) throws Exception {

        Set<Integer> requestedGroups;

        if (groups == null) {
            requestedGroups = new HashSet<>();
        } else {
            requestedGroups = ImmutableSet.copyOf(groups);
        }

        Profile userProfile = context.getUserSession().getProfile();

        if (userProfile != null
                && userProfile.equals(Profile.Administrator)) {
            return requestedGroups;
        } else {
            GeonetContext gc = (GeonetContext) context
                .getHandlerContext(Geonet.CONTEXT_NAME);
            AccessManager am = gc.getBean(AccessManager.class);
            Set<Integer> userGroups = am.getUserGroups(context.getUserSession(),
                    context.getIpAddress(), false);

            // Remove special groups
            userGroups.remove(ReservedGroup.guest.getId());
            userGroups.remove(ReservedGroup.all.getId());
            userGroups.remove(ReservedGroup.intranet.getId());

            // If no specific group requested, filter by user groups
            if (requestedGroups.isEmpty()) {
                return userGroups;

            } else {
                // Remove not allowed groups from request
                Set<Integer> filterRequestedGroups = new HashSet<>();

                for (Integer gr : requestedGroups) {
                    if (userGroups.contains(gr)) {
                        filterRequestedGroups.add(gr);
                    }
                }

                if (!filterRequestedGroups.isEmpty()) {
                    return filterRequestedGroups;
                } else {
                    // If no specific group requested, filter by user groups
                    return userGroups;
                }
            }
        }
    }


    /**
     * Retrieves a metadata title from the Lucene index.
     *
     * @param context Service context.
     * @param metadataId Metadata identifier.
     * @return Metadata title.
     */
    public static String retrieveMetadataTitle(final ServiceContext context,
                                               final int metadataId) {
        return retrieveMetadataIndexField(context, metadataId, "_defaultTitle");
    }


    /**
     * Retrieves a metadata uuid from the Lucene index.
     *
     * @param context Service context.
     * @param metadataId Metadata identifier.
     * @return Metadata uuid.
     */
    public static String retrieveMetadataUuid(final ServiceContext context,
                                              final int metadataId) {
        return retrieveMetadataIndexField(context, metadataId, "_uuid");
    }


    /**
     * Retrieves a metadata value from the Lucene index.
     *
     * @param context Service context.
     * @param metadataId Metadata identifier.
     * @param fieldName Lucene field name.
     * @return Field value.
     */
    private static String retrieveMetadataIndexField(
            final ServiceContext context,
            final int metadataId,
            final String fieldName) {
        String value = "";

        try {
            value = LuceneSearcher.getMetadataFromIndexById(
                    context.getLanguage(),
                    metadataId + "",
                    fieldName);

            if (value == null) {
                value = "";
            }
        } catch (Exception ex) {
            Log.error(Geonet.GEONETWORK, ex.getMessage(), ex);
        }

        return value;
    }
}
