package org.fao.geonet.entitylistener;

import org.fao.geonet.Constants;
import org.fao.geonet.NodeInfo;
import org.fao.geonet.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

/**
 * Obtains the current node id from the application context and sets it on the user object.
 *
 * User: Jesse
 * Date: 11/26/13
 * Time: 12:17 PM
 */
public class UserNodeIdSetter implements GeonetworkEntityListener<User> {
    @Autowired
    private ApplicationContext context;

    @Override
    public Class<User> getEntityClass() {
        return User.class;
    }

    @Override
    public void handleEvent(final PersistentEventType type, final User entity) {
        if (type == PersistentEventType.PostLoad || type == PersistentEventType.PostPersist || type == PersistentEventType.PrePersist) {
            entity.getSecurity().setNodeId(context.getBean(NodeInfo.class).getId());
        }
    }
}
