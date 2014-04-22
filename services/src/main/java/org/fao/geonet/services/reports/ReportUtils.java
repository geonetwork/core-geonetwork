package org.fao.geonet.services.reports;

import jeeves.server.context.ServiceContext;
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.GeonetContext;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.constants.Params;
import org.fao.geonet.domain.Profile;
import org.fao.geonet.kernel.AccessManager;
import org.fao.geonet.kernel.search.LuceneSearcher;
import org.jdom.Element;

import java.util.*;


public class ReportUtils {

    /**
     * Obtains a list of valid groups to apply in filter query taken from requested groups
     *
     * @param context
     * @param params
     * @return
     * @throws Exception
     */
    public static Set<Integer> groupsForFilter(ServiceContext context, Element params) throws Exception {
        List listGroups = params.getChildren(Params.GROUPS);
        Set<Integer> requestedGroups = new HashSet<Integer>();

        for(int i=0; i<listGroups.size(); i++) {
            String group = ((Element) listGroups.get(i)).getText();
            if (!StringUtils.isEmpty(group)) requestedGroups.add(Integer.parseInt(group));
        }

        if (context.getUserSession().getProfile().equals(Profile.Administrator)) {
            return requestedGroups;

        } else {
            GeonetContext gc = (GeonetContext) context
                    .getHandlerContext(Geonet.CONTEXT_NAME);
            AccessManager am = gc.getBean(AccessManager.class);
            Set<Integer> userGroups = am.getUserGroups(context.getUserSession(), context.getIpAddress(), false);

            // If no specific group requested, filter by user groups
            if (requestedGroups.isEmpty()) {

                return userGroups;

            } else {
                // Remove not allowed groups from request
                Set<Integer> filterRequestedGroups = new HashSet<Integer>();

                for(Integer gr : requestedGroups) {
                    if (userGroups.contains(gr)) {
                        filterRequestedGroups.add(gr) ;
                    }
                }

                return filterRequestedGroups;
            }
        }
    }

    public static String retrieveMetadataTitle(ServiceContext context, int metadataId) {
        return retrieveMetadataIndexField(context, metadataId, "_title");
    }

    public static String retrieveMetadataUuid(ServiceContext context, int metadataId) {
        return retrieveMetadataIndexField(context, metadataId, "_uuid");
    }

    private static String retrieveMetadataIndexField(ServiceContext context, int metadataId, String fieldName) {
        String value = "";

        try {
            value = LuceneSearcher.getMetadataFromIndexById(context.getLanguage(), metadataId + "", fieldName);

            if (value == null) value = "";
        } catch (Exception ex) {
            // TODO: log exception
            ex.printStackTrace();
        }

        return value;
    }
}
