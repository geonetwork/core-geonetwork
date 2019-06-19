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

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.domain.*;
import org.fao.geonet.exceptions.TaskExecutionException;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.repository.*;
import org.fao.geonet.repository.specification.MetadataSpecs;
import org.fao.geonet.repository.specification.MetadataStatusSpecs;
import org.fao.geonet.repository.specification.UserGroupSpecs;
import org.fao.geonet.utils.Log;
import org.springframework.context.ApplicationContext;
import org.springframework.data.jpa.domain.Specification;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.transaction.Transactional;
import java.util.*;

/**
 * This migration merge users sharing the same case insensitive user name. Final result will be only one user with
 * lower case username.
 */
public class MergeUsersByUsernameDatabaseMigration implements ContextAwareTask {

    @Transactional
    @Override
    public void run(ApplicationContext applicationContext) throws TaskExecutionException {
        UserRepository userRepository = applicationContext.getBean(UserRepository.class);
        List<String> duplicatedUsernamesList = userRepository.findDuplicatedUsernamesCaseInsensitive();
        Log.debug(Log.JEEVES, "Found these duplicated usernames: " + duplicatedUsernamesList);
        try {
            for (String duplicatedUsername : duplicatedUsernamesList) {
                mergeUsers(applicationContext, duplicatedUsername);
            }
        } catch (Exception e) {
            Log.error(Log.JEEVES, "Exception merging users", e);
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
        Set<Address> addresses = new HashSet<>();

        mergeGroups(applicationContext, duplicatedUserList, userToKeep);
        transferMetadata(applicationContext, duplicatedUserList, userToKeep);
        transferSavedSelections(applicationContext, duplicatedUserList, userToKeep);

        User tempUser = new User();
        for (int i = duplicatedUserList.size() - 1; i >= 0; i--) { // i = 1  is intended
            User duplicatedUser = duplicatedUserList.get(i);
            addresses.addAll(duplicatedUser.getAddresses());
            mergeUser(tempUser, duplicatedUser);
            emails.addAll(duplicatedUser.getEmailAddresses());
        }

        mergeUser(userToKeep, tempUser);
        duplicatedUserList.remove(greatestProfileUser);
        userRepository.delete(duplicatedUserList);
        userToKeep.setUsername(userToKeep.getUsername().toLowerCase());
        userRepository.save(userToKeep);
    }

    private void transferSavedSelections(ApplicationContext applicationContext, List<User> duplicatedUserList, User userToKeep) {
        UserSavedSelectionRepository userSavedSelectionRepository = applicationContext.getBean(UserSavedSelectionRepository.class);
        // TODO

        for (int i = 1; i < duplicatedUserList.size(); i++) { // i intentionally initialised to 1
            userSavedSelectionRepository.deleteAllByUser(duplicatedUserList.get(i).getId());
        }
    }

    /**
     * Transfer the metadata ownership from all the users in duplicatedUserList to userToKeep
     *
     * @param applicationContext   the application context bean factory
     * @param oldMetadataOwnerList list with the users whose metadata is going to be transferred.
     * @param newMetadataOwner     new metadata owner user.
     * @throws Exception if anything doesn't work.
     */
    void transferMetadata(ApplicationContext applicationContext, List<User> oldMetadataOwnerList,
                          User newMetadataOwner) throws Exception {
        MetadataRepository metadataRepository = applicationContext.getBean(MetadataRepository.class);
        DataManager dataManager = applicationContext.getBean(DataManager.class);
        MetadataStatusRepository metadataStatusRepository = applicationContext.getBean(MetadataStatusRepository.class);

        for (int i = 1; i < oldMetadataOwnerList.size(); i++) {
            User oldOwner = oldMetadataOwnerList.get(i);

            // Transfer metadata to user but keep old group
            List<Metadata> metadataList = metadataRepository.findAll((Specification<Metadata>)MetadataSpecs.isOwnedByUser(oldOwner.getId()));
            for (Metadata metadata : metadataList) {
                dataManager.updateMetadataOwner(metadata.getId(), Integer.toString(newMetadataOwner.getId()),
                    Integer.toString(metadata.getSourceInfo().getGroupOwner()));
            }

            // Transfer metadata status
            List<MetadataStatus> metadataStatusList = metadataStatusRepository.findAll(
                MetadataStatusSpecs.hasUserId(oldOwner.getId()));
            for (MetadataStatus metadataStatus : metadataStatusList) {
                MetadataStatusId metadataStatusId = metadataStatus.getId();

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

            dataManager.indexMetadata(Lists.transform(metadataList, new Function<Metadata, String>() {
                @Nonnull
                @Override
                public String apply(@Nullable Metadata input) {
                    return String.valueOf(input.getId());
                }
            }));
        }
    }

    /**
     * Add the userToKeep user to all the groups of duplicateduserList with the same profile these have in each group.
     *
     * @param applicationContext the application context bean factory.
     * @param duplicatedUserList list of users used to get the group list.
     * @param userToKeep         the user to add to the groups of duplicatedUserList.
     */
    void mergeGroups(ApplicationContext applicationContext, List<User> duplicatedUserList, User userToKeep) {
        GroupRepository groupRepository = applicationContext.getBean(GroupRepository.class);
        UserGroupRepository userGroupRepository = applicationContext.getBean(UserGroupRepository.class);
        Set<String> listOfAddedProfiles = new HashSet<>();
        List<UserGroup> mergedUserGroups = new ArrayList<>();
        List<Integer> userIdList = new ArrayList<>(duplicatedUserList.size());

        for (int i = 0; i < duplicatedUserList.size(); i++) {
            User userToProcess = duplicatedUserList.get(i);
            userIdList.add(userToProcess.getId());

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

        userGroupRepository.deleteAllByIdAttribute(UserGroupId_.userId,
            userIdList);
        userGroupRepository.save(mergedUserGroups);
    }

    public User mergeUser(User toOverwrite, User toKeep) {
        if (StringUtils.isNotBlank(toKeep.getSurname())) {
            toOverwrite.setSurname(toKeep.getSurname());
        }
        if (StringUtils.isNotBlank(toKeep.getName())) {
            toOverwrite.setName(toKeep.getName());
        }
        if (StringUtils.isNotBlank(toKeep.getOrganisation())) {
            toOverwrite.setOrganisation(toKeep.getOrganisation());
        }
        if (StringUtils.isNotBlank(toKeep.getKind())) {
            toOverwrite.setKind(toKeep.getKind());
        }
        if (StringUtils.isNotBlank(toKeep.getProfile().name())) {
            toOverwrite.setProfile(toKeep.getProfile());
        }

        if (!toKeep.getEmailAddresses().isEmpty()) {
            toOverwrite.getEmailAddresses().clear();
            toOverwrite.getEmailAddresses().addAll(toKeep.getEmailAddresses());
        }

        ArrayList<Address> otherAddresses = new ArrayList<Address>(toKeep.getAddresses());
        if (!otherAddresses.isEmpty()) {
            for (Iterator<Address> iterator = toOverwrite.getAddresses().iterator(); iterator.hasNext(); ) {
                Address address = iterator.next();
                boolean found = false;

                for (Iterator<Address> iterator2 = otherAddresses.iterator(); iterator2.hasNext(); ) {
                    Address otherAddress = iterator2.next();
                    if (otherAddress.getId() == address.getId()) {
                        address.mergeAddress(otherAddress, false);
                        found = true;
                        iterator2.remove();
                        break;
                    }
                }
            }
            toOverwrite.getAddresses().addAll(otherAddresses);
        }

        toOverwrite.getSecurity().mergeSecurity(toKeep.getSecurity(), false);
        return toOverwrite;
    }
}
