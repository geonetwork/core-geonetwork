package org.fao.geonet.domain;

import javax.annotation.Nullable;

/**
 * The system reserved operations. Ids and names are hardcoded and have special meaning in Geonetwork.
 *
 * @author Jesse Eichar
 */
public enum ReservedOperation {
    /**
     * The operation required to view the metadata.
     */
    view(0),
    /**
     * The operation required to download the metadata.
     */
    download(1),
    /**
     * The operation required to edit the metadata.
     */
    editing(2),
    /**
     * The operation required for listeners to be notified of changes about the metadata.
     */
    notify(3),
    /**
     * Identifies a metadata as having a "dynamic" component.
     */
    dynamic(5),
    /**
     * Operation that allows the metadata to be one of the featured metadata.
     */
    featured(6);

    // Not final so Tests can change id
    private int _id;

    private ReservedOperation(int id) {
        this._id = id;
    }

    /**
     * Get the id of the operation.
     *
     * @return the id of the operation.
     */
    public int getId() {
        return _id;
    }

    /**
     * Look up a reserved operation by id.  Returns null if not a reserved operation.
     *
     * @param opId the id of the operation to look up.
     * @return null or the reserved operation.
     */
    public static
    @Nullable
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

    public String getLuceneIndexCode() {
        return "_op" + _id;
    }
}
