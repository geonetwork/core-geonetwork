//=============================================================================
//===	Copyright (C) 2001-2005 Food and Agriculture Organization of the
//===	United Nations (FAO-UN), United Nations World Food Programme (WFP)
//===	and United Nations Environment Programme (UNEP)
//===
//===	This library is free software; you can redistribute it and/or
//===	modify it under the terms of the GNU Lesser General Public
//===	License as published by the Free Software Foundation; either
//===	version 2.1 of the License, or (at your option) any later version.
//===
//===	This library is distributed in the hope that it will be useful,
//===	but WITHOUT ANY WARRANTY; without even the implied warranty of
//===	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
//===	Lesser General Public License for more details.
//===
//===	You should have received a copy of the GNU Lesser General Public
//===	License along with this library; if not, write to the Free Software
//===	Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
//===
//===	Contact: Jeroen Ticheler - FAO - Viale delle Terme di Caracalla 2,
//===	Rome - Italy. email: GeoNetwork@fao.org
//==============================================================================

package jeeves.server.context;

import jeeves.monitor.MonitorManager;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.fao.geonet.Logger;
import org.fao.geonet.NodeInfo;
import org.fao.geonet.kernel.GeonetworkDataDirectory;
import org.fao.geonet.utils.Log;
import org.springframework.context.ConfigurableApplicationContext;

import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.persistence.EntityManager;

//=============================================================================

/**
 * Contains a minimun context for a job execution (schedule, service etc...)
 */

public class BasicContext implements Logger {

    private final ConfigurableApplicationContext jeevesApplicationContext;
    protected Logger logger = Log.createLogger(Log.JEEVES);
    protected Map<String, Object> htContexts;
    private String baseUrl;
    private EntityManager entityManager;

    //--------------------------------------------------------------------------
    //---
    //--- Constructor
    //---
    //--------------------------------------------------------------------------

    public BasicContext(ConfigurableApplicationContext jeevesApplicationContext, Map<String, Object> contexts, EntityManager entityManager) {

        this.jeevesApplicationContext = jeevesApplicationContext;
        htContexts = Collections.unmodifiableMap(contexts);
        this.entityManager = entityManager;
    }

    //--------------------------------------------------------------------------
    //---
    //--- API methods
    //---
    //--------------------------------------------------------------------------


    public Logger getLogger() {
        return logger;
    }

    //--- read/write objects
    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String name) {
        baseUrl = name;
    }

    //--------------------------------------------------------------------------

    public Path getAppPath() {
        return this.jeevesApplicationContext.getBean(GeonetworkDataDirectory.class).getWebappDir();
    }

    //--------------------------------------------------------------------------

    public Object getHandlerContext(String contextName) {
        return htContexts.get(contextName);
    }

    //--------------------------------------------------------------------------

    public MonitorManager getMonitorManager() {
        return getBean(MonitorManager.class);
    }

    //--------------------------------------------------------------------------

    public ConfigurableApplicationContext getApplicationContext() {
        return jeevesApplicationContext;
    }
    //--------------------------------------------------------------------------

    public
    @Nonnull
    <T> T getBean(Class<T> beanType) {
        return jeevesApplicationContext.getBean(beanType);
    }

    public
    @Nonnull
    <T> T getBean(String name, Class<T> beanType) {
        return jeevesApplicationContext.getBean(name, beanType);
    }


    //--------------------------------------------------------------------------

    public EntityManager getEntityManager() {
        return entityManager;
    }

    //--------------------------------------------------------------------------

    @Override
    public boolean isDebugEnabled() {
        return logger.isDebugEnabled();
    }

    @Override
    public void debug(final String message) {
        logger.debug(message);
    }

    @Override
    public void info(final String message) {
        logger.info(message);
    }

    @Override
    public void warning(final String message) {
        logger.warning(message);
    }

    @Override
    public void error(final String message) {
        logger.error(message);
    }

    @Override
    public void error(Throwable ex) {
        logger.error(ex);
    }

    @Override
    public void fatal(final String message) {
        logger.fatal(message);
    }

    @Override
    public String getModule() {
        return logger.getModule();
    }

    @Override
    public void setAppender(FileAppender fa) {
        logger.setAppender(fa);
    }

    @Override
    public String getFileAppender() {
        return logger.getFileAppender();
    }

    @Override
    public Level getThreshold() {
        return logger.getThreshold();
    }

    /**
     * Return the id of the current node.
     *
     * @return the id of the current node.
     */
    public String getNodeId() {
        return this.jeevesApplicationContext.getBean(NodeInfo.class).getId();
    }
}

//=============================================================================

