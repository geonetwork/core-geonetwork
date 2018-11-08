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
package org.fao.geonet.kernel.datamanager;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.fao.geonet.AbstractCoreIntegrationTest;
import org.fao.geonet.domain.Group;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.Profile;
import org.fao.geonet.domain.ReservedOperation;
import org.fao.geonet.domain.User;
import org.fao.geonet.domain.UserGroup;
import org.fao.geonet.kernel.datamanager.base.BaseMetadataOperations;
import org.fao.geonet.repository.GroupRepository;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.repository.UserGroupRepository;
import org.fao.geonet.repository.UserRepository;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import jeeves.server.context.ServiceContext;

/**
 * Tests for {@link BaseMetadataOperations}.
 * 
 * @author delawen María Arias de Reyna
 * 
 */
public class BaseMetadataOperationTest extends AbstractCoreIntegrationTest {

	@Autowired
	private BaseMetadataOperations metadataOperation;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private UserGroupRepository userGroupRepository;

	@Autowired
	private GroupRepository groupRepository;

	@Autowired
	private MetadataRepository metadataRepository;

	private User user;
	private Group group;
	private Group groupOwner;
	private Metadata md;

	@Before
	public void init() {
		user = new User();
		user.setUsername("testuser");
		user.setProfile(Profile.Reviewer);
		user.setName("test");
		user.setEnabled(true);
		user = userRepository.save(user);

		group = new Group();
		group.setName("test-group");
		group = groupRepository.save(group);

		groupOwner = new Group();
		groupOwner.setName("test-group-owner");
		groupOwner = groupRepository.save(groupOwner);

		UserGroup userGroup = new UserGroup();
		userGroup.setGroup(group);
		userGroup.setUser(user);
		userGroup.setProfile(Profile.Reviewer);
		userGroupRepository.save(userGroup);

		md = new Metadata();
		md.setUuid("test-metadata");
		md.setData("<xml></xml>");
		md.getSourceInfo().setGroupOwner(groupOwner.getId());
		md.getSourceInfo().setOwner(1);
		md.getSourceInfo().setSourceId("test-faking");
		md.getDataInfo().setSchemaId("isoFake");
		metadataRepository.save(md);
	}

	@Test
	public void test() throws Exception {
		ServiceContext context = createServiceContext();

		assertTrue(metadataOperation.getAllOperations(md.getId()).isEmpty());
		assertTrue(metadataOperation.existsUser(context, user.getId()));

		assertTrue(metadataOperation
				.getOperationAllowedToAdd(context, md.getId(), group.getId(), ReservedOperation.view.getId())
				.isPresent());
		assertTrue(metadataOperation
				.getOperationAllowedToAdd(context, md.getId(), group.getId(), ReservedOperation.editing.getId())
				.isPresent());

		assertTrue(metadataOperation.setOperation(context, md.getId(), group.getId(),
				ReservedOperation.view.getId()));

		assertTrue(metadataOperation.setOperation(context, md.getId(), group.getId(),
				ReservedOperation.editing.getId()));
		
		assertFalse(metadataOperation
				.getOperationAllowedToAdd(context, md.getId(), group.getId(), ReservedOperation.view.getId())
				.isPresent());
		assertFalse(metadataOperation
				.getOperationAllowedToAdd(context, md.getId(), group.getId(), ReservedOperation.editing.getId())
				.isPresent());

		assertTrue(metadataOperation.existsUser(context, user.getId()));

		metadataOperation.unsetOperation(context, md.getId(), group.getId(), ReservedOperation.view.getId());
		metadataOperation.unsetOperation(context, md.getId(), group.getId(), ReservedOperation.editing.getId());

		assertTrue(metadataOperation
				.getOperationAllowedToAdd(context, md.getId(), group.getId(), ReservedOperation.view.getId())
				.isPresent());
		assertTrue(metadataOperation
				.getOperationAllowedToAdd(context, md.getId(), group.getId(), ReservedOperation.editing.getId())
				.isPresent());

	}
	

	@After
	public void cleanup() {
		metadataRepository.delete(md);
		groupRepository.delete(group);
		groupRepository.delete(groupOwner);
		userRepository.delete(user);
	}
}
