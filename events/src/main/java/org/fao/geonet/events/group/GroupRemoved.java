package org.fao.geonet.events.group;

import org.fao.geonet.domain.Group;
import org.springframework.context.ApplicationEvent;

public class GroupRemoved extends ApplicationEvent {

    private static final long serialVersionUID = 523534246220509L;

    private Group g;

    public GroupRemoved(Group g) {
        super(g);
        this.g = g;
    }

    /**
     * @return the g
     */
    public Group getGroup() {
        return g;
    }
    
}
