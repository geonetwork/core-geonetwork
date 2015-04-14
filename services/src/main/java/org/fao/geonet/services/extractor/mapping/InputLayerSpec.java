package org.fao.geonet.services.extractor.mapping;

import java.util.Map;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class InputLayerSpec {
	
	private String format;
	private String epsg;
	private String protocol;
	private String linkage;

	public InputLayerSpec() {};
	
	@JsonCreator
	public InputLayerSpec(Map<String,Object> props) {
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

	
	public String toString() {
		return String.format("InputLayerSpec[format: %s, epsg: %s, protocol:%s, linkage: %s]",
				format, epsg, protocol, linkage);
	}
}