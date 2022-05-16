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

package v3110;

import java.sql.Connection;
import java.sql.SQLException;

import org.fao.geonet.DatabaseMigrationTask;
import org.fao.geonet.EncryptorInitializer;
import org.springframework.context.ApplicationContext;


/**
 * Class to be executed during the migration which will flag the encryptor to do an password update.
 *
 */
public class MigrateEncryptor extends DatabaseMigrationTask {
    private EncryptorInitializer encryptorInitializer;

    /**
     * Override the setContext so do the autowire of the other fields.
     *
     * @param applicationContext
     */
    @Override
    public void setContext(ApplicationContext applicationContext) {
        super.setContext(applicationContext);
        encryptorInitializer = applicationContext.getBean(EncryptorInitializer.class);
        encryptorInitializer.setFirstInitialSetupFlag(true);
    }

    /**
     * update initial setup flag to indicate that a existing password should be done encrypted.
     *
     * @param connection
     * @throws SQLException
     */
    @Override
    public void update(Connection connection) throws SQLException {
        encryptorInitializer.setFirstInitialSetupFlag(true);
    }
}
