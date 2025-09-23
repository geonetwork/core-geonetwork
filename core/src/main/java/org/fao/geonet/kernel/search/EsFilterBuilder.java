package org.fao.geonet.kernel.search;

import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.NodeInfo;
import org.fao.geonet.domain.AnonymousAccessLink;
import org.fao.geonet.domain.Profile;
import org.fao.geonet.domain.ReservedOperation;
import org.fao.geonet.domain.Source;
import org.fao.geonet.kernel.AccessManager;
import org.fao.geonet.kernel.security.ViewMdGrantedAuthority;
import org.fao.geonet.repository.SourceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
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
            List<String> filters = new ArrayList<>();

            // op0 (ie. view operation) contains one of the ids of your groups
            Set<Integer> groups = accessManager.getUserGroups(userSession, context.getIpAddress(), false);
            final String ids = groups.stream()
                .map(Object::toString)
                .map(e -> e.replace("-", "\\\\-"))
                .collect(Collectors.joining(" OR "));
            filters.add(String.format("op%d:(%s)", ReservedOperation.view.getId(), ids));


            if (userSession.getUserIdAsInt() > 0) {
                // OR you are owner
                filters.add(String.format("owner:%d", userSession.getUserIdAsInt()));
                // OR member of groupOwner
                // TODOES
            } else {
                filters.add(SecurityContextHolder.getContext().getAuthentication().getAuthorities().stream()
                        .filter(ViewMdGrantedAuthority.class::isInstance)
                        .map(ViewMdGrantedAuthority.class::cast)
                        .map(ViewMdGrantedAuthority::getAnonymousAccessLink)
                        .map(AnonymousAccessLink::getMetadataUuid)
                        .map(uuid -> String.format("_id:%s", uuid))
                        .collect(Collectors.joining(" ")));
            }
            return String.format("(%s)", String.join(" ", filters).trim());
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
