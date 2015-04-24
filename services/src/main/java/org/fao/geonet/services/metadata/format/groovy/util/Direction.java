package org.fao.geonet.services.metadata.format.groovy.util;

/**
 * Represents the direction of a relationship.
 *
 * @author Jesse on 4/23/2015.
 */
public enum Direction {
    /**
     * Indicates that the metadata is the parent of a relationship.  For most relationships the child metadata contains the
     * link to the parent.  For example parentIdentifier, aggregationInfo and lineage links are in the child.  Other types
     * like coupledResource (in services) then the parent contains the relationship.
     */
    PARENT,
    /**
     * Indicates that the metadata is the child of a relationship.  For most relationships the child metadata contains the
     * link to the parent.  For example parentIdentifier, aggregationInfo and lineage links are in the child.  Other types
     * like coupledResource (in services) then the parent contains the relationship.
     */
    CHILD,
    /**
     * Indicates that the metadata is the sibling of the related metadata.  In otherwords, both metadata are part of the same
     * group of related metadata, both are children metadata and part of the same group/relationship.
     */
    SIBLING;

}
