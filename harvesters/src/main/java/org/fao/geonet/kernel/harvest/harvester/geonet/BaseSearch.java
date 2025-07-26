package org.fao.geonet.kernel.harvest.harvester.geonet;

import org.fao.geonet.Util;
import org.fao.geonet.exceptions.BadParameterEx;
import org.jdom.Element;

public class BaseSearch {
    public int from;
    public int to;
    public String freeText;
    public String title;
    public String abstrac;
    public String keywords;
    public String description;
    public String sourceUuid;

    public BaseSearch() {

    }

    public BaseSearch(Element search) throws BadParameterEx {
        freeText = Util.getParam(search, "freeText", "");
        title = Util.getParam(search, "title", "");
        abstrac = Util.getParam(search, "abstract", "");
        keywords = Util.getParam(search, "keywords", "");

        Element source = search.getChild("source");

        sourceUuid = Util.getParam(source, "uuid", "");

        from = Util.getParam(search, "from", 0);
        to = Util.getParam(search, "to", 0);

        if (from < 0 || to < 0) {
            throw new BadParameterEx("from/to", "must be >= 0");
        }


    }
}
