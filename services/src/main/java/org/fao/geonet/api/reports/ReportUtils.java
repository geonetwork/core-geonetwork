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
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.QuoteMode;
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.Profile;
import org.fao.geonet.domain.ReservedGroup;
import org.fao.geonet.kernel.AccessManager;
import org.fao.geonet.kernel.search.EsSearchManager;
import org.fao.geonet.kernel.search.IndexFields;

import java.util.*;


/**
 * Report utility methods.
 *
 * @author Jose García
 */
public final class ReportUtils {

    public static final CSVFormat CSV_FORMAT =
        CSVFormat.DEFAULT
            .withRecordSeparator("\n")
            .withQuoteMode(QuoteMode.ALL);
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
     * @param groups  Requested list of groups to filter.
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
     * Retrieves a metadata field from the index.
     *
     * @param metadataUuid Metadata identifier.
     * @param fieldName  Lucene field name.
     * @param fieldNameKey  Key of the field name (eg 'default').
     * @return Metadata field.
     */
    public static String retrieveMetadataIndex(final String metadataUuid,final String fieldName, final String fieldNameKey) {
        return retrieveMetadataIndexField(metadataUuid, fieldName, fieldNameKey);
    }


    /**
     * Retrieves a metadata value from the Lucene index.
     * <p>
     * Duplicated with XslUtil - to refactor.
     * <p>
     * TODO / TODOES improve the management of the different type of fields
     *
     * @param metadataUuid Metadata identifier.
     * @param fieldName  Lucene field name.
     * @param fieldNameKey  Key of the field name (eg 'default').
     * @return Field value.
     */
    private static String retrieveMetadataIndexField(
        final String metadataUuid,
        final String fieldName,
        final String fieldNameKey) {
        EsSearchManager searchManager = ApplicationContextHolder.get().getBean(EsSearchManager.class);
        try {
            Map<String, Object> mdIndexFields = searchManager.getDocument(metadataUuid);
            Object field = mdIndexFields.get(fieldName);
            if (field instanceof HashMap) {
                if (StringUtils.isNotEmpty(fieldNameKey)) {
                    return (String) ((HashMap<?, ?>) field).get(fieldNameKey);
                } else {
                    return (String) field.toString();
                }
            } else if (field instanceof String) {
                return (String) field;
            } else if (field instanceof ArrayList) {
                Object fieldItem = ((ArrayList<?>) field).get(0);
                if (fieldItem instanceof HashMap) {
                    if (StringUtils.isNotEmpty(fieldNameKey)) {
                        return (String) ((HashMap<?, ?>) fieldItem).get(fieldNameKey);
                    } else {
                        return (String) fieldItem.toString();
                    }
                }
                else if (fieldItem instanceof String) {
                    return (String) fieldItem;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
}
