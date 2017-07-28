package org.fao.geonet.services.extractor.mapping;

import java.util.Map;

import org.apache.commons.lang.StringEscapeUtils;
import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import com.fasterxml.jackson.xml.annotate.JacksonXmlProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class InputLayerSpec {

    @JacksonXmlProperty(isAttribute = true)
    private String format = "";
    @JacksonXmlProperty(isAttribute = true)
    private String epsg = "";
    @JacksonXmlProperty(isAttribute = true)
    private String protocol = "";
    @JacksonXmlProperty(isAttribute = true)
    private String linkage = "";
    @JacksonXmlProperty(isAttribute = true)
    private String filter = "";

    public InputLayerSpec() {
    };

    @JsonCreator
    public InputLayerSpec(Map<String, Object> props) {
        format = (String) props.get("format");
        epsg = (String) props.get("epsg");
        protocol = (String) props.get("protocol");
        linkage = (String) props.get("linkage");
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getEpsg() {
        return epsg;
    }

    public void setEpsg(String epsg) {
        this.epsg = epsg;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getLinkage() {
        return linkage;
    }

    public void setLinkage(String linkage) {
        this.linkage = linkage;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public String toString() {
        return String.format("InputLayerSpec[format: %s, epsg: %s, protocol:%s, linkage: %s, filter: %s]", format, epsg,
                protocol, linkage, filter);
    }

    public String asXml() {
        return String.format("      <input format=\"%s\" epsg=\"%s\" protocol=\"%s\" linkage=\"%s\" filter=\"%s\" />\n",
                this.format, this.epsg, this.protocol, this.linkage, StringEscapeUtils.escapeXml(this.filter));
    }
}