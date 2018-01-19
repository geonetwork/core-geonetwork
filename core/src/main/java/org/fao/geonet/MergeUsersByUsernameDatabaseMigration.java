/*
 * Copyright (C) 2001-2018 Food and Agriculture Organization of the
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
package org.fao.geonet;

import com.google.common.collect.Collections2;
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.*;
import org.fao.geonet.exceptions.TaskExecutionException;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.repository.*;
import org.fao.geonet.repository.specification.MetadataSpecs;
import org.fao.geonet.repository.specification.MetadataStatusSpecs;
import org.fao.geonet.repository.specification.UserGroupSpecs;
import org.fao.geonet.utils.Log;
import org.springframework.context.ApplicationContext;

import java.util.*;

/**
 * This migration merge users sharing the same case insensitive user name.
 *
 *
 */
public class MergeUsersByUsernameDatabaseMigration implements ContextAwareTask {

    @Override
    public void run(ApplicationContext applicationContext) throws TaskExecutionException {
        UserRepository userRepository = applicationContext.getBean(UserRepository.class);
        List<String> duplicatedUsernamesList = userRepository.findDuplicatedUsernamesCaseInsensitive();
        Log.debug(Geonet.DB,"Found these duplicated usernames: " + duplicatedUsernamesList);
        try {
            for (String duplicatedUsername : duplicatedUsernamesList) {
                mergeUsers(applicationContext, duplicatedUsername);
            }
        } catch (Exception e) {
            throw new TaskExecutionException(e);
        }

    }

    private void mergeUsers(ApplicationContext applicationContext, String duplicatedUsername) throws Exception {
        UserRepository userRepository = applicationContext.getBean(UserRepository.class);
        List<User> duplicatedUserList = userRepository.findByUsernameIgnoreCase(duplicatedUsername);

        duplicatedUserList.sort(Comparator.comparing(User::getProfile));
        User greatestProfileUser = duplicatedUserList.get(0);
        User userToKeep = userRepository.findOne(greatestProfileUser.getId());
        Set<String> emails = new HashSet<>();
        for(User duplicatedUser : duplicatedUserList) {
            userToKeep.mergeUser(duplicatedUser, false);
            emails.addAll(duplicatedUser.getEmailAddresses());
        }
        // Merge the greatestProfileUser again to keep its values
        userToKeep.mergeUser(greatestProfileUser, false);
        userToKeep.getEmailAddresses().addAll(emails);

        mergeGroups(applicationContext, duplicatedUserList, userToKeep);
        transferMetadata(applicationContext, duplicatedUserList, userToKeep);
        transferSavedSelections(applicationContext, duplicatedUserList, userToKeep);



        userToKeep = userRepository.save(userToKeep);
        duplicatedUserList.remove(greatestProfileUser);
        userRepository.delete(duplicatedUserList);



    }

    private void transferSavedSelections(ApplicationContext applicationContext, List<User> duplicatedUserList, User userToKeep) {
        UserSavedSelectionRepository userSavedSelectionRepository = applicationContext.getBean(UserSavedSelectionRepository.class);
        // TODO
    }

    /**
     * Transfer the metadata ownership from all the users in duplicatedUserList to userToKeep
     * @param applicationContext the application context bean factory
     * @param oldMetadataOwnerList list with the users whose metadata is going to be transferred.
     * @param newMetadataOwner new metadata owner user.
     * @throws Exception if anything doesn't work.
     */
    private void transferMetadata(ApplicationContext applicationContext, List<User> oldMetadataOwnerList,
                                  User newMetadataOwner) throws Exception {
        MetadataRepository metadataRepository = applicationContext.getBean(MetadataRepository.class);
        DataManager dataManager = applicationContext.getBean(DataManager.class);
        MetadataStatusRepository metadataStatusRepository = applicationContext.getBean(MetadataStatusRepository.class);

        for (int i = 1; i < oldMetadataOwnerList.size(); i++) {
            User oldOwner = oldMetadataOwnerList.get(i);

            // Transfer metadata to user but keep old group
            List<Metadata> metadataList = metadataRepository.findAll(MetadataSpecs.isOwnedByUser(oldOwner.getId()));
            for (Metadata metadata : metadataList) {
                dataManager.updateMetadataOwner(metadata.getId(), Integer.toString(newMetadataOwner.getId()),
                    Integer.toString(metadata.getSourceInfo().getGroupOwner()));
            }

            // Transfer metadata status
            List<MetadataStatus> metadataStatusList = metadataStatusRepository.findAll(
                MetadataStatusSpecs.hasUserId(oldOwner.getId()));
            for (MetadataStatus metadataStatus : metadataStatusList) {
                MetadataStatusId  metadataStatusId = metadataStatus.getId();

                MetadataStatusId newMetadataStatusId = new MetadataStatusId();
                newMetadataStatusId.setUserId(newMetadataOwner.getId());
                newMetadataStatusId.setMetadataId(metadataStatusId.getMetadataId());
                newMetadataStatusId.setChangeDate(metadataStatusId.getChangeDate());
                metadataStatusId.setStatusId(metadataStatusId.getStatusId());

                MetadataStatus newMetadataStatus = new MetadataStatus();
                newMetadataStatus.setId(newMetadataStatusId);
                newMetadataStatus.setChangeMessage(metadataStatus.getChangeMessage());
                newMetadataStatus.setStatusValue(metadataStatus.getStatusValue());
                metadataStatusRepository.save(newMetadataStatus);
            }
            metadataStatusRepository.deleteAllById_UserId(oldOwner.getId());
            metadataStatusRepository.save(metadataStatusList);

        }

    }

    /**
     * Add the userToKeep user to all the groups of duplicateduserList with the same profile these have in each group.
     * @param applicationContext the application context bean factory.
     * @param duplicatedUserList list of users used to get the group list.
     * @param userToKeep the user to add to the groups of duplicatedUserList.
     */
    private void mergeGroups(ApplicationContext applicationContext, List<User> duplicatedUserList, User userToKeep) {
        GroupRepository groupRepository = applicationContext.getBean(GroupRepository.class);
        UserGroupRepository userGroupRepository = applicationContext.getBean(UserGroupRepository.class);
        Set<String> listOfAddedProfiles = new HashSet<>();
        List<UserGroup> mergedUserGroups = new ArrayList<>();

        for(int i = 0; i < duplicatedUserList.size(); i++) {
            User userToProcess = duplicatedUserList.get(i);

            List<UserGroup> userGroupsToProcess = userGroupRepository.findAll(
                UserGroupSpecs.hasUserId(userToProcess.getId()));
            if (Log.isDebugEnabled(Log.JEEVES)) {
                Log.debug(Log.JEEVES, "Groups of user {" + userToProcess.getId() + " - "
                    + userToProcess.getUsername() + "}: " + userGroupsToProcess.size());
                Log.debug(Log.JEEVES, userGroupsToProcess);
            }

            for (UserGroup ug : userGroupsToProcess) {
                String key = ug.getProfile().name() + "#" + ug.getGroup().getId();
                if (!listOfAddedProfiles.contains(key)) {
                    listOfAddedProfiles.add(key);
                }
            }
        }
        // Create a new UserGroup for each one of the old profiles
        for (String key : listOfAddedProfiles) {
            String[] splitKey = StringUtils.split(key, "#");
            String profileName = splitKey[0];
            Integer groupId = Integer.valueOf(splitKey[1]);

            Group group = groupRepository.findOne(groupId);
            Profile profile = Profile.findProfileIgnoreCase(profileName);
            UserGroup userGroup = new UserGroup().setGroup(group).setProfile(profile).setUser(userToKeep);
            mergedUserGroups.add(userGroup);
        }

        userGroupRepository.save(mergedUserGroups);
    }
}
