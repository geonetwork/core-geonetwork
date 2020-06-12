package org.fao.geonet.kernel.search;

import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.NodeInfo;
import org.fao.geonet.domain.Profile;
import org.fao.geonet.domain.ReservedOperation;
import org.fao.geonet.domain.Source;
import org.fao.geonet.kernel.AccessManager;
import org.fao.geonet.repository.SourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class EsFilterBuilder {

    @Autowired
    AccessManager accessManager;

    @Autowired
    NodeInfo node;

    @Autowired
    SourceRepository sourceRepository;

    private String buildPermissionsFilter(ServiceContext context) throws Exception {
        final UserSession userSession = context.getUserSession();

        // If admin you can see all
        if (Profile.Administrator.equals(userSession.getProfile())) {
            return "*";
        } else {
            // op0 (ie. view operation) contains one of the ids of your groups
            Set<Integer> groups = accessManager.getUserGroups(userSession, context.getIpAddress(), false);
            final String ids = groups.stream().map(Object::toString)
                .collect(Collectors.joining(" OR "));
            String operationFilter = String.format("op%d:(%s)", ReservedOperation.view.getId(), ids);


            String ownerFilter = "";
            if (userSession.getUserIdAsInt() > 0) {
                // OR you are owner
                ownerFilter = String.format("owner:%d");
                // OR member of groupOwner
                // TODOES
            }
            return String.format("%s %s", operationFilter, ownerFilter).trim();
        }
    }
    /**
     * Add search privilege criteria to a query.
     */
    public String build(ServiceContext context, String type) throws Exception {
        StringBuilder query = new StringBuilder();
        query.append(buildPermissionsFilter(context).trim());

        if (type.equalsIgnoreCase("metadata")) {
            query.append(" AND (isTemplate:n)");
        } else if (type.equalsIgnoreCase("template")) {
            query.append(" AND (isTemplate:y)");
        } else if (type.equalsIgnoreCase("subtemplate")) {
            query.append(" AND (isTemplate:s)");
        }

        final String portalFilter = buildPortalFilter();
        if (!"".equals(portalFilter)) {
            query.append(" ").append(portalFilter);
        }
        return query.toString();

    }

    private String buildPortalFilter() {
        // If the requested portal define a filter
        // Add it to the request.
        if (node != null && !NodeInfo.DEFAULT_NODE.equals(node.getId())) {
            final Source portal = sourceRepository.findById(node.getId()).get();
            if (portal == null) {
                //LOGGER.warn("Null portal " + node);
            } else if (StringUtils.isNotEmpty(portal.getFilter())) {
                //LOGGER.debug("Applying portal filter: {}", portal.getFilter());
                return portal.getFilter();
            }
        }
        return "";
    }
}
