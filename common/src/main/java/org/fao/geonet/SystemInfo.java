package org.fao.geonet;

/**
 * Contains information about the system.
 *
 * @author Jesse on 10/21/2014.
 */
public class SystemInfo {
    public static final String STAGE_DEVELOPMENT = "development";
    public static final String STAGE_PRODUCTION = "production";
    private String stagingProfile = STAGE_PRODUCTION;
    private String buildDate;

    public SystemInfo(String stagingProfile, String buildDate) {
        this.stagingProfile = stagingProfile;
        this.buildDate = buildDate;
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

    public boolean isDevMode() {
        return STAGE_DEVELOPMENT.equals(stagingProfile);
    }
}
