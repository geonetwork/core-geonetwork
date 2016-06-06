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

package org.fao.geonet.services.reports;

import jeeves.server.UserSession;
import jeeves.server.context.ServiceContext;

import org.fao.geonet.AbstractCoreIntegrationTest;
import org.fao.geonet.domain.ISODate;
import org.fao.geonet.domain.Metadata;
import org.fao.geonet.domain.MetadataType;
import org.fao.geonet.domain.ReservedGroup;
import org.fao.geonet.repository.MetadataRepository;
import org.fao.geonet.repository.Updater;
import org.fao.geonet.services.AbstractServiceIntegrationTest;
import org.jdom.Element;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.net.URL;
import java.util.Calendar;
import java.util.UUID;

import javax.annotation.Nonnull;

import static org.fao.geonet.domain.Pair.read;
import static org.junit.Assert.assertEquals;

public class ReportUpdatedMetadataTest extends AbstractServiceIntegrationTest {

    final ReportUpdatedMetadata metadata = new ReportUpdatedMetadata();
    @Autowired
    MetadataRepository metadataRepository;

    @Test
    public void testExecNoData() throws Exception {
        ServiceContext context = createServiceContext();
        context.setUserSession(new UserSession());
        final String now = getTomorrow();
        final String lastMonth = getLastMonth();
        assertRecords(0, createParams(read("dateFrom", now), read("dateTo", now)), context);
        assertRecords(0, createParams(read("dateFrom", lastMonth), read("dateTo", now)), context);
        assertRecords(0, createParams(read("dateFrom", lastMonth), read("dateTo", now), read("groups", "2"),
            read("groups", "42")), context);


        loginAsAdmin(context);
        assertRecords(0, createParams(read("dateFrom", now), read("dateTo", now)), context);
        assertRecords(0, createParams(read("dateFrom", lastMonth), read("dateTo", now)), context);
        assertRecords(0, createParams(read("dateFrom", lastMonth), read("dateTo", now), read("groups", "2"),
            read("groups", "42")), context);
        // there was a bug where certain queries would cause an exception.  We are just checking that no exception occurs
    }

    @Test
    public void testExec() throws Exception {

        final URL resource = AbstractCoreIntegrationTest.class.getResource("kernel/valid-metadata.iso19139.xml");

        String uuid = UUID.randomUUID().toString();
        ServiceContext importContext = createServiceContext();
        loginAsAdmin(importContext);
        importMetadataXML(importContext, uuid, resource.openStream(), MetadataType.METADATA, ReservedGroup.all.getId(), uuid);
        uuid = UUID.randomUUID().toString();
        final int mdId1 = importMetadataXML(importContext, uuid, resource.openStream(), MetadataType.METADATA, ReservedGroup.all.getId(),
            uuid);

        metadataRepository.update(mdId1, new Updater<Metadata>() {
            @Override
            public void apply(@Nonnull Metadata entity) {
                entity.getSourceInfo().setGroupOwner(null);
            }
        });


        ServiceContext context = createServiceContext();
        context.setUserSession(new UserSession());
        final String now = getTomorrow();
        final String lastMonth = getLastMonth();
        assertRecords(0, createParams(read("dateFrom", now), read("dateTo", now)), context);
        assertRecords(2, createParams(read("dateFrom", lastMonth), read("dateTo", now)), context);
        assertRecords(0, createParams(read("dateFrom", lastMonth), read("dateTo", now), read("groups", "2"), read("groups", "42")), context);
        assertRecords(1, createParams(read("dateFrom", lastMonth), read("dateTo", now), read("groups", ReservedGroup.all.getId()),
            read("groups", "42")), context);


        loginAsAdmin(context);
        assertRecords(0, createParams(read("dateFrom", now), read("dateTo", now)), context);
        assertRecords(2, createParams(read("dateFrom", lastMonth), read("dateTo", now)), context);
        assertRecords(0, createParams(read("dateFrom", lastMonth), read("dateTo", now), read("groups", "2"), read("groups", "42")), context);
        assertRecords(1, createParams(read("dateFrom", lastMonth), read("dateTo", now), read("groups", ReservedGroup.all.getId()),
            read("groups", "42")), context);
        // there was a bug where certain queries would cause an exception.  We are just checking that no exception occurs
    }

    protected String getTomorrow() {
        final Calendar instance = Calendar.getInstance();
        instance.add(Calendar.DAY_OF_YEAR, 1);
        return new ISODate(instance.getTime().getTime(), true).getDateAsString();
    }

    protected String getLastMonth() {
        final Calendar instance = Calendar.getInstance();
        instance.add(Calendar.MONTH, -1);
        return new ISODate(instance.getTime().getTime(), true).getDateAsString();
    }

    private void assertRecords(int expectedNumberOfRecords, Element params, ServiceContext context) throws Exception {
        final Element result = metadata.exec(params, context);
        assertEquals(expectedNumberOfRecords, result.getChildren("record").size());
    }
}
