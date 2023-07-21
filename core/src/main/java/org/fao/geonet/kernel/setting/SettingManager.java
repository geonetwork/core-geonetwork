//=============================================================================
//===	Copyright (C) 2001-2023 Food and Agriculture Organization of the
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

import jeeves.server.context.ServiceContext;
import jeeves.server.sources.http.ServletPathFinder;
import org.apache.commons.lang.StringUtils;
import org.fao.geonet.ApplicationContextHolder;
import org.fao.geonet.NodeInfo;
import org.fao.geonet.constants.Geonet;
import org.fao.geonet.domain.HarvesterSetting;
import org.fao.geonet.domain.Setting;
import org.fao.geonet.domain.SettingDataType;
import org.fao.geonet.domain.Setting_;
import org.fao.geonet.repository.SettingRepository;
import org.fao.geonet.repository.SortUtils;
import org.fao.geonet.repository.SourceRepository;
import org.fao.geonet.utils.Log;
import org.fao.geonet.web.DefaultLanguage;
import org.jasypt.encryption.pbe.StandardPBEStringEncryptor;
import org.jdom.Element;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.Nonnull;
import javax.annotation.PostConstruct;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.ServletContext;
import java.sql.SQLException;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.TimeZone;

import static com.google.common.xml.XmlEscapers.xmlContentEscaper;
import static org.fao.geonet.kernel.setting.Settings.SYSTEM_SITE_NAME_PATH;

/**
 * A convenience class for updating and accessing settings.  One of the primary needs of this class
 * at the moment is to maintain backwards compatibility so not all code and xsl files that make use
 * of the settings need to be modified.
 */
public class SettingManager {

    /**
     * This value stores the original server timezone id had when it was started and  can be used to
     * recover the server timezone in case the value of the setting {@link Settings#SYSTEM_SERVER_TIMEZONE}
     * is set to empty or {@code null}.
     */
    public static final ZoneId DEFAULT_SERVER_TIMEZONE = ZoneId.systemDefault();

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private ServletContext servletContext;

    private ServletPathFinder pathFinder;

    @Autowired
    SettingRepository repo;

    @Autowired
    SourceRepository sourceRepository;

    @Autowired
    StandardPBEStringEncryptor encryptor;

    @Autowired
    DefaultLanguage defaultLanguage;

    @PostConstruct
    private void init() {
        this.pathFinder = new ServletPathFinder(servletContext);
        // Set the default timezone
        String zoneId = StringUtils.defaultIfBlank(getValue(Settings.SYSTEM_SERVER_TIMEZONE), DEFAULT_SERVER_TIMEZONE.getId());
        TimeZone tzFromSettings = TimeZone.getTimeZone(zoneId);
        TimeZone.setDefault(tzFromSettings);
    }

    public List<Setting> getAll() {
        return repo.findAll(SortUtils.createSort(Setting_.name));
    }

    /**
     * Get all settings as xml.
     *
     * @param asTree get the settings as a tree. If true only settings from the system family will
     *               be returned.
     * @return all settings as xml.
     */
    public Element getAllAsXML(boolean asTree) {
        Element env = new Element("settings");
        List<Setting> settings = repo.findAll(SortUtils.createSort(Setting_.name));

        Map<String, Element> pathElements = new HashMap<>();

        for (Setting setting : settings) {
            if (asTree) {
                buildXmlTree(env, pathElements, setting);
            } else {
                Element settingEl = new Element("setting");
                settingEl.setAttribute("name", setting.getName());
                settingEl.setAttribute("position", String.valueOf(setting.getPosition()));
                settingEl.setAttribute("datatype", String.valueOf(setting.getDataType()));
                settingEl.setAttribute("internal", String.valueOf(setting.isInternal()));
                settingEl.setText(setting.getValue());
                env.addContent(settingEl);
            }
        }
        return env;
    }

    private void buildXmlTree(Element env, Map<String, Element> pathElements, Setting setting) {
        String[] segments = setting.getName().split("/");
        Element parent = env;
        StringBuilder path = new StringBuilder();
        for (int i = 0; i < segments.length; i++) {
            String segment = segments[i];
            path.append("/").append(segment);
            Element currentElement = pathElements.get(path.toString());
            if (currentElement == null) {
                try {
                    currentElement = new Element(segment);
                    currentElement.setAttribute("name", path.substring(1));
                    currentElement.setAttribute("position", String.valueOf(setting.getPosition()));
                    if (i == segments.length - 1) {
                        final SettingDataType dataType;
                        if (setting.getDataType() != null) {
                            dataType = setting.getDataType();
                        } else {
                            dataType = SettingDataType.STRING;
                        }
                        currentElement.setAttribute("datatype", String.valueOf(dataType.ordinal()));
                        currentElement.setAttribute("datatypeName", dataType.name());

                        if (setting.getValue() != null)
                            currentElement.setText(xmlContentEscaper().escape(setting.getValue()));
                    } else {
                        currentElement.setText("");
                    }
                    parent.addContent(currentElement);
                    pathElements.put(path.toString(), currentElement);
                } catch (Exception e) {
                    Log.error("Settings table has an illegal setting: " + path, e);
                }
            }

            parent = currentElement;
        }
    }

    /**
     * Return a setting by its key
     *
     * @param path eg. system/site/name
     */
    public String getValue(String path) {
        return getValue(path, false);
    }

    public String getValue(Settings.GNSetting setting) {
        return getValue(setting.getName(), setting.isNullable());
    }

    public String getValue(String path, boolean nullable) {
        if (Log.isDebugEnabled(Geonet.SETTINGS)) {
            Log.trace(Geonet.SETTINGS, "Requested setting with name: " + path);
        }

        Optional<Setting> se = repo.findById(path);
        if (!se.isPresent()) {
            // TODO : When a settings is not available in the settings table
            // we end here. It could be relevant to add a list of default
            // settings and populate the settings table when the settings is
            // missing (due to bad migration for example).
            Log.error(Geonet.SETTINGS, "  Requested setting with name: " + path + "  not found. Add it to the settings table.");
            return null;
        }

        String value = se.get().getValue();

        // This case occurs during the application startup, before the encryptor is initialized:
        // value is null and storedValue has the correct value in this case.
        // Affects OpenApiConfig and SettingManager PostConstruct that retrieve settings during the startup,
        // before the encryptor is initialized.
        // TODO: Improve EncryptorInitializer. For now it depends on GeonetworkDataDirectory
        //  that requires to be initialised in GeoNetwork start method.
        if (!encryptor.isInitialized()) {
            if (!se.get().isEncrypted()) {
                value = se.get().getStoredValue();
            } else {
                throw new IllegalStateException("Encrypted settings can't be accessed before encryptor is initialized");
            }
        }

        if (value == null && ! nullable) {
            Log.warning(Geonet.SETTINGS, "  Requested setting with name: " + path + " but null value found. Check the settings table.");
        }

        return value;
    }

    /**
     * Return a set of values
     *
     * @param keys A list of setting's key to retrieve
     */
    public List<Setting> getSettings(String[] keys) {
        List<Setting> settings = new ArrayList<>();
        for (int i = 0; i < keys.length; i++) {
            String key = keys[i];
            Optional<Setting> se = repo.findById(key);
            if (!se.isPresent()) {
                Log.warning(Geonet.SETTINGS, "  Requested setting with name: " + key + " not found. Add it to the settings table.");
            } else {
                settings.add(se.get());
            }
        }
        return settings;
    }

    /**
     * Return a set of values as XML
     *
     * @param keys A list of setting's key to retrieve
     */
    public Element getValues(String[] keys) {
        Element env = new Element("settings");
        for (int i = 0; i < keys.length; i++) {
            String key = keys[i];
            Optional<Setting> se = repo.findById(key);
            if (!se.isPresent()) {
                Log.error(Geonet.SETTINGS, "  Requested setting with name: " + key + " not found. Add it to the settings table.");
            } else {
                String value = se.get().getValue();
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
     * @param key The setting key
     * @return The setting valueThe setting key
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
     * @param key          The setting key
     * @param defaultValue The default value
     * @return The setting value as boolean
     */
    public boolean getValueAsBool(String key, boolean defaultValue) {
        String value = getValue(key);
        if (value != null) {
            return "y".equalsIgnoreCase(value) || "yes".equalsIgnoreCase(value) || Boolean.parseBoolean(value);
        } else {
            return defaultValue;
        }
    }

    /**
     * Get value of a setting as integer
     *
     * @param key The setting key
     * @return The integer value of the setting or null
     */
    public Integer getValueAsInt(String key) {
        String value = getValue(key);
        if (value == null || value.trim().length() == 0)
            return null;
        return Integer.valueOf(value);
    }

    public Integer getValueAsInt(String key, Integer defaultValue) {
        String value = getValue(key);
        if (value == null || value.trim().length() == 0)
            return defaultValue;
        return Integer.valueOf(value);
    }

    /**
     * Set the value of a Setting entity
     *
     * @param key   the path/name/key of the setting.
     * @param value the new value
     * @return true if the types are correct and the setting is found.
     */
    public boolean setValue(String key, String value) {
        if (Log.isDebugEnabled(Geonet.SETTINGS)) {
            Log.debug(Geonet.SETTINGS, "Setting with name: " + key + ", value: " + value);
        }

        Optional<Setting> settingOpt = repo.findById(key);

        if (!settingOpt.isPresent()) {
            throw new NoSuchElementException("There is no existing setting element with the key: " + key);
        }

        Setting setting = settingOpt.get();

        setting.getDataType().validate(value);
        setting.setValue(value);

        repo.save(setting);

        return true;
    }

    public boolean setValue(Settings.GNSetting setting, String value) {
        return setValue(setting.getName(), value);
    }

    public boolean setValue(Settings.GNSetting setting, boolean value) {
        return setValue(setting.getName(), value);
    }

    /**
     * Set the setting value by key to the boolean value.
     *
     * @param key   the key/path/name of the setting.
     * @param value the new boolean value
     */
    public boolean setValue(String key, boolean value) {
        return setValue(key, String.valueOf(value));
    }

    /**
     * Set a list of settings.
     *
     * @param values The settings to update
     * @return true if the types are correct and the setting is found.
     */
    public final boolean setValues(final Map<String, String> values) {
        boolean success = true;
        for (Map.Entry<String, String> entry : values.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (StringUtils.isNotEmpty(key)) {
                 setValue(key, value);
            }
        }
        return success;
    }

    /**
     * Refreshes current settings manager. This has to be used when updating the Settings table
     * without using this class. For example when using an SQL script.
     */
    public final boolean refresh() throws SQLException {
        entityManager.getEntityManagerFactory().getCache().evict(HarvesterSetting.class);
        return true;
    }

    public final String getSiteId() {
        return getValue(Settings.SYSTEM_SITE_SITE_ID_PATH);
    }

    public final String getSiteName() {
        return getValue(SYSTEM_SITE_NAME_PATH);
    }

    public void setSiteUuid(String siteUuid) {
        setValue(Settings.SYSTEM_SITE_SITE_ID_PATH, siteUuid);
    }

    /**
     * Return complete site URL including language eg. http://localhost:8080/geonetwork/srv/eng
     */
    public
    @Nonnull
    String getSiteURL(@Nonnull ServiceContext context) {
        return getSiteURL(context.getLanguage());
    }

    /**
     * Return complete site URL including language eg. http://localhost:8080/geonetwork/srv/eng
     */
    public
    @Nonnull
    String getSiteURL(String language) {
        if (language == null) {
            language = defaultLanguage.getLanguage();
        }

        return getNodeURL() + language + "/";
    }

    /**
     * Return complete node URL eg. http://localhost:8080/geonetwork/srv/
     */
    public
    @Nonnull
    String getNodeURL() {
        return getBaseURL() + getNodeId() + "/";
    }

    /**
     * Return node id - i.e. srv
     */
    public
    @Nonnull
    String getNodeId() {
        String nodeId = NodeInfo.DEFAULT_NODE;
        try {
            NodeInfo node = ApplicationContextHolder.get().getBean(NodeInfo.class);
            if (node != null) {
                nodeId = node.getId();
            }
        } catch (Exception e) {}
        return  nodeId;
    }
    /**
     * Return complete node URL eg. http://localhost:8080/geonetwork/
     */
    public
    @Nonnull
    String getBaseURL() {
        String baseURL = pathFinder.getBaseUrl();
        return getServerURL() + baseURL + "/";
    }
    /**
     * Return server URL without webapp name eg. http://localhost:8080/
     */
    public
    @Nonnull
    String getServerURL() {
        String protocol = getValue(Settings.SYSTEM_SERVER_PROTOCOL);
        String host = getValue(Settings.SYSTEM_SERVER_HOST);
        Integer port = getServerPort();

        StringBuffer sb = new StringBuffer(protocol + "://");

        sb.append(host);

        if (isPortRequired(protocol, port + "")) {
            sb.append(":");
            sb.append(port);
        }

        return sb.toString();
    }

    public Integer getServerPort() {
        String protocol = getValue(Settings.SYSTEM_SERVER_PROTOCOL);

        // some conditional logic to handle the case where there's no port in the settings
        Integer sitePort;

        Integer configuredPort = getValueAsInt(Settings.SYSTEM_SERVER_PORT, -1);
        if (configuredPort != -1) {
            sitePort = configuredPort;
        } else if (protocol != null && protocol.equalsIgnoreCase(Geonet.HttpProtocol.HTTPS)) {
            sitePort = Geonet.DefaultHttpPort.HTTPS;
        } else {
            sitePort = Geonet.DefaultHttpPort.HTTP;
        }

        return sitePort;
    }

    public static boolean isPortRequired(String protocol, String port) {
        if (Geonet.HttpProtocol.HTTP.equals(protocol) && String.valueOf(Geonet.DefaultHttpPort.HTTP).equals(port)) {
            return false;
        } else if (Geonet.HttpProtocol.HTTPS.equals(protocol) && String.valueOf(Geonet.DefaultHttpPort.HTTPS).equals(port)) {
            return false;
        } else {
            return true;
        }
    }

    private Integer toIntOrNull(String key) {
        try {
            return Integer.parseInt(getValue(key));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
