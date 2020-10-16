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

package org.fao.geonet.api.users.transfer;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.api.API;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.domain.*;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.kernel.datamanager.IMetadataManager;
import org.fao.geonet.kernel.datamanager.IMetadataUtils;
import org.fao.geonet.repository.OperationAllowedRepository;
import org.fao.geonet.repository.UserGroupRepository;
import org.fao.geonet.repository.UserRepository;
import org.fao.geonet.repository.specification.MetadataSpecs;
import org.fao.geonet.repository.specification.UserGroupSpecs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

import static org.fao.geonet.repository.specification.OperationAllowedSpecs.hasGroupId;

@RequestMapping(value = {
    "/{portal}/api/users"
})
@Tag(name = "users",
    description = "User operations")
@Controller("usersTransfer")
public class TransferApi {

    @Autowired
    UserRepository userRepository;
    @Autowired
    IMetadataUtils metadataRepository;
    @Autowired
    UserGroupRepository userGroupRepository;
    @Autowired
    OperationAllowedRepository operationAllowedRepository;
    @Autowired
    IMetadataManager metadataManager;
    @Autowired
    DataManager dataManager;

    @io.swagger.v3.oas.annotations.Operation(
        summary = "Get owners",
        description = "Return users who actually owns one or more records.")
    @RequestMapping(
        path = "owners",
        produces = MediaType.APPLICATION_JSON_VALUE,
        method = RequestMethod.GET
    )
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("hasAuthority('UserAdmin')")
    @ResponseBody
    public List<OwnerResponse> getRecordOwners(
        @Parameter(hidden = true)
            HttpSession httpSession,
        HttpServletRequest request
    ) throws Exception {
        List<User> users = userRepository.findAll();

        List<OwnerResponse> ownerList = new ArrayList<>();
        for (User u : users) {
            long userRecordsCount = metadataRepository.count(
                MetadataSpecs.hasOwner(u.getId())
            );
            if (userRecordsCount > 0) {
                ownerList.add(
                    new OwnerResponse(u, userRecordsCount)
                );
            }
        }
        return ownerList;
    }


    @io.swagger.v3.oas.annotations.Operation(
        summary = "Retrieve all user groups",
        description = "")
    @RequestMapping(
        value = "/groups",
        produces = MediaType.APPLICATION_JSON_VALUE,
        method = RequestMethod.GET
    )
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("isAuthenticated()")
    @ResponseBody
    public List<UserGroupsResponse> retrieveAllUserGroups(
        @Parameter(hidden = true)
            HttpSession httpSession
    ) throws Exception {
        UserSession session = ApiUtils.getUserSession(httpSession);
        Profile myProfile = session.getProfile();

        List<UserGroupsResponse> list = new ArrayList<>();
        if (myProfile == Profile.Administrator || myProfile == Profile.UserAdmin) {
            // add all admins first
            List<User> allAdmin = userRepository.findAllByProfile(Profile.Administrator);
            Group adminGroup = new Group();
            adminGroup.setName("allAdmins");
            for (User u : allAdmin) {
                list.add(
                    new UserGroupsResponse(u, adminGroup, Profile.Administrator.name())
                );
            }

            // add all users
            List<UserGroup> userGroups;

            if (myProfile == Profile.Administrator) {
                userGroups = userGroupRepository.findAll();
            } else {
                List<Integer> myGroups = userGroupRepository.findAll(UserGroupSpecs.hasUserId(session.getUserIdAsInt()))
                    .stream().map(ug -> ug.getGroup().getId()).collect(Collectors.toList());
                userGroups = userGroupRepository.findAll(UserGroupSpecs.hasGroupIds(myGroups));
            }

            for (UserGroup ug : userGroups) {
                list.add(
                    new UserGroupsResponse(ug.getUser(), ug.getGroup(), ug.getProfile().name())
                );
            }
            return list;
        } else {
            throw new SecurityException("You don't have rights to do get the groups for this user");
        }
    }

    @io.swagger.v3.oas.annotations.Operation(
        summary = "Transfer privileges",
        description = "")
    @RequestMapping(
        value = "/owners",
        produces = MediaType.APPLICATION_JSON_VALUE,
        method = RequestMethod.PUT
    )
    @PreAuthorize("hasAuthority('UserAdmin')")
    @ResponseBody
    public ResponseEntity saveOwners(
        @RequestBody
            TransferRequest transfer,
        @Parameter(hidden = true)
            HttpSession httpSession,
        HttpServletRequest request
    ) throws Exception {
        ApplicationContext applicationContext = ApplicationContextHolder.get();
        ServiceContext context = ApiUtils.createServiceContext(request);

        //--- transfer privileges (if case)

        Set<String> sourcePriv = retrievePrivileges(
            context, transfer.getSourceUser(), transfer.getSourceGroup());
        Set<String> targetPriv = retrievePrivileges(
            context, null, transfer.getTargetGroup());

        //--- a commit just to release some resources
        dataManager.flush();

        int privCount = 0;

        Set<Integer> metadata = new HashSet<Integer>();

        if (sourcePriv.size() > 0) {
            for (String priv : sourcePriv) {
                StringTokenizer st = new StringTokenizer(priv, "|");

                int opId = Integer.parseInt(st.nextToken());
                int mdId = Integer.parseInt(st.nextToken());

                // 2 cases could happen, 1) only the owner change
                // in that case sourceGrp = targetGrp and operations
                // allowed does not need to be modified.
                if (transfer.getSourceGroup() != transfer.getTargetGroup()) {
                    // 2) the sourceGrp != targetGrp and in that
                    // case, all operations need to be transfered to
                    // the new group if not already defined.
                    dataManager.unsetOperation(context, mdId, transfer.getSourceGroup(), opId);

                    if (!targetPriv.contains(priv)) {
                        OperationAllowedId id = new OperationAllowedId()
                            .setGroupId(transfer.getTargetGroup())
                            .setMetadataId(mdId)
                            .setOperationId(opId);
                        OperationAllowed operationAllowed = new OperationAllowed(id);
                        operationAllowedRepository.save(operationAllowed);
                    }
                }

                // Collect all metadata ids
                metadata.add(mdId);
                privCount++;
            }
        }
        // If no privileges defined for the target group
        // assign the new owner and ownerGroup for the source
        // user records.
        final List<Integer> sourceUserRecords =
            metadataRepository.findAllIdsBy(MetadataSpecs.hasOwner(transfer.getSourceUser()));
        metadata.addAll(sourceUserRecords);

        // Set owner for all records to be modified.
        for (Integer i : metadata) {
            final AbstractMetadata metadata1 = metadataRepository.findOne(i);
            metadata1.getSourceInfo()
                .setGroupOwner(transfer.getTargetGroup())
                .setOwner(transfer.getTargetUser());
            metadataManager.save(metadata1);
        }

        dataManager.flush();

        //--- reindex metadata
        List<String> list = new ArrayList<String>();
        for (int mdId : metadata) {
            list.add(Integer.toString(mdId));
        }
        dataManager.indexMetadata(list);
        return new ResponseEntity(HttpStatus.CREATED);
    }

    private Set<String> retrievePrivileges(ServiceContext context, Integer userId, int groupId) throws SQLException {
        final List<OperationAllowed> opsAllowed;
        if (userId == null) {
            opsAllowed = operationAllowedRepository.findAllById_GroupId(groupId);
        } else {
            opsAllowed = operationAllowedRepository.findAllWithOwner(userId, com.google.common.base.Optional.of(hasGroupId(groupId)));
        }

        Set<String> result = new HashSet<String>();
        for (OperationAllowed elem : opsAllowed) {
            int opId = elem.getId().getOperationId();
            int mdId = elem.getId().getMetadataId();
            result.add(opId + "|" + mdId);
        }
        return result;
    }
}
