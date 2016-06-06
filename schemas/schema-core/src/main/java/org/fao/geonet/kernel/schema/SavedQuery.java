package org.fao.geonet.kernel.schema;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A query to be applied to a metadata record.
 *
 */
public class SavedQuery {
    /**
     * Query identifier.
     */
    private String id;
    /**
     * XPath to use for the query
     */
    private String xpath;

    public String getXpath() {
        return xpath;
    }

    public void setXpath(String xpath) {
        this.xpath = xpath;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
    public List<String> getParameters() {
        List<String> parameters = new ArrayList<>();
        Pattern parameterExtractorPattern = Pattern.compile("\\{\\{(\\w*)\\}\\}");
        Matcher m = parameterExtractorPattern.matcher(this.xpath);
        while (m.find()) {
            parameters.add(m.group(1));
        }
        return parameters;
    }
}
