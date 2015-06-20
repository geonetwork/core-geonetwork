package org.fao.geonet.transifex;

import java.util.Set;

/**
 * Represents the data for uploading to Transifex or that was downloaded from Transifex.
 *
 * @author Jesse on 6/18/2015.
 */
public class TransifexReadyFile {
    public final String resourceId;
    public final String transifexName;
    public final String data;
    public final Set<String> categories;

    public TransifexReadyFile(String resourceId, String transifexName, String data, Set<String> categories) {
        this.transifexName = transifexName;
        this.resourceId = resourceId;
        this.data = data;
        this.categories = categories;
    }
}
