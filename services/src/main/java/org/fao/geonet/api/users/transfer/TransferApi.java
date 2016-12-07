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

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.api.API;
import org.fao.geonet.api.ApiUtils;
import org.fao.geonet.domain.*;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.repository.OperationAllowedRepository;
import org.fao.geonet.repository.UserGroupRepository;
import org.fao.geonet.repository.UserRepository;
import org.fao.geonet.repository.specification.MetadataSpecs;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import springfox.documentation.annotations.ApiIgnore;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.sql.SQLException;
import java.util.*;

import static org.fao.geonet.repository.specification.OperationAllowedSpecs.hasGroupId;

@RequestMapping(value = {
    "/api/users",
    "/api/" + API.VERSION_0_1 +
        "/users"
})
@Api(value = "users",
    tags = "users",
    description = "User operations")
@Controller("usersTransfer")
public class TransferApi {

    @ApiOperation(
        value = "Get owners",
        notes = "Return users who actually owns one or more records.",
        nickname = "getOwners")
    @RequestMapping(
        path = "owners",
        produces = MediaType.APPLICATION_JSON_VALUE,
        method = RequestMethod.GET
    )
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("hasRole('UserAdmin')")
    @ResponseBody
    public List<OwnerResponse> getUsers(
        @ApiIgnore
            HttpSession httpSession,
        HttpServletRequest request
    ) throws Exception {
        UserSession us = ApiUtils.getUserSession(httpSession);
        ApplicationContext applicationContext = ApplicationContextHolder.get();
        List<User> users = applicationContext.getBean(UserRepository.class).findAll();
        MetadataRepository metadataRepository = applicationContext.getBean(MetadataRepository.class);

        List<OwnerResponse> ownerList = new ArrayList<>();
        for (User u : users) {
            List<Metadata> userRecords = metadataRepository.findAll(
                MetadataSpecs.hasOwner(u.getId())
            );
            if (userRecords.size() > 0) {
                ownerList.add(
                    new OwnerResponse(u, userRecords.size())
                );
            }
        }
        return ownerList;
    }


    @ApiOperation(
        value = "Retrieve all user groups",
        notes = "",
        nickname = "retrieveAllUserGroups")
    @RequestMapping(
        value = "/groups",
        produces = MediaType.APPLICATION_JSON_VALUE,
        method = RequestMethod.GET
    )
    @ResponseStatus(value = HttpStatus.OK)
    @PreAuthorize("isAuthenticated()")
    @ResponseBody
    public List<UserGroupsResponse> retrieveAllUserGroups(
        @ApiIgnore
            HttpSession httpSession
    ) throws Exception {
        UserSession session = ApiUtils.getUserSession(httpSession);
        Profile myProfile = session.getProfile();


        ApplicationContext applicationContext = ApplicationContextHolder.get();
        final UserRepository userRepository = applicationContext.getBean(UserRepository.class);
        final UserGroupRepository userGroupRepository = applicationContext.getBean(UserGroupRepository.class);
        List<UserGroupsResponse> list = new ArrayList<>();
        if (myProfile == Profile.Administrator || myProfile == Profile.UserAdmin) {
            List<User> allAdmin = userRepository.findAllByProfile(Profile.Administrator);
            List<UserGroup> userGroups = userGroupRepository.findAll();
            for (UserGroup ug : userGroups) {
                list.add(
                    new UserGroupsResponse(ug.getUser(), ug.getGroup(), ug.getProfile().name())
                );
                for (User u : allAdmin) {
                    list.add(
                        new UserGroupsResponse(u, ug.getGroup(), Profile.Administrator.name())
                    );
                }
            }
            return list;
        } else {
            throw new SecurityException("You don't have rights to do get the groups for this user");
        }
    }

    @ApiOperation(
        value = "Transfer privileges",
        notes = "",
        nickname = "saveOwners")
    @RequestMapping(
        value = "/owners",
        produces = MediaType.APPLICATION_JSON_VALUE,
        method = RequestMethod.PUT
    )
    @PreAuthorize("hasRole('UserAdmin')")
    @ResponseBody
    public ResponseEntity saveOwners(
        @RequestBody
            TransferRequest transfer,
        @ApiIgnore
            HttpSession httpSession,
        HttpServletRequest request
    ) throws Exception {
        ApplicationContext applicationContext = ApplicationContextHolder.get();
        ServiceContext context = ApiUtils.createServiceContext(request);
        DataManager dm = applicationContext.getBean(DataManager.class);
        final MetadataRepository metadataRepository = applicationContext.getBean(MetadataRepository.class);

        //--- transfer privileges (if case)

        Set<String> sourcePriv = retrievePrivileges(
            context, transfer.getSourceUser(), transfer.getSourceGroup());
        Set<String> targetPriv = retrievePrivileges(
            context, null, transfer.getTargetGroup());

        //--- a commit just to release some resources
        dm.flush();

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
                    dm.unsetOperation(context, mdId, transfer.getSourceGroup(), opId);

                    if (!targetPriv.contains(priv)) {
                        OperationAllowedRepository repository = context.getBean(OperationAllowedRepository.class);
                        OperationAllowedId id = new OperationAllowedId()
                            .setGroupId(transfer.getTargetGroup())
                            .setMetadataId(mdId)
                            .setOperationId(opId);
                        OperationAllowed operationAllowed = new OperationAllowed(id);
                        repository.save(operationAllowed);
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
            final Metadata metadata1 = metadataRepository.findOne(i);
            metadata1.getSourceInfo()
                .setGroupOwner(transfer.getTargetGroup())
                .setOwner(transfer.getTargetUser());
            metadataRepository.save(metadata1);
        }

        dm.flush();

        //--- reindex metadata
        List<String> list = new ArrayList<String>();
        for (int mdId : metadata) {
            list.add(Integer.toString(mdId));
        }
        dm.indexMetadata(list);
        return new ResponseEntity(HttpStatus.CREATED);
    }

    private Set<String> retrievePrivileges(ServiceContext context, Integer userId, int groupId) throws SQLException {
        OperationAllowedRepository opAllowedRepo = context.getBean(OperationAllowedRepository.class);
        final List<OperationAllowed> opsAllowed;
        if (userId == null) {
            opsAllowed = opAllowedRepo.findAllById_GroupId(groupId);
        } else {
            opsAllowed = opAllowedRepo.findAllWithOwner(userId, com.google.common.base.Optional.of(hasGroupId(groupId)));
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
