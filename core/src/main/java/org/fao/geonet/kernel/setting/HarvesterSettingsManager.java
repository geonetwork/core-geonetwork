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

import org.apache.commons.collections.CollectionUtils;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.HarvesterSetting;
import org.fao.geonet.repository.HarvesterSettingRepository;
import org.fao.geonet.utils.Log;
import org.jdom.Element;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

/**
 * Allows hierarchical management of harvester settings. The harvester settings API has been
 * designed with the following goals:
 *
 * - speed: all the settings tree is kept into memory
 *
 * - transactional: changes follow the rules of transactions. The only issue is that changes are not
 * visible until commit. If a thread changes a value and then reads it, the thread gets the old
 * value. Added settings will not be visible and removed ones will still be visible until commit.
 *
 * - concurrent: many thread can access the settings API at the same time. A read/write lock is used
 * to arbitrate threads
 *
 * Multiple removes: there are no issues. If thread A removes a subtree S1 and another thread B
 * removes a subtree S2 inside S1, the first thread to commit succeeds while the second always rises
 * a 'cannot serializable exception'. In any commit combination, the settings integrity is
 * maintained.
 *
 * Tree structure:
 *
 * + root | +-- harvester1settings | +-- harvester1settings
 *
 * Harvester settings depend on the harvester's protocol.
 */
public class HarvesterSettingsManager {

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
     * Get settings and with only names.
     *
     * @param names path to the setting that is root of the subtree
     */
    public Element getList(List<String> names) {
        HarvesterSettingRepository settingsRepo = ApplicationContextHolder.get().getBean(HarvesterSettingRepository.class);

        List<HarvesterSetting> s = settingsRepo.findAllByNames(names);

        Element el = null;
        if (s != null) {
            HarvesterSetting r = null;
            Map<Integer, List<HarvesterSetting>> mapSettings = new HashMap<Integer, List<HarvesterSetting>>();
            List<HarvesterSetting> settings = null;
            // create Map where keys are idParent
            // this is avoid to multiple call in base
            for (HarvesterSetting h : s) {
                settings = new ArrayList<HarvesterSetting>();
                if (h.getParent() == null) {
                    // root found, then create it
                    r = h;
                } else {
                    if (mapSettings.containsKey(h.getParent().getId())) {
                        // get list
                        settings = mapSettings.get(h.getParent().getId());
                    }
                    settings.add(h);
                }
                if (CollectionUtils.isNotEmpty(settings)) {
                    mapSettings.put(h.getParent().getId(), settings);
                }
            }

            // construct the element from map
            el = buildFromMap(r, mapSettings);
        }

        return el;
    }

    /**
     * Get the indicated setting and its children up-to the indicated depth.
     *
     * @param path  path to the setting that is root of the subtree
     * @param level depth of tree to create
     */
    public Element get(String path, int level) {
        HarvesterSettingRepository settingsRepo = ApplicationContextHolder.get().getBean(HarvesterSettingRepository.class);

        HarvesterSetting s = settingsRepo.findOneByPath(path);

        return (s == null) ? null : build(s, level);
    }

    // ---------------------------------------------------------------------------

    public String getValue(String path) {
        HarvesterSettingRepository settingsRepo = ApplicationContextHolder.get().getBean(HarvesterSettingRepository.class);
        HarvesterSetting s = settingsRepo.findOneByPath(path);

        return (s == null) ? null : s.getValue();
    }

    // ---------------------------------------------------------------------------
    // --- Setters
    // ---------------------------------------------------------------------------

    /**
     * TODO javadoc.
     */
    public boolean setName(String path, String name) {
        HarvesterSettingRepository settingsRepo = ApplicationContextHolder.get().getBean(HarvesterSettingRepository.class);

        if (path == null)
            throw new IllegalArgumentException("Path cannot be null");

        HarvesterSetting updatedSetting = settingsRepo.findOneByPath(path);
        if (updatedSetting == null) {
            return false;
        } else {
            updatedSetting.setName(name);
            settingsRepo.save(updatedSetting);

            return true;
        }
    }

    // ---------------------------------------------------------------------------

    /**
     * TODO javadoc.
     */
    public boolean setValue(String path, Object value) {
        Map<String, Object> values = new HashMap<String, Object>();
        values.put(path, value);

        return setValues(values);
    }

    // ---------------------------------------------------------------------------

    /**
     * TODO javadoc.
     */
    public boolean setValues(Map<String, Object> values) {
        HarvesterSettingRepository settingsRepo = ApplicationContextHolder.get().getBean(HarvesterSettingRepository.class);

        boolean success = true;

        List<HarvesterSetting> toSave = new ArrayList<HarvesterSetting>(values.size());
        for (Map.Entry<String, Object> entry : values.entrySet()) {
            String path = entry.getKey();
            String value = makeString(entry.getValue());

            HarvesterSetting s = settingsRepo.findOneByPath(path);

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

        settingsRepo.saveAll(toSave);

        return success;
    }

    /**
     * When adding to a newly created node, path must be 'id:...'.
     */
    public String add(String path, Object name, Object value) {
        return add(path, name, value, false);
    }

    public String add(String path, Object name, Object value, boolean encrypted) {

        if (name == null)
            throw new IllegalArgumentException("Name cannot be null");

        String sName = makeString(name);
        String sValue = makeString(value);

        // --- first, we look into the tasks list because the 'id' could have been
        // --- added just now

        HarvesterSettingRepository settingsRepo = ApplicationContextHolder.get().getBean(HarvesterSettingRepository.class);
        HarvesterSetting parent = settingsRepo.findOneByPath(path);

        if (parent == null)
            return null;

        HarvesterSetting child = new HarvesterSetting().setParent(parent)
            .setName(sName).setValue(sValue).setEncrypted(encrypted);

        settingsRepo.save(child);
        return Integer.toString(child.getId());
    }

    // ---------------------------------------------------------------------------

    /**
     * TODO javadoc.
     */
    public boolean remove(String path) {
        HarvesterSettingRepository settingsRepo = ApplicationContextHolder.get().getBean(HarvesterSettingRepository.class);

        HarvesterSetting s = settingsRepo.findOneByPath(path);
        if (s == null)
            return false;

        //First we have to remove all children
        removeChildren(s.getId());

        settingsRepo.delete(s);
        settingsRepo.flush();
        return true;
    }

    // ---------------------------------------------------------------------------

    /**
     * TODO javadoc.
     */
    public boolean removeChildren(String path) {
        HarvesterSettingRepository settingsRepo = ApplicationContextHolder.get().getBean(HarvesterSettingRepository.class);

        HarvesterSetting parent = settingsRepo.findOneByPath(path);

        if (parent == null)
            return false;

        return removeChildren(parent.getId());
    }

    /**
     * Remove  all children recursively with parent id as parent. Useful because no cascading is applied.
        * @param parent
        * @return
     */
    private boolean removeChildren(Integer parent) {
        HarvesterSettingRepository settingsRepo = ApplicationContextHolder.get().getBean(HarvesterSettingRepository.class);

        List<HarvesterSetting> children = settingsRepo.findAllChildren(parent);
        for (HarvesterSetting child : children) {
            //This should be recursive
            removeChildren(child.getId());

            remove(child);
        }

        return true;
    }

    // ---------------------------------------------------------------------------
    // --- Auxiliary methods
    // ---------------------------------------------------------------------------


    public boolean getValueAsBool(String path, boolean defValue) {
        HarvesterSettingRepository settingsRepo = ApplicationContextHolder.get().getBean(HarvesterSettingRepository.class);

        HarvesterSetting setting = settingsRepo.findOneByPath(path);
        if (setting == null) {
            return defValue;
        }
        return setting.getValueAsBool();
    }

    // ---------------------------------------------------------------------------

    public boolean getValueAsBool(String path) {
        HarvesterSettingRepository settingsRepo = ApplicationContextHolder.get().getBean(HarvesterSettingRepository.class);

        HarvesterSetting value = settingsRepo.findOneByPath(path);

        if (value == null)
            return false;

        return value.getValueAsBool();
    }

    public String getSiteId() {
        SettingManager _settingManager = ApplicationContextHolder.get().getBean(SettingManager.class);
        return _settingManager.getSiteId();
    }

    public String getSiteName() {
        SettingManager _settingManager = ApplicationContextHolder.get().getBean(SettingManager.class);
        return _settingManager.getSiteName();
    }

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
     * Create recursively the tree of harvesterSettings
     */
    private Element buildFromMap(HarvesterSetting s, Map<Integer, List<HarvesterSetting>> mapSettings) {
        if (s == null) {
            return null;
        }

        // construct tree from HashMap and begin with root found
        Element el = new Element(s.getName());
        el.setAttribute("id", Integer.toString(s.getId()));
        if (s.getValue() != null) {
            Element value = new Element("value");
            value.setText(s.getValue());
            el.addContent(value);
        }

        List<HarvesterSetting> childrenSettings = (List<HarvesterSetting>) mapSettings.get(s.getId());

        if (childrenSettings != null) {
            Element children = new Element("children");
            for (HarvesterSetting childSetting : childrenSettings) {
                // get children and add to element
                children.addContent(buildFromMap(childSetting, mapSettings));
            }
            if (children.getContentSize() != 0)
                el.addContent(children);
        }

        return el;
    }

    /**
     * Convert a setting and subtree into xml
     */
    private Element build(HarvesterSetting s, int level) {
        HarvesterSettingRepository settingsRepo = ApplicationContextHolder.get().getBean(HarvesterSettingRepository.class);

        Element el = new Element(s.getName());
        el.setAttribute("id", Integer.toString(s.getId()));

        if (s.getValue() != null) {
            Element value = new Element("value");
            value.setText(s.getValue());

            el.addContent(value);
        }

        if (level != 0) {
            Element children = new Element("children");

            // get children in base
            List<HarvesterSetting> childrenHarvestSettings = settingsRepo.findAllChildren(s.getId());
            // add children recursively
            for (HarvesterSetting child : childrenHarvestSettings) {
//                fromList.remove(child);
                children.addContent(build(child, level - 1));
            }

            if (children.getContentSize() != 0)
                el.addContent(children);
        }

        return el;
    }

    // ---------------------------------------------------------------------------

    private void remove(HarvesterSetting s) {
        HarvesterSettingRepository settingsRepo = ApplicationContextHolder.get().getBean(HarvesterSettingRepository.class);

        settingsRepo.delete(s);
    }

    public void refresh() {
        _entityManager.getEntityManagerFactory().getCache().evict(HarvesterSetting.class);
    }
}
