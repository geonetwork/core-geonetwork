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

package jeeves.server.overrides;

import org.fao.geonet.utils.Log;
import org.jdom.Element;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;

import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

public class SpringPropertyOverrides {

    List<Updater> updaters = new LinkedList<Updater>();
    private Properties properties;

    public SpringPropertyOverrides(List<Element> springOverrides, Properties properties) {
        this.properties = properties;
        for (Element element : springOverrides) {
            Updater updater = create(element);
            this.updaters.add(updater);
        }
    }

    public void onFinishedRefresh(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        for (Updater updater : updaters) {
            if (updater.runOnFinish()) {
                updater.update(beanFactory, properties);
            }
        }
    }

    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        for (Updater updater : updaters) {
            if (!updater.runOnFinish()) {
                updater.update(beanFactory, properties);
            }
        }
    }

    Updater create(Element element) {
        if ("set".equalsIgnoreCase(element.getName()) || "add".equalsIgnoreCase(element.getName())) {
            return PropertyUpdater.create(element);
        } else if ("addInterceptUrl".equalsIgnoreCase(element.getName())) {
            return new AddInterceptUrlUpdater(element);
        } else if ("removeInterceptUrl".equalsIgnoreCase(element.getName())) {
            return new RemoveInterceptUrlUpdater(element);
        } else if ("SetInterceptUrl".equalsIgnoreCase(element.getName())) {
            return new SetInterceptUrlUpdater(element);
        } else {
            throw new IllegalArgumentException(element.getName() + " is not known type of updater");
        }
    }

}
