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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import jeeves.resources.dbms.Dbms;
import jeeves.server.resources.ProviderManager;
import jeeves.utils.Log;

import org.fao.geonet.constants.Geonet;
import org.jdom.Element;


/**
 * 
 */
public class SettingManager {
    
    private static final int DATATYPE_INT = 1;
    private static final int DATATYPE_BOOLEAN = 2;

    private class SettingEntry {
        private String name;
        private String value;
        private int position;
        private int datatype;
        
        public SettingEntry(String name, String value, int position, int datatype) {
            this.setName(name);
            this.setValue(value);
            this.setPosition(position);
            this.setDatatype(datatype);
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public int getPosition() {
            return position;
        }

        public void setPosition(int position) {
            this.position = position;
        }

        public int getDatatype() {
            return datatype;
        }

        public void setDatatype(int datatype) {
            this.datatype = datatype;
        }
    }
    private Map<String, SettingEntry> settings = new ConcurrentHashMap<String, SettingEntry>();

    public SettingManager(Dbms dbms, ProviderManager provMan) throws SQLException {
        init(dbms);
    }

    /**
     * Init the settings map from the Settings table content
     * 
     * @param dbms
     * @throws SQLException
     */
    private void init(Dbms dbms) throws SQLException {
        @SuppressWarnings("unchecked")
        List<Element> records = dbms.select("SELECT * FROM Settings").getChildren();
        for (Iterator<Element> i = records.iterator(); i.hasNext();) {
            Element elem = (Element) i.next();
            settings.put(elem.getChildText("name"), 
                    new SettingEntry(elem.getChildText("name"),
                            elem.getChildText("value"),
                            Integer.parseInt(elem.getChildText("position")),
                            Integer.parseInt(elem.getChildText("datatype"))
                            )
            );
        }
    }
    /**
     * Return all settings sorted by key
     * 
     * @param asTree true to return an XML tree based on settings
     * key or false to return a flat list.
     * 
     * @return
     */
    public Element getAllAsXML(boolean asTree) {
        Element env = new Element("settings");
        List<String> sortedSetting = new ArrayList<String>(settings.keySet());
        Collections.sort(sortedSetting);
        for(String key : sortedSetting) {
            // settings/site/host
            if (asTree) {
                buildTree(env, key, "");
            } else {
                SettingEntry entry = settings.get(key);
                Element setting = new Element("setting");
                setting.setAttribute("name", key);
                setting.setAttribute("position", entry.getPosition() + "");
                setting.setAttribute("datatype", entry.getDatatype() + "");
                setting.setText(entry.getValue());
                env.addContent(setting);
            }
        }
        return env;
    }
    private void buildTree(Element env, String key, String keyRoot) {
        int separatorIndex = key.indexOf("/");
        // settings
        String start= key.substring(0, separatorIndex);
        // site/host
        String end = key.substring(separatorIndex + 1, key.length());
        // Add settings to env if not exist
        Element child = env.getChild(start);
        // create it if not
        if (child == null) {
            child = new Element(start);
            String id = (keyRoot.equals("") ? key : keyRoot.substring(1, keyRoot.length()) + "/" + key);
            child.setAttribute("position", settings.get(id).getPosition() + "");
            env.addContent(child);
        }
        
        // site/host contains separator, continue
        if (end.contains("/")) {
            buildTree(child, end, keyRoot + "/" + start);
        } else {
            // host is a end node, add value
            String id = keyRoot.substring(1, keyRoot.length()) + "/" + key;
            Element setting = new Element(end);
            setting.setAttribute("name", id);
            setting.setAttribute("position", settings.get(id).getPosition() + "");
            setting.setAttribute("datatype", settings.get(id).getDatatype() + "");
            setting.setText(settings.get(id).getValue());
            child.addContent(setting);
        }
    }
    
    /**
     * Return a setting by its key
     * 
     * @param path eg. system/site/name
     * @return
     */
    public String getValue(String path) {
        if (Log.isDebugEnabled(Geonet.SETTINGS)) {
            Log.debug(Geonet.SETTINGS, "Requested setting with name: " + path);
        }
        SettingEntry se = settings.get(path);
        if (se == null) {
            // TODO : When a settings is not available in the settings table
            // we end here. It could be relevant to add a list of default
            // settings and populate the settings table when the settings is 
            // missing (due to bad migration for example).
            Log.error(Geonet.SETTINGS, "  Requested setting with name: " + path + "  not found. Add it to the settings table.");
            return null;
        }
        String value = se.getValue();
        if (value == null) {
            Log.warning(Geonet.SETTINGS, "  Requested setting with name: " + path + " but null value found. Check the settings table.");
        }
        return value;
    }
    
    /**
     * Return a set of values as XML
     * @param keys  A list of setting's key to retrieve
     * @return
     */
    public Element getValues(String[] keys) {
        Element env = new Element("settings");
        for (int i = 0; i < keys.length; i++) {
            String key = keys[i];
            SettingEntry se = settings.get(key);
            if (se == null) {
                Log.error(Geonet.SETTINGS, "  Requested setting with name: " + key + " not found. Add it to the settings table.");
            } else {
                String value = se.getValue();
                if (value != null) {
                    Element setting = new Element("setting");
                    setting.setAttribute("name", key).setAttribute("value", value);
                    env.addContent(setting);
                }
            }
        }
        return env;
    }

    /**
     * Get value of a setting as boolean
     * 
     * @param key   The setting key
     * @return  The setting valueThe setting key
     */
    public boolean getValueAsBool(String key) {
        String value = getValue(key);
        if (value == null)
            return false;
        return Boolean.parseBoolean(value);
    }

    /**
     * Get value of a setting as boolean
     * 
     * @param key   The setting key
     * @param defaultValue  The default value
     * @return  The setting value as boolean
     */
    public boolean getValueAsBool(String key, boolean defaultValue) {
        String value = getValue(key);
        return (value != null) ? Boolean.parseBoolean(value) : defaultValue;
    }

    /**
     * Get value of a setting as integer
     * @param key   The setting key
     * @return  The integer value of the setting or null
     */
    public Integer getValueAsInt(String key) {
        String value = getValue(key);
        if (value == null || value.trim().length() == 0)
            return null;
        return Integer.valueOf(value);
    }

    /**
     * TODO javadoc.
     * 
     * @param dbms
     * @param path
     * @param value
     * @return
     * @throws SQLException
     */
    public boolean setValue(Dbms dbms, String path, String value) throws Exception {
        Map<String, String> values = new HashMap<String, String>();
        values.put(path, value);
        return setValues(dbms, values);
    }
    public boolean setValue(Dbms dbms, String path, boolean value) throws Exception {
        return setValue(dbms, path, String.valueOf(value));
    }
    
    /**
     * Set a list of settings.
     * 
     * @param dbms
     * @param values    The settings to update
     * @return
     * @throws SQLException
     */
    public boolean setValues(Dbms dbms, Map<String, String> values) throws Exception {
        boolean success = true;
        for (Map.Entry<String, String> entry : values.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            SettingEntry setting = settings.get(key);
            
            if (Log.isDebugEnabled(Geonet.SETTINGS)) {
                Log.debug(Geonet.SETTINGS, "Setting with name: " + key + ", value: " + value);
            }
            try {
                if (setting.getDatatype() == DATATYPE_BOOLEAN) {
                    dbms.execute("UPDATE Settings SET value=? WHERE name=?", Boolean.parseBoolean(value), key);
                } else if (setting.getDatatype() == DATATYPE_INT && !"".equals(value)) {
                    dbms.execute("UPDATE Settings SET value=? WHERE name=?", Integer.parseInt(value), key);
                } else {
                    dbms.execute("UPDATE Settings SET value=? WHERE name=?", value, key);
                }
                setting.setValue(value);
            } catch (Exception e) {
                Log.warning(Geonet.SETTINGS, "Failed to save setting with name: " + key + ", value: " + value + ". Error: " + e.getMessage());
                throw e;
            }
        }
        return success;
    }

    /**
     * Refreshes current settings manager. This has to be used when updating the Settings table without 
     * using this class. For example when
     * using an SQL script.
     */
    public boolean refresh(Dbms dbms) throws SQLException {
        this.init(dbms);
        return true;
    }

    public String getSiteId() {
        return getValue("system/site/siteId");
    }
    
    public String getSiteName() {
        return getValue("system/site/name");
    }
}
