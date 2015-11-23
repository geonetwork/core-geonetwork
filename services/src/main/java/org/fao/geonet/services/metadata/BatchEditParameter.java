package org.fao.geonet.services.metadata;

import org.apache.commons.lang.StringUtils;

/**
 * Created by francois on 22/10/15.
 */
public class BatchEditParameter {
    public String getXpath() {
        return xpath;
    }

    public void setXpath(String xpath) {
        this.xpath = xpath;
    }

    public String getSearchValue() {
        return searchValue;
    }

    public void setSearchValue(String searchValue) {
        this.searchValue = searchValue;
    }

    public String getReplaceValue() {
        return replaceValue;
    }

    public void setReplaceValue(String replaceValue) {
        this.replaceValue = replaceValue;
    }

    private String xpath;
    private String searchValue;
    private String replaceValue;
    public BatchEditParameter(String xpath, String searchPath,
                              String replaceValue) {
        this.xpath = xpath;
        this.searchValue = searchPath;
        this.replaceValue = replaceValue;
    }

    public String toString() {
        StringBuffer sb = new StringBuffer("Editing xpath ");
        sb.append(this.xpath);
        if (StringUtils.isNotEmpty(this.searchValue)) {
            sb.append(", searching for ");
            sb.append(this.searchValue);
        }
        sb.append(", value is ");
        sb.append(this.replaceValue);
        sb.append(".");
        return sb.toString();
    }
}
