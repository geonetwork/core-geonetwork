package org.fao.geonet.services.metadata.format.groovy;

/**
 * A mode is a way of grouping {@link org.fao.geonet.services.metadata.format.groovy.Handler} and
 * {@link org.fao.geonet.services.metadata.format.groovy.Sorter} so that groups of handlers and sorters can be partitioned.
 *
 * @author Jesse on 10/22/2014.
 */
public class Mode {
    public static final String DEFAULT = "";
    private String id;
    private String fallback;

    public Mode(String modeId, String fallback) {
        this.id = modeId;
        this.fallback = fallback;
    }

    public Mode(String modeId) {
        this(modeId, null);
    }

    /**
     * The id of this mode
     */
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    /**
     * The id of another mode which will be used if the object (Handler or Sorter) was not found in this mode.  This can be null
     * if not fallback is desired.
     */
    public String getFallback() {
        return fallback;
    }

    public void setFallback(String fallback) {
        this.fallback = fallback;
    }
}
