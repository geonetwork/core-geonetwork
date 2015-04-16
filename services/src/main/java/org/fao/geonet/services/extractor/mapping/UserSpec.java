package org.fao.geonet.services.extractor.mapping;

import java.util.Map;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import com.fasterxml.jackson.xml.annotate.JacksonXmlRootElement;

@JsonIgnoreProperties(ignoreUnknown = true)
@JacksonXmlRootElement(localName = "user")
public class UserSpec {

	private String lastname;
	private String firstname;
	private String mail;

	public UserSpec () {}

	@JsonCreator
	public UserSpec(Map<String,Object> props) {
		lastname  = (String) props.get("lastname");
		firstname = (String) props.get("firstname");
		mail      = (String) props.get("mail");
	}

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
}
