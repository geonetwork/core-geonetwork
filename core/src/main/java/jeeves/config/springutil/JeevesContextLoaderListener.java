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

import java.io.File;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Pattern;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.fao.geonet.NodeInfo;
import org.fao.geonet.domain.User;
import org.fao.geonet.utils.Log;
import org.springframework.data.jpa.repository.JpaRepository;

import com.google.common.io.Files;

import jeeves.server.JeevesEngine;
import jeeves.server.overrides.ConfigurationOverrides;

/**
 * Initializes the ApplicationContexts for each node.
 */
public class JeevesContextLoaderListener implements ServletContextListener {


    private JeevesApplicationContext parentAppContext;

    public static String[] getNodeIds(final ServletContext servletContext) {
        final File[] nodeConfigurationFiles = getNodeConfigurationFiles(servletContext);
        String[] ids = new String[nodeConfigurationFiles.length];

        for (int i = 0; i < nodeConfigurationFiles.length; i++) {
            File file = nodeConfigurationFiles[i];
            ids[i] = Files.getNameWithoutExtension(file.getName());
        }
        Arrays.sort(ids);
        return ids;
    }

    /**
     * Returns the node configuration files
        * @param servletContext
        * @return
     */
    private static File[] getNodeConfigurationFiles(ServletContext servletContext) {

        // Check if an external node config directory is defined via JNDI
        String configNodeFolderLocation = null;
        try {
            Context ctx = new InitialContext();
            Context envCtx = (Context)ctx.lookup("java:comp/env");
            configNodeFolderLocation = (String) envCtx.lookup("configNodeFolderLocation");
        } catch (NamingException e) {
            Log.info(Log.ENGINE, "No config folder url defined as JNDI string");
        }

        File nodeConfigDir = null;
        if (configNodeFolderLocation == null || configNodeFolderLocation.equals("")) {
            nodeConfigDir = new File(servletContext.getRealPath("/WEB-INF/config-node"));
        } else {
            nodeConfigDir = new File(configNodeFolderLocation);
        }

        // List Files in the directory (Must not be empty)
        File[] files = nodeConfigDir.listFiles();
        if (files == null || files.length==0) {
            throw new IllegalStateException("No node configuration file found in: " + nodeConfigDir);
        }

        // Removes folders from the list of node files
        List<File> fileAsListToReturn = removeDirectoryFromList(files);

        return fileAsListToReturn.toArray(new File[fileAsListToReturn.size()]);
    }

    /**
     *  Removes folders from the list of node files
        * @param files
        * @return
     */
    private static List<File> removeDirectoryFromList(File[] files) {
        List<File> fileAsList = Arrays.asList(files);
        Iterator<File> fileAsIterable = fileAsList.iterator();
        List<File> fileAsListToReturn = new ArrayList<>();

        while(fileAsIterable.hasNext()) {
            File f = fileAsIterable.next();
            if(!f.isDirectory()) {
                fileAsListToReturn.add(f);
            }
        }
        return fileAsListToReturn;
    }

    /**
     *  Removes element from List of Files
        * @param original
        * @param element
        * @return
     */
    private static File[] removeElement(File[] original, int element){
        File[] n = new File[original.length - 1];
        System.arraycopy(original, 0, n, 0, element );
        System.arraycopy(original, element+1, n, element, original.length - element-1);
        return n;
    }

    @Override
    public void contextInitialized(final ServletContextEvent sce) {
        try {
            final Pattern nodeNamePattern = Pattern.compile("[a-zA-Z0-9_\\-]+");
            final ServletContext servletContext = sce.getServletContext();

            JeevesApplicationContext defaultContext = null;
            File[] nodes = getNodeConfigurationFiles(servletContext);

            if (nodes.length == 0) {
                throw new IllegalArgumentException("Need at least one node defined");
            }

            ConfigurationOverrides overrides = ConfigurationOverrides.DEFAULT;

            String parentConfigFile = "/WEB-INF/config-spring-geonetwork-parent.xml";

            parentAppContext = new JeevesApplicationContext(null, null, parentConfigFile);
            parentAppContext.setServletContext(servletContext);
            parentAppContext.refresh();

            String commonConfigFile = "/WEB-INF/config-spring-geonetwork.xml";
            for (File node : nodes) {
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

                servletContext.setAttribute(User.NODE_APPLICATION_CONTEXT_KEY + nodeId, jeevesAppContext);

                // check if the context is the default context
                NodeInfo nodeInfo = jeevesAppContext.getBean(NodeInfo.class);
                nodeInfo.setId(nodeId);

                boolean isDefault = nodeInfo.isDefaultNode();

                if (isDefault) {
                    if (defaultContext != null) {
                        throw new IllegalArgumentException("Two nodes where defined as the default.  This is not acceptable.");
                    }
                    defaultContext = jeevesAppContext;

                    servletContext.setAttribute(User.NODE_APPLICATION_CONTEXT_KEY, jeevesAppContext);
                }
            }

            if (defaultContext == null) {
                throw new IllegalArgumentException("There are no default contexts defined");
            }
        } catch (Throwable e) {
            e.printStackTrace();
            JeevesEngine.handleStartupError(e);
        }
    }

    @Override
    public void contextDestroyed(final ServletContextEvent sce) {
        final ServletContext servletContext = sce.getServletContext();

        /**
         * Destroy all the Spring contexts
         */
        for (String node : getNodeIds(sce.getServletContext())) {
            if (!node.trim().isEmpty()) {
                Log.info(Log.ENGINE, "Destroying the appContext for " + node);
                JeevesApplicationContext jeevesAppContext = (JeevesApplicationContext) servletContext.getAttribute(
                    User.NODE_APPLICATION_CONTEXT_KEY + node.trim());
                jeevesAppContext.destroy();
            }
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
