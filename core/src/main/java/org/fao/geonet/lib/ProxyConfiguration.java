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

package org.fao.geonet.lib;

import org.apache.commons.lang.StringUtils;
import org.fao.geonet.kernel.setting.SettingManager;
import org.fao.geonet.kernel.setting.Settings;

/**
 * Class to abstract the http proxy configuration from Java system properties or GeoNetwork configuration.
 */
public class ProxyConfiguration {
    // HTTP Proxy host
    public static final String HTTP_PROXY_HOST = "http.proxyHost";

    // HTTP Proxy port
    public static final String HTTP_PROXY_PORT = "http.proxyPort";

    // HTTP Proxy username
    public static final String HTTP_PROXY_USERNAME = "http.proxyUser";

    // HTTP Proxy password
    public static final String HTTP_PROXY_PASSWORD = "http.proxyPassword";

    // HTTPS Proxy host
    public static final String HTTPS_PROXY_HOST = "https.proxyHost";

    // HTTPS Proxy port
    public static final String HTTPS_PROXY_PORT = "https.proxyPort";

    // HTTPS Proxy username
    public static final String HTTPS_PROXY_USERNAME = "https.proxyUser";

    // HTTPS Proxy password
    public static final String HTTPS_PROXY_PASSWORD = "https.proxyPassword";

    // HTTP Non-Proxy Hosts
    public static final String HTTP_NON_PROXY_HOSTS = "http.nonProxyHosts";

    private boolean enabled = false;

    private boolean isProxyConfiguredInSystemProperties = false;
    private String host;
    private String port;
    private String username;
    private String password;
    private String ignoreHostList;

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isProxyConfiguredInSystemProperties() {
        return isProxyConfiguredInSystemProperties;
    }

    public String getHost() {
        return host;
    }

    public String getPort() {
        return port;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getIgnoreHostList() {
        return ignoreHostList;
    }

    public ProxyConfiguration(boolean isProxyConfiguredInSystemProperties) {
        this.isProxyConfiguredInSystemProperties = isProxyConfiguredInSystemProperties;
        if (this.isProxyConfiguredInSystemProperties) {
            this.enabled = true;
        }
    }

    public void refresh(SettingManager settingManager) {
        this.enabled = this.isProxyConfiguredInSystemProperties ||
            settingManager.getValueAsBool(Settings.SYSTEM_PROXY_USE, false);

        if (this.enabled) {
            if (this.isProxyConfiguredInSystemProperties) {
                if (StringUtils.isNotBlank(System.getProperty(ProxyConfiguration.HTTPS_PROXY_HOST))) {
                    this.host = System.getProperty(ProxyConfiguration.HTTPS_PROXY_HOST);
                    this.port = System.getProperty(ProxyConfiguration.HTTPS_PROXY_PORT);
                    this.username = System.getProperty(ProxyConfiguration.HTTPS_PROXY_USERNAME, "");
                    this.password = System.getProperty(ProxyConfiguration.HTTPS_PROXY_PASSWORD, "");
                } else {
                    this.host = System.getProperty(ProxyConfiguration.HTTP_PROXY_HOST);
                    this.port = System.getProperty(ProxyConfiguration.HTTP_PROXY_PORT);
                    this.username = System.getProperty(ProxyConfiguration.HTTP_PROXY_USERNAME, "");
                    this.password = System.getProperty(ProxyConfiguration.HTTP_PROXY_PASSWORD, "");
                }

                // Escape characters for regular expression matching
                this.ignoreHostList = System.getProperty(ProxyConfiguration.HTTP_NON_PROXY_HOSTS, "")
                    .replace("\\.", "\\\\.")
                    .replace("\\*", "\\.\\*");

            } else {
                this.host = settingManager.getValue(Settings.SYSTEM_PROXY_HOST);
                this.port = settingManager.getValue(Settings.SYSTEM_PROXY_PORT);
                this.username = settingManager.getValue(Settings.SYSTEM_PROXY_USERNAME);
                this.password = settingManager.getValue(Settings.SYSTEM_PROXY_PASSWORD);
                this.ignoreHostList = settingManager.getValue(Settings.SYSTEM_PROXY_IGNOREHOSTLIST);

            }
        }
    }
}
