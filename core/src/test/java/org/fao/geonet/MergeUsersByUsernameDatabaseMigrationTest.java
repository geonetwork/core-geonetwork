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


import jeeves.server.context.ServiceContext;
import org.fao.geonet.constants.Params;
import org.fao.geonet.domain.*;
import org.fao.geonet.kernel.DataManager;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.repository.specification.MetadataSpecs;
import org.fao.geonet.repository.specification.UserGroupSpecs;
import org.fao.geonet.utils.Xml;
import org.jdom.Element;
import org.junit.Before;
import org.junit.Test;
import org.springframework.data.jpa.domain.Specification;

import java.io.ByteArrayInputStream;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;

public class MergeUsersByUsernameDatabaseMigrationTest extends AbstractCoreIntegrationTest {

    private Map<Integer, Integer> metadataIdList;

    @Before
    public void setUpData() throws Exception {
        metadataIdList = new LinkedHashMap<>();
        User user1 = _userRepo.save(newUser("Username1", Profile.Guest));
        User user2 = _userRepo.save(newUser("uSername1", Profile.Reviewer));
        User user3 = _userRepo.save(newUser("usErname1", Profile.Editor));
        User user4 = _userRepo.save(newUser("useRname1", Profile.Administrator));
        Group group1 = _groupRepo.save(newGroup("groupTest1"));
        Group group2 = _groupRepo.save(newGroup("groupTest2"));

        // user1 is UserAdmin in group1
        _userGroupRepo.save(new UserGroup().setGroup(group1).setUser(user1).setProfile(Profile.UserAdmin));
        _userGroupRepo.save(new UserGroup().setGroup(group1).setUser(user1).setProfile(Profile.Reviewer));
        _userGroupRepo.save(new UserGroup().setGroup(group1).setUser(user1).setProfile(Profile.Editor));
        _userGroupRepo.save(new UserGroup().setGroup(group1).setUser(user1).setProfile(Profile.RegisteredUser));
        _userGroupRepo.save(new UserGroup().setGroup(group1).setUser(user1).setProfile(Profile.Guest));


        // user2 is Editor in group1
        _userGroupRepo.save(new UserGroup().setGroup(group1).setUser(user2).setProfile(Profile.Editor));
        _userGroupRepo.save(new UserGroup().setGroup(group1).setUser(user2).setProfile(Profile.RegisteredUser));
        _userGroupRepo.save(new UserGroup().setGroup(group1).setUser(user2).setProfile(Profile.Guest));

        // user3 is RegisteredUser in group1
        _userGroupRepo.save(new UserGroup().setGroup(group1).setUser(user3).setProfile(Profile.RegisteredUser));
        _userGroupRepo.save(new UserGroup().setGroup(group1).setUser(user3).setProfile(Profile.Guest));

        // user3 is Reviewer in group2
        _userGroupRepo.save(new UserGroup().setGroup(group2).setUser(user3).setProfile(Profile.Reviewer));
        _userGroupRepo.save(new UserGroup().setGroup(group2).setUser(user3).setProfile(Profile.Editor));
        _userGroupRepo.save(new UserGroup().setGroup(group2).setUser(user3).setProfile(Profile.RegisteredUser));
        _userGroupRepo.save(new UserGroup().setGroup(group2).setUser(user3).setProfile(Profile.Guest));

        // user4 is Guest in group1
        _userGroupRepo.save(new UserGroup().setGroup(group1).setUser(user4).setProfile(Profile.Guest));


        ServiceContext serviceContext = createServiceContext();
        loginAs(user1, serviceContext);
        final Element sampleMetadataXml = getSampleMetadataXml();
        byte[] xmlBytes = Xml.getString(sampleMetadataXml).getBytes("UTF-8");
        final ByteArrayInputStream stream = new ByteArrayInputStream(xmlBytes);
        metadataIdList.put(importMetadataXML(serviceContext, "uuid", new ByteArrayInputStream(xmlBytes),
            MetadataType.METADATA, group1.getId(), Params.GENERATE_UUID), group1.getId());
        metadataIdList.put(importMetadataXML(serviceContext, "uuid", new ByteArrayInputStream(xmlBytes),
            MetadataType.METADATA, group1.getId(), Params.GENERATE_UUID), group1.getId());
        loginAs(user2, serviceContext);
        metadataIdList.put(importMetadataXML(serviceContext, "uuid", new ByteArrayInputStream(xmlBytes),
            MetadataType.METADATA, group1.getId(), Params.GENERATE_UUID), group1.getId());
        loginAs(user3, serviceContext);
        metadataIdList.put(importMetadataXML(serviceContext, "uuid", new ByteArrayInputStream(xmlBytes),
            MetadataType.METADATA, group1.getId(), Params.GENERATE_UUID), group1.getId());
        metadataIdList.put(importMetadataXML(serviceContext, "uuid", new ByteArrayInputStream(xmlBytes),
            MetadataType.METADATA, group2.getId(), Params.GENERATE_UUID), group2.getId());
        loginAs(user4, serviceContext);
        metadataIdList.put(importMetadataXML(serviceContext, "uuid", new ByteArrayInputStream(xmlBytes),
            MetadataType.METADATA, group1.getId(), Params.GENERATE_UUID), group1.getId());
    }

    @Test
    public void testRun() {
        User user1 = _userRepo.findOneByUsername("Username1");
        User user2 = _userRepo.findOneByUsername("uSername1");
        User user3 = _userRepo.findOneByUsername("usErname1");
        User user4 = _userRepo.findOneByUsername("useRname1");
        Group group1 = _groupRepo.findByName("groupTest1");
        Group group2 = _groupRepo.findByName("groupTest2");
        MergeUsersByUsernameDatabaseMigration migration = new MergeUsersByUsernameDatabaseMigration();
        List<User> duplicatedUserNames = _userRepo.findByUsernameIgnoreCase("username1");
        duplicatedUserNames.sort(Comparator.comparing(User::getProfile));
        User greatestProfileUser = duplicatedUserNames.get(0);
        User userTmp = migration.mergeUser(new User(), greatestProfileUser);
        userTmp.setUsername(greatestProfileUser.getUsername().toLowerCase());

        migration.run(_applicationContext);
        assertNull(_userRepo.findOne(user1.getId()));
        assertNull(_userRepo.findOne(user2.getId()));
        assertNull(_userRepo.findOne(user3.getId()));
        User mergedUser = _userRepo.findOne(greatestProfileUser.getId());
        assertNotNull(mergedUser);
        assertEquals(userTmp.getUsername(), mergedUser.getUsername());
        assertEquals(userTmp.getProfile(), mergedUser.getProfile());
        assertEquals(userTmp.getPassword(), mergedUser.getPassword());
    }

    @Test
    public void testMergeGroups() {

        User user1 = _userRepo.findOneByUsername("Username1");
        User user2 = _userRepo.findOneByUsername("uSername1");
        User user3 = _userRepo.findOneByUsername("usErname1");
        User user4 = _userRepo.findOneByUsername("useRname1");
        Group group1 = _groupRepo.findByName("groupTest1");
        Group group2 = _groupRepo.findByName("groupTest2");

        MergeUsersByUsernameDatabaseMigration migration = new MergeUsersByUsernameDatabaseMigration();
        List<User> duplicatedUserNames = _userRepo.findByUsernameIgnoreCase("username1");
        duplicatedUserNames.sort(Comparator.comparing(User::getProfile));
        User greatestProfileUser = duplicatedUserNames.get(0);

        migration.mergeGroups(_applicationContext, duplicatedUserNames, greatestProfileUser);

        assertNull(_userGroupRepo.findOne(new UserGroupId().setGroupId(group1.getId())
            .setUserId(greatestProfileUser.getId()).setProfile(Profile.Administrator)));
        assertNotNull(_userGroupRepo.findOne(new UserGroupId().setGroupId(group1.getId())
            .setUserId(greatestProfileUser.getId()).setProfile(Profile.UserAdmin)));
        assertNotNull(_userGroupRepo.findOne(new UserGroupId().setGroupId(group1.getId())
            .setUserId(greatestProfileUser.getId()).setProfile(Profile.Reviewer)));
        assertNotNull(_userGroupRepo.findOne(new UserGroupId().setGroupId(group1.getId())
            .setUserId(greatestProfileUser.getId()).setProfile(Profile.Editor)));
        assertNotNull(_userGroupRepo.findOne(new UserGroupId().setGroupId(group1.getId())
            .setUserId(greatestProfileUser.getId()).setProfile(Profile.RegisteredUser)));
        assertNotNull(_userGroupRepo.findOne(new UserGroupId().setGroupId(group1.getId())
            .setUserId(greatestProfileUser.getId()).setProfile(Profile.Guest)));

        // Reviewer in group2
        assertNull(_userGroupRepo.findOne(new UserGroupId().setGroupId(group2.getId())
            .setUserId(greatestProfileUser.getId()).setProfile(Profile.Administrator)));
        assertNull(_userGroupRepo.findOne(new UserGroupId().setGroupId(group2.getId())
            .setUserId(greatestProfileUser.getId()).setProfile(Profile.UserAdmin)));
        assertNotNull(_userGroupRepo.findOne(new UserGroupId().setGroupId(group2.getId())
            .setUserId(greatestProfileUser.getId()).setProfile(Profile.Reviewer)));
        assertNotNull(_userGroupRepo.findOne(new UserGroupId().setGroupId(group2.getId())
            .setUserId(greatestProfileUser.getId()).setProfile(Profile.Editor)));
        assertNotNull(_userGroupRepo.findOne(new UserGroupId().setGroupId(group2.getId())
            .setUserId(greatestProfileUser.getId()).setProfile(Profile.RegisteredUser)));
        assertNotNull(_userGroupRepo.findOne(new UserGroupId().setGroupId(group2.getId())
            .setUserId(greatestProfileUser.getId()).setProfile(Profile.Guest)));

        assertEquals(0, _userGroupRepo.findAll(UserGroupSpecs.hasUserId(user1.getId())).size());
        assertEquals(0, _userGroupRepo.findAll(UserGroupSpecs.hasUserId(user2.getId())).size());
        assertEquals(0, _userGroupRepo.findAll(UserGroupSpecs.hasUserId(user3.getId())).size());
        assertEquals(9, _userGroupRepo.findAll(UserGroupSpecs.hasUserId(user4.getId())).size());
    }

    @Test
    public void testTransferMetadata() throws Exception {
        User user1 = _userRepo.findOneByUsername("Username1");
        User user2 = _userRepo.findOneByUsername("uSername1");
        User user3 = _userRepo.findOneByUsername("usErname1");
        User user4 = _userRepo.findOneByUsername("useRname1");

        MergeUsersByUsernameDatabaseMigration migration = new MergeUsersByUsernameDatabaseMigration();
        List<User> duplicatedUserNames = _userRepo.findByUsernameIgnoreCase("username1");
        duplicatedUserNames.sort(Comparator.comparing(User::getProfile));
        User greatestProfileUser = duplicatedUserNames.get(0);

        final DataManager dataManager = _applicationContext.getBean(DataManager.class);
        final MetadataRepository metadataRepository = _applicationContext.getBean(MetadataRepository.class);
        migration.transferMetadata(_applicationContext, duplicatedUserNames, greatestProfileUser);
        for (Map.Entry<Integer, Integer> entry : metadataIdList.entrySet()) {
            Integer metadataId = entry.getKey();
            Integer groupId = entry.getValue();
            Metadata metadata = metadataRepository.findOne(metadataId);
            assertEquals((Integer) greatestProfileUser.getId(), metadata.getSourceInfo().getOwner());
            assertEquals(groupId, metadata.getSourceInfo().getGroupOwner());
        }
        assertEquals(0, metadataRepository.findAll((Specification<Metadata>)MetadataSpecs.isOwnedByUser(user1.getId())).size());
        assertEquals(0, metadataRepository.findAll((Specification<Metadata>)MetadataSpecs.isOwnedByUser(user2.getId())).size());
        assertEquals(0, metadataRepository.findAll((Specification<Metadata>)MetadataSpecs.isOwnedByUser(user3.getId())).size());
        assertEquals(6, metadataRepository.findAll((Specification<Metadata>)MetadataSpecs.isOwnedByUser(user4.getId())).size());
    }

    private Group newGroup(String groupName) {
        Group group = new Group();
        group.setName(groupName);
        group.setDescription(groupName + "_description");
        group.setEmail(groupName + "_group@example.com");
        group.setWebsite("http://" + groupName + ".example.com");
        return group;
    }

    private User newUser(String username, Profile profile) {
        User user = new User();
        user.setUsername(username);
        user.setProfile(profile);
        user.setName(username + "_name");
        user.setSurname(username + "_surname");
        user.setOrganisation(username + "_organisation");
        user.setEnabled(true);
        user.setLastLoginDate(new ISODate().toString());
        user.getEmailAddresses().add(username + "@example.com");
        Address address = new Address();
        address.setZip(username + "_zip");
        address.setState(username + "_state");
        address.setCountry("ES");
        address.setCity(username + "_city");
        address.setAddress(username + "_address");
        user.getAddresses().add(address);
        user.getSecurity().setPassword(username + "_password");


        return user;

    }
}
