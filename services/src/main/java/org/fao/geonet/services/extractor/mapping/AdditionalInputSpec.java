package org.fao.geonet.services.extractor.mapping;

import java.io.UnsupportedEncodingException;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.xml.annotate.JacksonXmlProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AdditionalInputSpec {

    @JacksonXmlProperty(isAttribute = true)
    @JsonProperty
    private String protocol = "";
    @JacksonXmlProperty(isAttribute = true)
    @JsonProperty
    private String linkage = "";
    @JacksonXmlProperty(isAttribute = true)
    @JsonProperty
    private String params = "";
    @JacksonXmlProperty(isAttribute = true)
    @JsonProperty
    private String identifier = "";
    @JacksonXmlProperty(isAttribute = true)
    @JsonProperty
    private String outputMimeType = "";
    @JacksonXmlProperty(isAttribute = true)
    @JsonProperty
    private String outputIdentifier = "";

    public String getOutputIdentifier() {
        return outputIdentifier;
    }
    public void setOutputIdentifier(String outputIdentifier) {
        this.outputIdentifier = outputIdentifier;
    }
    public String getOutputMimeType() {
        return outputMimeType;
    }
    public void setOutputMimeType(String outputMimeType) {
        this.outputMimeType = outputMimeType;
    }
    public String getIdentifier() {
        return identifier;
    }
    public void setIdentifier(String identifier) {
        this.identifier = identifier;
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
    public String getParams() {
        return params;
    }
    public void setParams(String params) {
        this.params = params;
    }
    public String asXml() throws UnsupportedEncodingException {
        return String.format("      <additionalInput protocol=\"%s\" linkage=\"%s\" params=\"%s\" "
                + "identifier=\"%s\" outputMimeType=\"%s\" outputIdentifier=\"%s\" />\n", this.protocol,
                this.linkage, this.params, this.identifier, this.outputMimeType, this.outputIdentifier);
    }
}
