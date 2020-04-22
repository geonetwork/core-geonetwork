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

import jeeves.server.sources.http.ServletPathFinder;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.XmlWebApplicationContext;

import java.io.IOException;
import java.nio.file.Path;

public class JeevesApplicationContext extends XmlWebApplicationContext {

    public JeevesApplicationContext(ApplicationContext parent, String... configLocations) {
        if (configLocations == null || configLocations.length == 0) {
            throw new IllegalArgumentException("No config locations were specified.  There must be at least one");
        }
        setParent(parent);
        setConfigLocations(configLocations);
    }


    @Override
    protected void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) {
        super.postProcessBeanFactory(beanFactory);
    }

    @Override
    protected void finishRefresh() {
        super.finishRefresh();
    }

    /**
     * Get the path to the webapplication directory.
     *
     * This method is protected so tests can provide custom implementations.
     */
    protected Path getAppPath() {
        final ServletPathFinder pathFinder = new ServletPathFinder(getServletContext());
        return pathFinder.getAppPath();
    }

    @Override
    protected void loadBeanDefinitions(XmlBeanDefinitionReader reader) throws IOException {
        reader.setValidating(false);
        super.loadBeanDefinitions(reader);
    }
}
