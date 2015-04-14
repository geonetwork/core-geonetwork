package org.fao.geonet.services.extractor.mapping;

import java.util.Map;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import com.fasterxml.jackson.xml.annotate.JacksonXmlProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class OutputLayerSpec {

	@JacksonXmlProperty(isAttribute = true)
	private String format;
	@JacksonXmlProperty(isAttribute = true)
	private String name;
	@JacksonXmlProperty(isAttribute = true)
	private String epsg;
	@JacksonXmlProperty(isAttribute = true)
	private String xmin, ymin, xmax, ymax;
	@JacksonXmlProperty(isAttribute = true)
	private String mercator_lat;

	public OutputLayerSpec() {}
	
	@JsonCreator
	public OutputLayerSpec(Map<String,Object> props) {
		format = (String) props.get("format");
		name = (String) props.get("name");
		epsg = (String) props.get("epsg");
		xmin = (String) props.get("xmin");
		xmax = (String) props.get("xmax");
		ymin = (String) props.get("ymin");
		ymax = (String) props.get("xmax");
		mercator_lat = (String) props.get("mercator_lat");
		
	}
	public String getFormat() {
		return format;
	}
	public void setFormat(String format) {
		this.format = format;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getEpsg() {
		return epsg;
	}
	public void setEpsg(String epsg) {
		this.epsg = epsg;
	}
	public String getXmin() {
		return xmin;
	}
	public void setXmin(String xmin) {
		this.xmin = xmin;
	}
	public String getYmin() {
		return ymin;
	}
	public void setYmin(String ymin) {
		this.ymin = ymin;
	}
	public String getXmax() {
		return xmax;
	}
	public void setXmax(String xmax) {
		this.xmax = xmax;
	}
	public String getYmax() {
		return ymax;
	}
	public void setYmax(String ymax) {
		this.ymax = ymax;
	}
	public String getMercator_lat() {
		return mercator_lat;
	}
	public void setMercator_lat(String mercator_lat) {
		this.mercator_lat = mercator_lat;
	}
	
	public String toString() {
		return String.format("OutputLayerSpec[format: %s, name: %s, epsg: %s, xmin: %s, xmax: %s, ymin: %s, ymax: %s, mercator_lat: %s]",
				format, name, epsg, xmin, xmax, ymin, ymax, mercator_lat);

	}
}