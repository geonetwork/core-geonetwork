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

    public static final String DOI_GET = "doi-get";
    public static final String RESOURCEID_GET = "resourceid-get";

    /**
     * Query identifier.
     */
    private String id;
    /**
     * XPath to use for the query
     */
    private String xpath;
    /**
     * Optional XPath to use to identify each matching element.
     * This is only relevant if the xpath match nodes.
     */
    private String label;
    /**
     * When retrieving element, any sub children to clean up ?
     */
    private String cleanValues;

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

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setCleanValues(String cleanValues) {
        this.cleanValues = cleanValues;
    }

    public String getCleanValues() {
        return cleanValues;
    }
}
