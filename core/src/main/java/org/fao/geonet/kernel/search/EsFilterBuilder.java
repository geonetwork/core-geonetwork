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

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class EsFilterBuilder {

    private static AccessManager accessManager;

    private static SourceRepository sourceRepository;

    @Autowired
    public EsFilterBuilder(AccessManager accessManager, SourceRepository sourceRepository) {
        EsFilterBuilder.sourceRepository = sourceRepository;
        EsFilterBuilder.accessManager = accessManager;

    }

    public static String buildPermissionsFilter(ServiceContext context) throws Exception {
        final UserSession userSession = context.getUserSession();

        // If admin you can see all
        if (Profile.Administrator.equals(userSession.getProfile())) {
            return "*:*";
        } else {
            // op0 (ie. view operation) contains one of the ids of your groups
            Set<Integer> groups = accessManager.getUserGroups(userSession, context.getIpAddress(), false);
            final String ids = groups.stream()
                .map(Object::toString)
                .map(e -> e.replace("-", "\\\\-"))
                .collect(Collectors.joining(" OR "));
            String operationFilter = String.format("op%d:(%s)", ReservedOperation.view.getId(), ids);


            String ownerFilter = "";
            String groupOwnerFilter = "";
            if (userSession.getUserIdAsInt() > 0) {
                // OR you are owner
                ownerFilter = String.format("owner:%d", userSession.getUserIdAsInt());
                // OR member of groupOwner
                groupOwnerFilter = String.format("groupOwner:(%s)",
                    // don't use groups 0, 1, -1 as groupOwner
                    groups.stream().filter(g -> g > 1)
                        .map(Object::toString)
                        .collect(Collectors.joining(" OR ")));

            }
            return String.format("(%s %s %s)", operationFilter, ownerFilter, groupOwnerFilter).trim();
        }
    }
    /**
     * Add search privilege criteria to a query.
     */
    public static String build(ServiceContext context, String type, boolean isSearchingForDraft, NodeInfo node) throws Exception {
        StringBuilder query = new StringBuilder();
        query.append(EsFilterBuilder.buildPermissionsFilter(context).trim());

        if (type.equalsIgnoreCase("metadata")) {
            query.append(" AND (isTemplate:n)");
        } else if (type.equalsIgnoreCase("template")) {
            query.append(" AND (isTemplate:y)");
        } else if (type.equalsIgnoreCase("subtemplate")) {
            query.append(" AND (isTemplate:s)");
        }

        if (!isSearchingForDraft) {
            query.append(" AND (draft:n OR draft:e)");
        }

        final String portalFilter = EsFilterBuilder.buildPortalFilter(node);
        if (!"".equals(portalFilter)) {
            query.append(" AND ").append(portalFilter);
        }
        return query.toString();
    }

    public static String buildPortalFilter(NodeInfo node) {
        // If the requested portal define a filter
        // Add it to the request.
        if (node != null && !NodeInfo.DEFAULT_NODE.equals(node.getId())) {
            final Optional<Source> portal = sourceRepository.findById(node.getId());
            if (portal.isPresent() && StringUtils.isNotEmpty(portal.get().getFilter())) {
                return portal.get().getFilter().replace("\"", "\\\"");
            }
        }
        return "";
    }
}
