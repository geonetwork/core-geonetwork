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

package org.fao.geonet.repository;


import org.fao.geonet.domain.HarvestHistory;
import org.jdom.Element;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.*;

public class HarvestHistoryRepositoryTest extends AbstractSpringDataTest {

    @Autowired
    HarvestHistoryRepository _repo;

    public static HarvestHistory createHarvestHistory(AtomicInteger inc) {
        int val = inc.incrementAndGet();
        HarvestHistory customElementSet = new HarvestHistory()
            .setDeleted(val % 2 == 0)
            .setHarvesterName("name" + val)
            .setHarvesterType("type" + val)
            .setHarvesterUuid("uuid" + val);
        return customElementSet;
    }

    @Test
    public void testFindOne() {
        HarvestHistory history1 = newHarvestHistory();
        history1 = _repo.save(history1);

        HarvestHistory history2 = newHarvestHistory();
        history2 = _repo.save(history2);


        assertEquals(history2, _repo.findOne(history2.getId()));
        assertEquals(history1, _repo.findOne(history1.getId()));
    }

    @Test
    public void testFindCustomFindAllAsXml() {
        HarvestHistory history1 = newHarvestHistory();
        String infoText = "this is the info string";
        String paramText = "this is the param string";
        history1.setInfo(new Element("infodata").setText(infoText));
        history1.setParams(new Element("params").addContent(new Element("param1").setText(paramText)));
        _repo.save(history1);

        Element xml = _repo.findAllAsXml();

        Element history1AsEl = (Element) xml.getChildren().get(0);
        final Element info = history1AsEl.getChild("info");
        assertNotNull(info);
        final Element infodata = info.getChild("infodata");
        assertNotNull(infodata);
        assertEquals(infoText, infodata.getText());

        final Element params = history1AsEl.getChild("params");
        assertNotNull(params);
        final Element params2 = params.getChild("params");
        assertNotNull(params2);
        final Element param1 = params2.getChild("param1");
        assertNotNull(param1);
        assertEquals(paramText, param1.getText());
    }

    @Test
    public void testFindByEmailCswServerCapabilitiesInfo() {
        HarvestHistory history1 = newHarvestHistory();
        history1 = _repo.save(history1);

        HarvestHistory history2 = newHarvestHistory();
        history2 = _repo.save(history2);

        List<HarvestHistory> histories = _repo.findAllByHarvesterType(history1.getHarvesterType());

        assertEquals(history1.getHarvesterType(), histories.get(0).getHarvesterType());

        histories = _repo.findAllByHarvesterType(history2.getHarvesterType());

        assertEquals(history2.getHarvesterType(), histories.get(0).getHarvesterType());
    }

    @Test
    public void testMarkAllAsDeleted() {
        HarvestHistory history1 = newHarvestHistory();
        history1.setDeleted(false);
        history1 = _repo.save(history1);

        HarvestHistory history2 = newHarvestHistory();
        history2.setDeleted(false);
        history2 = _repo.save(history2);

        _repo.markAllAsDeleted(history1.getHarvesterUuid());

        List<HarvestHistory> found = _repo.findAll(Collections.singleton(history1.getId()));
        assertTrue(found.get(0).isDeleted());
        List<HarvestHistory> found2 = _repo.findAll(Collections.singleton(history2.getId()));
        assertFalse(found2.get(0).isDeleted());
        assertTrue(_repo.findOne(history1.getId()).isDeleted());
        assertFalse(_repo.findOne(history2.getId()).isDeleted());
    }

    @Test
    public void testDeleteAllById() {
        HarvestHistory history1 = newHarvestHistory();
        history1.setDeleted(false);
        history1 = _repo.save(history1);

        HarvestHistory history2 = newHarvestHistory();
        history2.setDeleted(false);
        history2 = _repo.save(history2);

        _repo.deleteAllById(Arrays.asList(history1.getId()));

        List<HarvestHistory> found = _repo.findAll();
        assertEquals(1, found.size());
        assertEquals(history2.getId(), found.get(0).getId());

        assertNull(_repo.findOne(history1.getId()));
    }

    private HarvestHistory newHarvestHistory() {
        return createHarvestHistory(_inc);
    }

}
