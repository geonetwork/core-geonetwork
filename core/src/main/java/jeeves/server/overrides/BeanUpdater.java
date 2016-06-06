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
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

import java.util.Properties;

abstract class BeanUpdater implements Updater {
    protected String beanName;

    public void setBeanName(Element element) {
        this.beanName = element.getAttributeValue("bean");
    }


    @Override
    public final void update(ConfigurableListableBeanFactory beanFactory, Properties properties) {
        try {
            BeanDefinition bean = beanFactory.getBeanDefinition(beanName);
            update(beanFactory, properties, bean);
        } catch (NoSuchBeanDefinitionException e) {
            Log.warning(Log.JEEVES, "Unable apply override to bean: " + beanName + " because bean was not found");
        }

    }

    @Override
    public boolean runOnFinish() {
        return false;
    }

    protected abstract void update(ConfigurableListableBeanFactory beanFactory, Properties properties, BeanDefinition bean);

}
