package org.fao.geonet.domain;

import javax.annotation.Nullable;

/**
 * The system reserved operations. Ids and names are hardcoded and have special meaning in Geonetwork.
 * 
 * @author Jesse Eichar
 */
public enum ReservedOperation {
    view(0), download(1), editing(2), notify(3), dynamic(5), featured(6);

    // Not final so Tests can change id
    private int _id;

    private ReservedOperation(int id) {
        this._id = id;
    }

    public int getId() {
        return _id;
    }

    public static @Nullable
    ReservedOperation lookup(int opId) {
        for (ReservedOperation op : ReservedOperation.values()) {
            if (op._id == opId) {
                return op;
            }
        }
        return null;
    }

    /**
     * Create a transient operation entity with the data of the ReservedOperation
     */
    public Operation getOperationEntity() {
        return new Operation().setId(_id).setName(name());
    }
}
