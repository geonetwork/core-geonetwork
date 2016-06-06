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

package org.fao.geonet;

import com.google.common.base.Optional;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.datasource.embedded.ConnectionProperties;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseConfigurer;
import org.springframework.util.ClassUtils;

import javax.sql.DataSource;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.Callable;

/**
 * Configures the H2 database to be a bit more string so mysql and other databases are more likely
 * to work if the tests pass.
 *
 * User: Jesse Date: 10/31/13 Time: 10:42 AM
 */
public class GeonetworkH2TestEmbeddedDatabaseConfigurer implements EmbeddedDatabaseConfigurer {

    protected final Log logger = LogFactory.getLog(getClass());
    private final Class<? extends Driver> driverClass;
    private String _mode = "";
    private String _username = "sa";
    private String _password = "p";
    private Optional<Callable<String>> _dbPathLocator = Optional.absent();

    public GeonetworkH2TestEmbeddedDatabaseConfigurer() {
        try {
            this.driverClass = (Class<? extends Driver>) ClassUtils.forName("org.h2.Driver", GeonetworkH2TestEmbeddedDatabaseConfigurer.class.getClassLoader());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public void configureConnectionProperties(ConnectionProperties properties, String databaseName) {
        properties.setDriverClass(this.driverClass);
//        properties.setUrl(String.format("jdbc:h2:mem:%s;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=FALSE", databaseName));
        if (_dbPathLocator.isPresent()) {
            try {
                properties.setUrl(String.format("jdbc:h2:%s;DB_CLOSE_DELAY=-1%s", _dbPathLocator.get().call(), _mode));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            properties.setUrl(String.format("jdbc:h2:mem:%s;DB_CLOSE_DELAY=-1%s", databaseName, _mode));
        }
        properties.setUsername(_username);
        properties.setPassword(_password);
    }

    public void setCompatilityMode(String mode) {
        this._mode = "MODE=" + mode;
    }

    public void shutdown(DataSource dataSource, String databaseName) {
        try {
            Connection connection = dataSource.getConnection();
            Statement stmt = connection.createStatement();
            stmt.execute("SHUTDOWN");
        } catch (SQLException ex) {
            if (logger.isWarnEnabled()) {
                logger.warn("Could not shutdown embedded database", ex);
            }
        }
    }

    public void setDatabasePathLocator(Callable<String> locator) {
        this._dbPathLocator = Optional.of(locator);
    }

    public void setUsername(String username) {
        this._username = username;
    }

    public void setPassword(String password) {
        this._password = password;
    }
}
