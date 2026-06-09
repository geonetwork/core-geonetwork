//=============================================================================
//===	Copyright (C) 2001-2024 Food and Agriculture Organization of the
//===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===	and United Nations Environment Programme (UNEP)
//===
//===	This program is free software; you can redistribute it and/or modify
//===	it under the terms of the GNU General Public License as published by
//===	the Free Software Foundation; either version 2 of the License, or (at
//===	your option) any later version.
//===
//===	This program is distributed in the hope that it will be useful, but
//===	WITHOUT ANY WARRANTY; without even the implied warranty of
//===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
//===	General Public License for more details.
//===
//===	You should have received a copy of the GNU General Public License
//===	along with this program; if not, write to the Free Software
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301, USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: geonetwork@osgeo.org
//==============================================================================

package org.fao.geonet.kernel.harvest.harvester.database;

import org.fao.geonet.Logger;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.utils.Log;
import org.junit.Test;

import static org.junit.Assert.*;

public class DatabaseMetadataRetrieverFactoryTest {

    @Test
    public void testNonValidDatabaseType() {
        Logger log = Log.createLogger(Geonet.HARVESTER);

        try {
            DatabaseMetadataRetrieverFactory.getDatabaseMetadataRetriever("nonvalid", "localhost", 5432, "test", "username", "password", log);
            fail();

        } catch (DatabaseMetadataRetrieverException ex) {
            assertEquals("Connection for database type nonvalid not supported", ex.getMessage());
        }

    }

    @Test
    public void testValidDatabaseType() {
        Logger log = Log.createLogger(Geonet.HARVESTER);

        try {
            DatabaseMetadataRetrieverFactory.getDatabaseMetadataRetriever("postgresql", "localhost", 5432,
                "test", "username", "password", log);
            fail();
        } catch (DatabaseMetadataRetrieverException ex) {
            fail();
        } catch (ExceptionInInitializerError ex) {
            // The connection fails as no Postgres database available,
            // but it should not fail due to an unsupported database type.
            assertNotNull(ex.getCause());
            assertTrue(ex.getCause() instanceof DatabaseMetadataRetrieverException );

            assertEquals("Exception in getting database connection: can not connect to the database",
                ex.getCause().getMessage());
        }

    }

}
