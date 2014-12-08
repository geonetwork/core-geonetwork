package org.fao.geonet;

import java.io.Serializable;

/**
 * Encapsulates information about the current node such as the node id.
 *
 * User: Jesse
 * Date: 11/27/13
 * Time: 2:35 PM
 */
public class NodeInfo implements Serializable {
    private String id;
    private boolean defaultNode;
    private boolean readOnly;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean isDefaultNode() {
        return defaultNode;
    }

    public void setDefaultNode(boolean defaultNode) {
        this.defaultNode = defaultNode;
    }

    public void setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
    }

    public boolean isReadOnly() {
        return readOnly;
    }
}
