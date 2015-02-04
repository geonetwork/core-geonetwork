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

import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.HarvesterSetting;
import org.fao.geonet.repository.HarvesterSettingRepository;
import org.fao.geonet.utils.Log;
import org.jdom.Element;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 * Allows hierarchical management of harvester settings. The harvester settings API has been designed with the following goals:
 * 
 * - speed: all the settings tree is kept into memory
 * 
 * - transactional: changes follow the rules of transactions. The only issue is that changes are not visible until commit. If a thread
 * changes a value and then reads it, the thread gets the old value. Added settings will not be visible and removed ones will still be
 * visible until commit.
 * 
 * - concurrent: many thread can access the settings API at the same time. A read/write lock is used to arbitrate threads
 * 
 * Multiple removes: there are no issues. If thread A removes a subtree S1 and another thread B removes a subtree S2 inside S1, the first
 * thread to commit succeeds while the second always rises a 'cannot serializable exception'. In any commit combination, the settings
 * integrity is maintained.
 * 
 * Tree structure:
 * 
 * + root | +-- harvester1settings | +-- harvester1settings
 * 
 * Harvester settings depend on the harvester's protocol.
 * 
 */
public class HarvesterSettingsManager {

    @Autowired
    private HarvesterSettingRepository _settingsRepo;
    @Autowired
    private SettingManager _settingManager;

    @PersistenceContext
    private EntityManager _entityManager;

    // ---------------------------------------------------------------------------
    // ---
    // --- API methods
    // ---
    // ---------------------------------------------------------------------------

    // ---------------------------------------------------------------------------
    // --- Getters
    // ---------------------------------------------------------------------------

    /**
     * Get the indicated setting and its children up-to the indicated depth.
     * 
     * @param path path to the setting that is root of the subtree
     * @param level depth of tree to create
     * @return
     */
    public Element get(String path, int level) {
        HarvesterSetting s = _settingsRepo.findOneByPath(path);

        return (s == null) ? null : build(s, level);
    }

    // ---------------------------------------------------------------------------

    public String getValue(String path) {
        HarvesterSetting s = _settingsRepo.findOneByPath(path);

        return (s == null) ? null : s.getValue();
    }

    // ---------------------------------------------------------------------------
    // --- Setters
    // ---------------------------------------------------------------------------

    /**
     * TODO javadoc.
     * 
     * @param path
     * @param name
     * @return
     */
    public boolean setName(String path, String name) {
        if (path == null)
            throw new IllegalArgumentException("Path cannot be null");

        HarvesterSetting updatedSetting = _settingsRepo.findOneByPath(path);
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
     * @param path
     * @param value
     * @return
     */
    public boolean setValue(String path, Object value) {
        Map<String, Object> values = new HashMap<String, Object>();
        values.put(path, value);

        return setValues(values);
    }

    // ---------------------------------------------------------------------------

    /**
     * TODO javadoc.
     * 
     * @param values
     * @return
     */
    public boolean setValues(Map<String, Object> values) {
        boolean success = true;

        List<HarvesterSetting> toSave = new ArrayList<HarvesterSetting>(values.size());
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            String path = entry.getKey();
            String value = makeString(entry.getValue());

            HarvesterSetting s = _settingsRepo.findOneByPath(path);

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
     * @param path
     * @param name
     * @param value
     * @return
     */
    public String add(String path, Object name, Object value) {
        if (name == null)
            throw new IllegalArgumentException("Name cannot be null");

        String sName = makeString(name);
        String sValue = makeString(value);

        // --- first, we look into the tasks list because the 'id' could have been
        // --- added just now

        HarvesterSetting parent = _settingsRepo.findOneByPath(path);

        if (parent == null)
            return null;

        HarvesterSetting child = new HarvesterSetting().setParent(parent).setName(sName).setValue(sValue);

        _settingsRepo.save(child);
        return Integer.toString(child.getId());
    }

    // ---------------------------------------------------------------------------

    /**
     * TODO javadoc.
     * 
     * @param path
     * @return
     */
    public boolean remove(String path) {
        HarvesterSetting s = _settingsRepo.findOneByPath(path);
        if (s == null)
            return false;

        _settingsRepo.delete(s);
        return true;
    }

    // ---------------------------------------------------------------------------

    /**
     * TODO javadoc.
     * 
     * @param path
     * @return
     */
    public boolean removeChildren(String path) {
        HarvesterSetting parent = _settingsRepo.findOneByPath(path);

        if (parent == null)
            return false;

        List<HarvesterSetting> children = _settingsRepo.findAllChildren(parent.getId());
        for (HarvesterSetting child : children) {
            remove(child);
        }

        return true;
    }

    // ---------------------------------------------------------------------------
    // --- Auxiliary methods
    // ---------------------------------------------------------------------------


    public boolean getValueAsBool(String path, boolean defValue) {
        HarvesterSetting setting = _settingsRepo.findOneByPath(path);
        if (setting == null) {
            return defValue;
        }
        return setting.getValueAsBool();
    }

    // ---------------------------------------------------------------------------

    public boolean getValueAsBool(String path) {
        HarvesterSetting value = _settingsRepo.findOneByPath(path);

        if (value == null)
            return false;

        return value.getValueAsBool();
    }

    public String getSiteId()   { return _settingManager.getSiteId(); }
    public String getSiteName() { return _settingManager.getSiteName();   }
    
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
    private Element build(HarvesterSetting s, int level) {
        Element el = new Element(s.getName());
        el.setAttribute("id", Integer.toString(s.getId()));

        if (s.getValue() != null) {
            Element value = new Element("value");
            value.setText(s.getValue());

            el.addContent(value);
        }

        if (level != 0) {
            Element children = new Element("children");

            for (HarvesterSetting child : _settingsRepo.findAllChildren(s.getId()))
                children.addContent(build(child, level - 1));

            if (children.getContentSize() != 0)
                el.addContent(children);
        }

        return el;
    }

    // ---------------------------------------------------------------------------

    private void remove(HarvesterSetting s) {
        _settingsRepo.delete(s);
    }

    public void refresh() {
        _entityManager.getEntityManagerFactory().getCache().evict(HarvesterSetting.class);
    }
}