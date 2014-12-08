package org.fao.geonet;

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
}
