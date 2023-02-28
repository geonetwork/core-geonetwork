/*
 * Copyright (C) 2023 Food and Agriculture Organization of the
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

import org.fao.geonet.domain.FavouriteMetadataList;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class FavouriteMetadataListItemRepositoryTest extends AbstractSpringDataTest {

    @Autowired
    FavouriteMetadataListItemRepository _repo;

    @Autowired
    FavouriteMetadataListRepository favouriteMetadataListRepository;


    @Test
    public void testQueryByParent() {
        FavouriteMetadataList list = FavouriteMetadataListRepositoryTest.createListWithItems("testcase1","list.");
        FavouriteMetadataList list2 = FavouriteMetadataListRepositoryTest.createListWithItems("testcase2","list2.");
        list= favouriteMetadataListRepository.save(list);
        list2= favouriteMetadataListRepository.save(list2);

        List<String> items = _repo.queryByParent(list.getId());
        assertEquals(2, items.size());
        assertTrue(items.contains("list.metadataid1"));
        assertTrue(items.contains("list.metadataid2"));


        List<String> items2 = _repo.queryByParent(list2.getId());
        assertEquals(2, items2.size());
        assertTrue(items2.contains("list2.metadataid1"));
        assertTrue(items2.contains("list2.metadataid2"));
    }

}
