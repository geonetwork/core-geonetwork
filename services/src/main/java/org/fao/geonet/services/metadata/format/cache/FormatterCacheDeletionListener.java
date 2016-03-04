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

package org.fao.geonet.services.metadata.format.cache;

import org.fao.geonet.events.md.MetadataRemove;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;

import java.io.IOException;
import java.sql.SQLException;

/**
 * This class is responsible for listening for metadata index events and updating the cache's publication values so that it stays in
 * sync with the actual metadata.
 *
 * @author Jesse on 3/6/2015.
 */
public class FormatterCacheDeletionListener implements ApplicationListener<MetadataRemove> {
    @Autowired
    private FormatterCache formatterCache;

    @Override
    public synchronized void onApplicationEvent(MetadataRemove event) {
        final int metadataId = event.getMd().getId();
        try {
            formatterCache.removeAll(metadataId);
        } catch (SQLException | IOException e) {
            throw new RuntimeException(e);
        }

    }
}
