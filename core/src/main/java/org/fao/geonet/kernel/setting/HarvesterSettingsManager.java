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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import jeeves.resources.dbms.Dbms;
import jeeves.server.resources.ProviderManager;
import jeeves.server.resources.ResourceListener;
import jeeves.server.resources.ResourceProvider;
import jeeves.utils.Log;

import org.fao.geonet.constants.Geonet;
import org.jdom.Element;

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
    private Setting root;

    public HarvesterSettingsManager(Dbms dbms, ProviderManager provMan) throws SQLException {
        init(dbms);

        for (ResourceProvider rp : provMan.getProviders()) {
            if (rp.getName().equals(dbms.getURL())) {
                rp.addListener(resList);
            }
        }
    }

    /**
     * Init the settings tree from the HarvesterSettings table content
     * 
     * @param dbms
     * @throws SQLException
     */
    private void init(Dbms dbms) throws SQLException {
        @SuppressWarnings("unchecked")
        List<Element> list = dbms.select("SELECT * FROM HarvesterSettings").getChildren();

        root = new Setting(0, "root", null);
        createSubTree(root, list);
    }
    
    /**
     * TODO javadoc.
     */
    private ResourceListener resList = new ResourceListener() {
        public void beforeClose(Object resource) {
         // do nothing
        }

        public void close(Object resource) {
            flush(resource, true);
        }

        public void abort(Object resource) {
            flush(resource, false);
        }
    };

    /**
     * Get a setting
     * 
     * @param path  Path to the settings
     * @param level
     * @return  the setting value or null
     */
    public Element get(String path, int level) {
        lock.readLock().lock();
        try {
            Setting s = resolve(path);
            return (s == null) ? null : build(s, level);
        } finally {
            lock.readLock().unlock();
        }
    }


    /**
     * Get a setting
     * 
     * @param path  Path to the settings
     * @return  the setting value or null
     */
    public String getValue(String path) {
        lock.readLock().lock();
        try {
            Setting s = resolve(path);
            return (s == null) ? null : s.getValue();
        } finally {
            lock.readLock().unlock();
        }
    }

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

        lock.writeLock().lock();

        try {
            Setting s = resolve(path);

            if (s == null)
                return false;

            dbms.execute("UPDATE HarvesterSettings SET name=? WHERE id=?", name, s.getId());
            tasks.add(Task.getNameChangedTask(dbms, s, name));

            return true;
        } finally {
            lock.writeLock().unlock();
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
        lock.writeLock().lock();

        try {
            boolean success = true;

            for (Map.Entry<String, Object> entry : values.entrySet()) {
                String path = entry.getKey();
                String value = makeString(entry.getValue());
                if (Log.isDebugEnabled(Geonet.SETTINGS))
                    Log.debug(Geonet.SETTINGS, "Set path: " + path + ", value: " + value);

                Setting s = resolve(path);

                if (s == null) {
                    success = false;
                    Log.warning(Geonet.SETTINGS, "Unable to find Settings row for: " + path + ". Check settings table.");
                } else {
                    dbms.execute("UPDATE HarvesterSettings SET value=? WHERE id=?", value, s.getId());
                    tasks.add(Task.getValueChangedTask(dbms, s, value));
                }
            }

            return success;
        } finally {
            lock.writeLock().unlock();
        }
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

        lock.writeLock().lock();

        try {
            // --- first, we look into the tasks list because the 'id' could have been
            // --- added just now

            Setting parent = findAmongAdded(dbms, path);

            // --- if we fail, just do a normal search

            if (parent == null)
                parent = resolve(path);

            if (parent == null)
                return null;

            Setting child = new Setting(getNextSerial(dbms), sName, sValue);

            String query = "INSERT INTO HarvesterSettings(id, parentId, name, value) VALUES(?, ?, ?, ?)";

            dbms.execute(query, child.getId(), parent.getId(), sName, sValue);

            tasks.add(Task.getAddedTask(dbms, parent, child));

            return Integer.toString(child.getId());
        } finally {
            lock.writeLock().unlock();
        }
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
        lock.writeLock().lock();

        try {
            Setting s = resolve(path);

            if (s == null)
                return false;

            remove(dbms, s);

            return true;
        } finally {
            lock.writeLock().unlock();
        }
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
        lock.writeLock().lock();
        try {
            Setting s = resolve(path);
            if (s == null)
                return false;

            for (Setting child : s.getChildren())
                remove(dbms, child);

            return true;
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    /**
     * Refreshes current settings manager. This has to be used when updating the Settings table without using this class. For example when
     * using an SQL script.
     */
    public boolean refresh(Dbms dbms) throws SQLException {
        lock.readLock().lock();
        try {
            this.init(dbms);
            return true;
        } finally {
            lock.readLock().unlock();
        }
    }

    public boolean getValueAsBool(String path, boolean defValue) {
        String value = getValue(path);

        return (value != null) ? value.equals("true") : defValue;
    }

    // ---------------------------------------------------------------------------

    public boolean getValueAsBool(String path) {
        String value = getValue(path);

        if (value == null)
            return false;

        return value.equals("true");
    }

    // ---------------------------------------------------------------------------

    public Integer getValueAsInt(String path) {
        String value = getValue(path);

        if (value == null || value.trim().length() == 0)
            return null;

        return Integer.valueOf(value);
    }

    // ---------------------------------------------------------------------------
    // ---
    // --- Private methods
    // ---
    // ---------------------------------------------------------------------------

    /**
     * Create a tree of {@link Setting}.
     * 
     * @param setting
     * @param records
     */
    private void createSubTree(Setting setting, List<Element> records) {
        for (Iterator<Element> i = records.iterator(); i.hasNext();) {
            Element elem = (Element) i.next();
            String sParId = elem.getChildText("parentid");
            int parId = sParId.equals("") ? 0 : Integer.parseInt(sParId);

            if (setting.getId() == parId) {
                String id = elem.getChildText("id");
                String name = elem.getChildText("name");
                String value = elem.getChildText("value");

                Setting child = new Setting(Integer.parseInt(id), name, value);

                setting.addChild(child);
                i.remove();
            }
        }

        for (Setting child : setting.getChildren())
            createSubTree(child, records);
    }

    // ---------------------------------------------------------------------------

    private String makeString(Object obj) {
        return (obj == null) ? null : obj.toString();
    }

    // ---------------------------------------------------------------------------

    /**
     * TODO javadoc.
     * 
     * @param path
     * @return
     */
    private Setting resolve(String path) {
        StringTokenizer st = new StringTokenizer(path, SEPARATOR);

        Setting s = root;

        while (s != null && st.hasMoreTokens()) {
            String child = st.nextToken();

            if (child.startsWith("id:"))
                s = find(s, Integer.parseInt(child.substring(3)));
            else
                s = s.getChild(child);
        }

        return s;
    }

    // ---------------------------------------------------------------------------

    /**
     * TODO javadoc.
     * 
     * @param s
     * @param id
     * @return
     */
    private Setting find(Setting s, int id) {
        ArrayList<Setting> stack = new ArrayList<Setting>();

        for (Setting child : s.getChildren())
            stack.add(child);

        while (!stack.isEmpty()) {
            s = stack.get(0);
            stack.remove(0);

            if (s.getId() == id)
                return s;

            for (Setting child : s.getChildren())
                stack.add(child);
        }

        return null;
    }

    // ---------------------------------------------------------------------------

    /**
     * TODO javadoc.
     * 
     * @param dbms
     * @param path
     * @return
     */
    private Setting findAmongAdded(Dbms dbms, String path) {
        if (!path.startsWith("id:"))
            return null;

        if (path.indexOf(SEPARATOR) != -1)
            return null;

        int id = Integer.parseInt(path.substring(3));

        for (Task task : tasks) {
            Setting s = task.getAddedSetting(dbms, id);

            if (s != null)
                return s;
        }

        return null;
    }

    // ---------------------------------------------------------------------------

    /**
     * Add children settings until level is reached
     * to a setting.
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

            for (Setting child : s.getChildren())
                children.addContent(build(child, level - 1));

            if (children.getContentSize() != 0)
                el.addContent(children);
        }

        return el;
    }

    private int getNextSerial(Dbms dbms) throws SQLException {
        if (maxSerial == 0) {
            @SuppressWarnings("unchecked")
            List<Element> list = dbms.select("SELECT MAX(id) AS max FROM HarvesterSettings").getChildren();
            String max = list.get(0).getChildText("max");
            maxSerial = Integer.parseInt(max);
        }
        return ++maxSerial;
    }

    private void remove(Dbms dbms, Setting s) throws SQLException {
        for (Setting child : s.getChildren())
            remove(dbms, child);

        dbms.execute("DELETE FROM HarvesterSettings WHERE id=?", s.getId());
        tasks.add(Task.getRemovedTask(dbms, s));
    }

    /**
     * TODO javadoc.
     * 
     * @param resource
     * @param commit
     */
    private void flush(Object resource, boolean commit) {
        lock.writeLock().lock();

        try {
            for (Iterator<Task> i = tasks.iterator(); i.hasNext();) {
                Task task = i.next();

                if (task.matches(resource)) {
                    i.remove();

                    if (commit)
                        task.commit();
                }
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    private static final String SEPARATOR = "/";

    private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    private List<Task> tasks = new ArrayList<Task>();

    private int maxSerial = 0;
}