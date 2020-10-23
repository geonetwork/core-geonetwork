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

package jeeves.config.springutil;

import com.google.common.io.Files;

import jeeves.server.JeevesEngine;
import jeeves.server.overrides.ConfigurationOverrides;

import org.fao.geonet.NodeInfo;
import org.fao.geonet.domain.User;
import org.fao.geonet.utils.Log;
import org.springframework.data.jpa.repository.JpaRepository;

import java.io.File;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Enumeration;
import java.util.regex.Pattern;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Initializes the ApplicationContexts for each node.
 */
public class JeevesContextLoaderListener implements ServletContextListener {

    private JeevesApplicationContext parentAppContext;

    @Override
    public void contextInitialized(final ServletContextEvent sce) {
        try {
            final Pattern nodeNamePattern = Pattern.compile("[a-zA-Z0-9_\\-]+");
            final ServletContext servletContext = sce.getServletContext();

            String javaVersion = System.getProperty("java.version");
            if(!javaVersion.startsWith("1.8")){
                // GeoNetwork does not support Java 11 at this time
                throw new RuntimeException("Java 8 required, cannot start with \""+javaVersion+"\"");
            }

            File node = new File(servletContext.getRealPath("/WEB-INF/config-node/srv.xml"));

            ConfigurationOverrides overrides = ConfigurationOverrides.DEFAULT;

            String parentConfigFile = "/WEB-INF/config-spring-geonetwork-parent.xml";

            parentAppContext = new JeevesApplicationContext(null, null, parentConfigFile);
            parentAppContext.setServletContext(servletContext);
            parentAppContext.refresh();

            String commonConfigFile = "/WEB-INF/config-spring-geonetwork.xml";
            String nodeId = Files.getNameWithoutExtension(node.getName());
            if (!nodeNamePattern.matcher(nodeId).matches()) {
                throw new IllegalArgumentException(nodeId + " has an illegal name.  Node names must be of the form: [a-zA-Z_\\-]+ ");

            }

            JeevesApplicationContext jeevesAppContext = new JeevesApplicationContext(overrides, parentAppContext,
                "classpath:mapfish-spring-application-context.xml", commonConfigFile, node.toURI().toString());

            jeevesAppContext.setServletContext(servletContext);
            jeevesAppContext.refresh();

            // initialize all JPA Repositories.  This should be done outside of the init
            // because spring-data-jpa first looks up named queries (based on method names) and
            // if the query is not found an exception is thrown.  This exception will set rollback
            // on the transaction if a transaction is active.
            //
            // We want to initialize all repositories here so they are not lazily initialized
            // at random places through out the code where it may be in a transaction.
            jeevesAppContext.getBeansOfType(JpaRepository.class, false, true);

            servletContext.setAttribute(User.NODE_APPLICATION_CONTEXT_KEY, jeevesAppContext);
        } catch (Throwable e) {
            Log.error(Log.JEEVES, "JeevesContextLoaderListener: " + e.getMessage(), e);
            JeevesEngine.handleStartupError(e);
        }
    }

    @Override
    public void contextDestroyed(final ServletContextEvent sce) {
        final ServletContext servletContext = sce.getServletContext();

        /**
         * Destroy all the Spring contexts
         */
        JeevesApplicationContext jeevesAppContext =
            (JeevesApplicationContext) servletContext.getAttribute(User.NODE_APPLICATION_CONTEXT_KEY);
        if (jeevesAppContext != null) {
            jeevesAppContext.destroy();
        }

        Log.info(Log.ENGINE, "Destroying the parent appContext");
        parentAppContext.destroy();

        // De-register JDBC drivers to avoid warnings
        Enumeration<Driver> drivers = DriverManager.getDrivers();
        while (drivers.hasMoreElements()) {
            try {
                DriverManager.deregisterDriver(drivers.nextElement());
            } catch (SQLException e) {
                Log.warning(Log.ENGINE, "Cannot de-register driver", e);
            }
        }
    }
}
