//=============================================================================
//===	Copyright (C) 2001-2007 Food and Agriculture Organization of the
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

package org.fao.geonet;

import org.fao.geonet.kernel.metadata.StatusActions;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.util.ThreadPool;
import org.springframework.context.ApplicationContext;

public class GeonetContext {
    ApplicationContext springAppContext;
    Class<StatusActions> statusActionsClass;
    boolean readOnly;
    ThreadPool threadPool;

    // ---------------------------------------------------------------------------
    /* package */GeonetContext() {
    }

    // ---------------------------------------------------------------------------

    public ApplicationContext getApplicationContext() {
        return springAppContext;
    }

    public ThreadPool getThreadPool() {
        return threadPool;
    }

    public <T> T getBean(Class<T> beanClass) {
        return springAppContext.getBean(beanClass);
    }

    // ---------------------------------------------------------------------------

    public String getSiteId() {
        return getBean(SettingManager.class).getSiteId();
    }

    public String getSiteName() {
        return getBean(SettingManager.class).getSiteName();
    }

    public Class<StatusActions> getStatusActionsClass() {
        return statusActionsClass;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }
}