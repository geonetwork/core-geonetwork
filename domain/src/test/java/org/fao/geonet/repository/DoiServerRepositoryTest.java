/*
 * Copyright (C) 2001-2024 Food and Agriculture Organization of the
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

package org.fao.geonet.repository;

import org.fao.geonet.domain.DoiServer;
import org.fao.geonet.domain.Group;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jasypt.hibernate5.encryptor.HibernatePBEEncryptorRegistry;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

public class DoiServerRepositoryTest extends AbstractSpringDataTest {

    @Autowired
    private DoiServerRepository doiServerRepository;

    @Autowired
    private GroupRepository groupRepository;

    @PersistenceContext
    EntityManager entityManager;

    @BeforeClass
    public static void init() {
        StandardPBEStringEncryptor strongEncryptor = new StandardPBEStringEncryptor();
        strongEncryptor.setPassword("testpassword");

        HibernatePBEEncryptorRegistry registry =
            HibernatePBEEncryptorRegistry.getInstance();
        registry.registerPBEStringEncryptor("STRING_ENCRYPTOR", strongEncryptor);
    }

    public static DoiServer newDoiServer(AtomicInteger nextId) {
        int id = nextId.incrementAndGet();
        return new DoiServer()
            .setName("Name " + id)
            .setDescription("Desc " + id)
            .setUrl("http://server" + id)
            .setUsername("username" + id)
            .setPassword("password" + id)
            .setLandingPageTemplate("http://landingpage" + id)
            .setPublicUrl("http://publicurl" + id)
            .setPattern("pattern" + id)
            .setPrefix("prefix" + id);
    }

    @Test
    public void test_Save_Count_FindOnly_DeleteAll() throws Exception {
        assertEquals(0, doiServerRepository.count());
        DoiServer doiServer = newDoiServer();
        DoiServer savedDoiServer = doiServerRepository.save(doiServer);

        doiServerRepository.flush();
        entityManager.flush();
        entityManager.clear();

        doiServer.setId(savedDoiServer.getId());
        assertEquals(1, doiServerRepository.count());
        Optional<DoiServer> retrievedDoiServerByIdOpt = doiServerRepository.findOneById(doiServer.getId());
        assertEquals(true, retrievedDoiServerByIdOpt.isPresent());
        assertSameContents(doiServer, retrievedDoiServerByIdOpt.get());

        doiServerRepository.deleteAll();

        doiServerRepository.flush();
        entityManager.flush();
        entityManager.clear();

        assertEquals(0, doiServerRepository.count());
    }

    @Test
    public void testUpdate() throws Exception {
        Group group1 = groupRepository.save(GroupRepositoryTest.newGroup(_inc));
        Group group2 = groupRepository.save(GroupRepositoryTest.newGroup(_inc));

        assertEquals(0, doiServerRepository.count());
        DoiServer doiServer = newDoiServer();
        doiServer.getPublicationGroups().add(group1);

        DoiServer savedDoiServer = doiServerRepository.save(doiServer);

        doiServerRepository.flush();
        entityManager.flush();
        entityManager.clear();

        doiServer.setId(savedDoiServer.getId());

        assertEquals(1, doiServerRepository.count());
        Optional<DoiServer> retrievedDoiServerByIdOpt = doiServerRepository.findOneById(doiServer.getId());
        assertEquals(true, retrievedDoiServerByIdOpt.isPresent());
        assertSameContents(doiServer, retrievedDoiServerByIdOpt.get());

        doiServer.setName("New Name");
        doiServer.getPublicationGroups().add(group2);
        DoiServer savedDoiServer2 = doiServerRepository.save(doiServer);

        doiServerRepository.flush();
        entityManager.flush();
        entityManager.clear();

        assertSameContents(savedDoiServer, savedDoiServer2);

        assertEquals(1, doiServerRepository.count());
        retrievedDoiServerByIdOpt = doiServerRepository.findOneById(doiServer.getId());
        assertSameContents(doiServer, retrievedDoiServerByIdOpt.get());
    }


    private DoiServer newDoiServer() {
        return newDoiServer(_inc);
    }
}
