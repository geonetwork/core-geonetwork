package org.fao.geonet.events.group;

import org.fao.geonet.domain.Group;
import org.springframework.context.ApplicationEvent;

/**
 * Event launched when a group is updated on the database
 * 
 * @author delawen
 *
 */
public class GroupUpdated extends ApplicationEvent {

    private static final long serialVersionUID = 523534246220509L;

    private Group g;

    public GroupUpdated(Group g) {
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
