package org.fao.geonet.domain;

/**
 * The indicator for how the schematron should be interpreted if it satisfies the criteria to run on a metadata.
 *
 * Created by Jesse on 2/6/14.
 */
public enum SchematronRequirement {
    /**
     * Indicates the schematron has to pass for a metadata to be considered "valid".
     */
    REQUIRED,
    /**
     * Indicates that the schematron will be ran during validation but only for purposes of reporting the status of that schematron.
     *
     * The schematron can fail and still be considered valid.
     */
    REPORT_ONLY,
    /**
     * Indicates the schematron is disabled and will not be shown in any reports and will be ignored during validation
     */
    DISABLED;


    public SchematronRequirement highestRequirement(SchematronRequirement requirement) {
        if (requirement.ordinal() < ordinal()) {
            return requirement;
        }
        return this;
    }
}
