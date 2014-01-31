//=============================================================================
//===	Copyright (C) 2001-2013 Food and Agriculture Organization of the
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

package org.fao.geonet.kernel.setting;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jeeves.server.context.ServiceContext;

import org.apache.commons.lang.NotImplementedException;
import org.fao.geonet.domain.Setting;
import org.fao.geonet.repository.SettingRepository;
import org.jdom.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * A convenience class for updating and accessing settings.  One of the primary needs of this
 * class at the moment is to maintain backwards compatibility so not all code and xsl files
 * that make use of the settings need to be modified.
 */
@Component
public class SpringSettingManager implements ISettingManager {
	
	@Autowired
	private SettingRepository settingrepository;

	public SettingRepository getSettingrepository() {
		return settingrepository;
	}

	public void setSettingrepository(SettingRepository settingrepository) {
		this.settingrepository = settingrepository;
	}

	@Override
	public Element getAllAsXML(boolean asTree) {
        Element env = new Element("settings");
		List<Setting> settings = getAllAsList();
		Map<Object, Element> pathElements = new HashMap<Object, Element>();

        for (Setting setting : settings) {
            if (asTree) {
                buildXmlTree(env, pathElements, setting);
            } else {
                Element settingEl = new Element("setting");
                settingEl.setAttribute("name", setting.getName());
                settingEl.setAttribute("position", String.valueOf(setting.getPosition()));
                settingEl.setAttribute("datatype", String.valueOf(setting.getDataType()));
                settingEl.setText(setting.getValue());
                env.addContent(settingEl);
            }
        }
        return env;
	}

    private void buildXmlTree(Element env, Map<Object, Element> pathElements, Setting setting) {
        String[] segments = setting.getName().split("/");
        Element parent = env;
        StringBuilder path = new StringBuilder();
        for (int i = 0; i < segments.length; i++) {
            String segment = segments[i];
            path.append("/");
            path.append(segment);
            Element currentElement = pathElements.get(path);
            if (currentElement == null) {
                currentElement = new Element(segment);
                currentElement.setAttribute("name", path.substring(1));
                currentElement.setAttribute("position", String.valueOf(setting.getPosition()));
                if (i == segments.length - 1) {
                    currentElement.setAttribute("datatype", String.valueOf(setting.getDataType().ordinal()));
                    currentElement.setAttribute("datatypeName", setting.getDataType().name());
                    currentElement.setText(setting.getValue());
                }
                parent.addContent(currentElement);
                pathElements.put(path.toString(), currentElement);
            }

            parent = currentElement;
        }
    }
    
	@Override
	public List<Setting> getAllAsList() {
		return settingrepository.findAll();
	}

	@Override
	public String getValue(String path) {
		throw new NotImplementedException();
	}

	@Override
	public Element getValues(String[] keys) {
		throw new NotImplementedException();
	}

	@Override
	public boolean getValueAsBool(String key) {
		throw new NotImplementedException();
	}

	@Override
	public boolean getValueAsBool(String key, boolean defaultValue) {
		throw new NotImplementedException();
	}

	@Override
	public Integer getValueAsInt(String key) {
		throw new NotImplementedException();
	}

	@Override
	public boolean setValue(String key, String value) {
		throw new NotImplementedException();
	}

	@Override
	public boolean setValue(String key, boolean value) {
		throw new NotImplementedException();
	}

	@Override
	public boolean setValues(Map<String, String> values) {
		throw new NotImplementedException();
	}

	@Override
	public boolean refresh() throws SQLException {
		throw new NotImplementedException();
	}

	@Override
	public String getSiteId() {
		throw new NotImplementedException();
	}

	@Override
	public String getSiteName() {
		throw new NotImplementedException();
	}

	@Override
	public void setSiteUuid(String siteUuid) {
		throw new NotImplementedException();
	}

	@Override
	public String getSiteURL(ServiceContext context) {
		throw new NotImplementedException();
	}

	@Override
	public boolean getHideWitheldElements() {
		throw new NotImplementedException();
	}

	@Override
	public boolean setHideWitheldElements(boolean value) {
		throw new NotImplementedException();
	}

    
}
