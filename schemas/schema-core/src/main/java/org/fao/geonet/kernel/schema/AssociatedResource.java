package org.fao.geonet.kernel.schema;

/**
 * Created by francois on 8/19/14.
 */
public class AssociatedResource {
    public AssociatedResource(String uuid, String initiativeType, String associationType) {
        this.uuid = uuid;
        this.initiativeType = initiativeType;
        this.associationType = associationType;
    }

    public String getUuid() {
        return uuid;
    }

    public AssociatedResource setUuid(String uuid) {
        this.uuid = uuid;
        return this;
    }

    public String getInitiativeType() {
        if (initiativeType == null) {
            return "";
        } else {
            return initiativeType;
        }
    }

    public AssociatedResource setInitiativeType(String initiativeType) {
        this.initiativeType = initiativeType;
        return this;
    }

    public String getAssociationType() {
        if (associationType == null) {
            return "";
        } else {
            return associationType;
        }
    }

    public AssociatedResource setAssociationType(String associationType) {
        this.associationType = associationType;
        return this;
    }

    private String uuid;
    private String initiativeType;
    private String associationType;
}
