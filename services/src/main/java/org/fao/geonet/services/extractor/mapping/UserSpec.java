package org.fao.geonet.services.extractor.mapping;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.xml.annotate.JacksonXmlProperty;
import com.fasterxml.jackson.xml.annotate.JacksonXmlRootElement;

@JsonIgnoreProperties(ignoreUnknown = true)
@JacksonXmlRootElement(localName = "user")
public class UserSpec {

    @JacksonXmlProperty(isAttribute = true)
    @JsonProperty
    private String lastname = "";
    @JacksonXmlProperty(isAttribute = true)
    @JsonProperty
    private String firstname = "";
    @JacksonXmlProperty(isAttribute = true)
    @JsonProperty
    private String mail = "";
    @JacksonXmlProperty(isAttribute = true)
    @JsonProperty
    private String org = "";
    @JacksonXmlProperty(isAttribute = true)
    @JsonProperty
    private String usage = "";

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getOrg() {
        return org;
    }

    public void setOrg(String org) {
        this.org = org;
    }

    public String getUsage() {
        return usage;
    }

    public void setUsage(String usage) {
        this.usage = usage;
    }
}
