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

import org.fao.geonet.domain.Link;
import org.fao.geonet.domain.LinkType;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class LinkRespositoryTest extends AbstractSpringDataTest {

    @Autowired
    private LinkRepository repository;

    @Test
    public void testFindAllByUrlIn() {
        Link link = new Link();
        link.setLinkType(LinkType.HTTP);
        link.setUrl("https://test.com/link");

        repository.save(link);

        List<String> links = new ArrayList<>();
        links.add(link.getUrl());
        List<Link> linkList = repository.findAllByUrlIn(links);

        Assert.assertNotNull(linkList);
        Assert.assertEquals(1, linkList.size());
        Assert.assertEquals(link.getUrl(), linkList.get(0).getUrl());
    }

    @Test
    public void testFindAllByUrlInNoResults() {
        List<String> links = new ArrayList<>();
        links.add("https://test.com/link");
        List<Link> linkList = repository.findAllByUrlIn(links);

        Assert.assertNotNull(linkList);
        Assert.assertEquals(0, linkList.size());
    }

    @Test
    public void testFindOneByUrl() {
        Link link = new Link();
        link.setLinkType(LinkType.HTTP);
        link.setUrl("https://test.com/link");

        repository.save(link);

        Optional<Link> linkToCheck = repository.findOneByUrl("https://test.com/link");

        Assert.assertNotNull(linkToCheck);
        Assert.assertTrue(linkToCheck.isPresent());
        Assert.assertEquals(link.getUrl(), linkToCheck.get().getUrl());
    }

    @Test
    public void testFindOneByUrlNoResult() {
        Optional<Link> link = repository.findOneByUrl("https://test.com/link");

        Assert.assertNotNull(link);
        Assert.assertTrue(!link.isPresent());
    }

}
