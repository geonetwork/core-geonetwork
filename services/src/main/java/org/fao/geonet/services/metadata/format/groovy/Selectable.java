package org.fao.geonet.services.metadata.format.groovy;

import groovy.util.slurpersupport.GPathResult;

import java.util.Map;

/**
 * A common super class for objects that are configured from the groovy scripts and that are applied on a by element basis like
 * {@link org.fao.geonet.services.metadata.format.groovy.Sorter} and {@link org.fao.geonet.services.metadata.format.groovy.Handler}.
 *
 * @author Jesse on 10/22/2014.
 */
public abstract class Selectable {
    protected int priority = 0;
    protected String name;

    public Selectable(int priority) {
        this.priority = priority;
    }

    /**
     * Return true if the sorter should sort the children of the element.
     */
    public abstract boolean select(TransformationContext context, GPathResult result);

    /**
     * A name for this handler.  This is strictly for debugging purposes.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * The priority of the handler.  If two (or more) handlers can be applied to a metadata element, the handler with the
     * higher priority will be selected.
     */
    public void setPriority(int priority) {
        this.priority = priority;
    }
    @Override
    public final String toString() {
        return getClass().getSimpleName() + "{" +
               (name != null ? "name='" + name + "\'," : "")  +
               " priority=" + priority +
               extraToString() +
               '}';
    }

    protected abstract String extraToString();

    public void configure(Map<String, Object> properties) {

    }
}
