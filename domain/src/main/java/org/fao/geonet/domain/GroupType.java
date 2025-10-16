package org.fao.geonet.domain;

/**
 * Enum representing different types of groups in the system.
 */
public enum GroupType {
    /**
     * Represents a workspace group type.
     * Workspace groups can create, import, and own metadata records as well as be assigned privileges for specific records.
     */
    Workspace,

    /**
     * Represents a record privilege group type.
     * Record privilege groups can be assigned privileges for specific records but cannot own metadata.
     */
    RecordPrivilege,

    /**
     * Represents a system privilege group type.
     * System privilege groups are for system permissions only; they cannot own metadata or be assigned privileges for specific records.
     */
    SystemPrivilege
}
