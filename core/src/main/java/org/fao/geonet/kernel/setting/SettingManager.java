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

package org.fao.geonet.kernel.setting;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

import jeeves.resources.dbms.Dbms;
import jeeves.utils.Log;

import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.Setting;
import org.fao.geonet.repository.SettingRepository;
import org.jdom.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

//=============================================================================

/**
 * Allows hierarchical management of application settings. Tree structure:
 * 
 * + system | + options | + useProxy | + host | + port | + harvesting
 */
@Component
public class SettingManager {

    @Autowired
    SettingRepository _settingsRepo;

    @Autowired
    EntityManager _entityManager;

    // ---------------------------------------------------------------------------
    // ---
    // --- API methods
    // ---
    // ---------------------------------------------------------------------------

    // ---------------------------------------------------------------------------
    // --- Getters
    // ---------------------------------------------------------------------------

    /**
     * Get the indicated setting and its children up-to the indicated depth
     * 
     * @param path path to the setting that is root of the subtree
     * @param level depth of tree to create
     * @return
     */
    public Element get(String path, int level) {
        Setting s = _settingsRepo.findOneByPath(path);

        return (s == null) ? null : build(s, level);
    }

    // ---------------------------------------------------------------------------

    public String getValue(String path) {
        Setting s = _settingsRepo.findOneByPath(path);

        return (s == null) ? null : s.getValue();
    }

    // ---------------------------------------------------------------------------
    // --- Setters
    // ---------------------------------------------------------------------------

    /**
     * TODO javadoc.
     * 
     * @param dbms
     * @param path
     * @param name
     * @return
     * @throws SQLException
     */
    public boolean setName(Dbms dbms, String path, String name) throws SQLException {
        if (path == null)
            throw new IllegalArgumentException("Path cannot be null");

        Setting updatedSetting = _settingsRepo.findOneByPath(path);
        if (updatedSetting == null) {
            return false;
        } else {
            updatedSetting.setName(name);
            _settingsRepo.save(updatedSetting);

            return true;
        }
    }

    // ---------------------------------------------------------------------------

    /**
     * TODO javadoc.
     * 
     * @param dbms
     * @param path
     * @param value
     * @return
     * @throws SQLException
     */
    public boolean setValue(Dbms dbms, String path, Object value) throws SQLException {
        Map<String, Object> values = new HashMap<String, Object>();
        values.put(path, value);

        return setValues(dbms, values);
    }

    // ---------------------------------------------------------------------------

    /**
     * TODO javadoc.
     * 
     * @param dbms
     * @param values
     * @return
     * @throws SQLException
     */
    public boolean setValues(Dbms dbms, Map<String, Object> values) throws SQLException {
        boolean success = true;

        List<Setting> toSave = new ArrayList<Setting>(values.size());
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            String path = entry.getKey();
            String value = makeString(entry.getValue());

            Setting s = _settingsRepo.findOneByPath(path);

            if (s == null) {
                success = false;
                Log.warning(Geonet.SETTINGS, "Unable to find Settings row for: " + path + ". Check settings table.");
            } else {
                s.setValue(value);
                if (Log.isDebugEnabled(Geonet.SETTINGS)) {
                    Log.debug(Geonet.SETTINGS, "Set path: " + path + ", value: " + value);
                }
                toSave.add(s);
            }
        }

        _settingsRepo.save(toSave);

        return success;
    }

    /**
     * When adding to a newly created node, path must be 'id:...'.
     * 
     * @param dbms
     * @param path
     * @param name
     * @param value
     * @return
     * @throws SQLException
     */
    public String add(Dbms dbms, String path, Object name, Object value) throws SQLException {
        if (name == null)
            throw new IllegalArgumentException("Name cannot be null");

        String sName = makeString(name);
        String sValue = makeString(value);

        // --- first, we look into the tasks list because the 'id' could have been
        // --- added just now

        Setting parent = _settingsRepo.findOneByPath(path);

        if (parent == null)
            return null;

        Setting child = new Setting().setParent(parent).setName(sName).setValue(sValue);

        _settingsRepo.save(child);
        return Integer.toString(child.getId());
    }

    // ---------------------------------------------------------------------------

    /**
     * TODO javadoc.
     * 
     * @param dbms
     * @param path
     * @return
     * @throws SQLException
     */
    public boolean remove(Dbms dbms, String path) throws SQLException {
        Setting s = _settingsRepo.findOneByPath(path);
        if (s == null)
            return false;

        _settingsRepo.delete(s);
        return true;
    }

    // ---------------------------------------------------------------------------

    /**
     * TODO javadoc.
     * 
     * @param dbms
     * @param path
     * @return
     * @throws SQLException
     */
    public boolean removeChildren(Dbms dbms, String path) throws SQLException {
        Setting parent = _settingsRepo.findOneByPath(path);

        if (parent == null)
            return false;

        List<Setting> children = _settingsRepo.findAllChildren(parent.getId());
        for (Setting child : children) {
            remove(dbms, child);
        }

        return true;
    }

    // ---------------------------------------------------------------------------
    // --- Auxiliary methods
    // ---------------------------------------------------------------------------


    public boolean getValueAsBool(String path, boolean defValue) {
        return _settingsRepo.findOneByPath(path).getValueAsBool();
    }

    // ---------------------------------------------------------------------------

    public boolean getValueAsBool(String path) {
        Setting value = _settingsRepo.findOneByPath(path);

        if (value == null)
            return false;

        return value.getValueAsBool();
    }

    // ---------------------------------------------------------------------------

    public Integer getValueAsInt(String path) {
        Setting value = _settingsRepo.findOneByPath(path);

        if (value == null)
            return null;

        return value.getValueAsInt();
    }
    public String getSiteId()   { return getValue("system/site/siteId"); }
    public String getSiteName() { return getValue("system/site/name");   }
    
    // ---------------------------------------------------------------------------
    // ---
    // --- Private methods
    // ---
    // ---------------------------------------------------------------------------

    private String makeString(Object obj) {
        return (obj == null) ? null : obj.toString();
    }

    // ---------------------------------------------------------------------------

    /**
     * Convert a setting and subtree into xml
     * 
     * @param s
     * @param level
     * @return
     */
    private Element build(Setting s, int level) {
        Element el = new Element(s.getName());
        el.setAttribute("id", Integer.toString(s.getId()));

        if (s.getValue() != null) {
            Element value = new Element("value");
            value.setText(s.getValue());

            el.addContent(value);
        }

        if (level != 0) {
            Element children = new Element("children");

            for (Setting child : _settingsRepo.findAllChildren(s.getId()))
                children.addContent(build(child, level - 1));

            if (children.getContentSize() != 0)
                el.addContent(children);
        }

        return el;
    }

    // ---------------------------------------------------------------------------

    private void remove(Dbms dbms, Setting s) throws SQLException {
        _settingsRepo.delete(s);
    }

    public void refresh() {
        _entityManager.getEntityManagerFactory().getCache().evict(Setting.class);
    }
}

// =============================================================================

