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

package org.fao.geonet;

import org.jdom.Element;

import java.util.Arrays;

/**
 * Contains information about the system.
 *
 * @author Jesse on 10/21/2014.
 */
public class SystemInfo {
    public static final String STAGE_TESTING = "testing";
    public static final String STAGE_DEVELOPMENT = "development";
    public static final String STAGE_PRODUCTION = "production";
    private String stagingProfile = STAGE_PRODUCTION;
    private String buildDate;
    private String version;
    private String subVersion;
    private String buildOsInfo;
    private String buildJavaVersion;
    private String buildJavaVendor;

    public static SystemInfo createForTesting(String stagingProfile) {
        return new SystemInfo(stagingProfile, "testing", "3.0.0", "SNAPSHOT", "testing", "testing", "testing");
    }

    public SystemInfo(String stagingProfile, String buildDate, String version, String subVersion,
                      String buildOsInfo, String buildJavaVersion, String buildJavaVendor) {
        this.stagingProfile = stagingProfile;
        this.buildDate = buildDate;
        this.version = version;
        this.subVersion = subVersion;
        this.buildOsInfo = buildOsInfo;
        this.buildJavaVersion = buildJavaVersion;
        this.buildJavaVendor = buildJavaVendor;
    }

    /**
     * A value indicating if the server is in development mode or production mode or...
     */
    public String getStagingProfile() {
        return stagingProfile;
    }

    public void setStagingProfile(String stagingProfile) {
        this.stagingProfile = stagingProfile;
    }

    /**
     * The date this build was built on.
     */
    public String getBuildDate() {
        return buildDate;
    }

    /**
     * Return part of the version after -
     */
    public String getSubVersion() {
        return subVersion;
    }

    /**
     * Return the part of the version before the -
     */
    public String getVersion() {
        return version;
    }

    /**
     * Return the os information of the machine that the web app was built on.
     */
    public String getBuildOsInfo() {
        return buildOsInfo;
    }
    /**
     * Return the version of java used to build the web app.
     */
    public String getBuildJavaVersion() {
        return this.buildJavaVersion;
    }
    /**
     * Return the jvm vendor of java used to build the web app.
     */
    public String getBuildJavaVendor() {
        return this.buildJavaVendor;
    }

    /**
     * Return true if the current stagingProfile is {@link #STAGE_DEVELOPMENT}
     */
    public boolean isDevMode() {
        return STAGE_DEVELOPMENT.equals(stagingProfile) || STAGE_TESTING.equals(stagingProfile);
    }

    public Element toXml() {
        return new Element("systemInfo").addContent(Arrays.asList(
                new Element("stagingProfile").setText(this.stagingProfile),
                new Element("buildOsInfo").setText(this.buildOsInfo),
                new Element("buildJavaVendor").setText(this.buildJavaVendor),
                new Element("buildJavaVersion").setText(this.buildJavaVersion),
                new Element("buildDate").setText(this.buildDate)
        ));
    }

    public static SystemInfo getInfo() {
        return getInfo(null);
    }
    public static SystemInfo getInfo(SystemInfo defaultInfo) {
        SystemInfo actualInfo = defaultInfo;
        if (actualInfo == null && ApplicationContextHolder.get() != null) {
            actualInfo = ApplicationContextHolder.get().getBean(SystemInfo.class);
        }


        return actualInfo;
    }
}
