package org.fao.geonet.services.extractor.mapping;

import java.util.Map;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import com.fasterxml.jackson.xml.annotate.JacksonXmlProperty;
import com.fasterxml.jackson.xml.annotate.JacksonXmlRootElement;


@JsonIgnoreProperties(ignoreUnknown = true)
@JacksonXmlRootElement(localName = "layer")
public class LayerSpec {

	@JacksonXmlProperty(isAttribute = true)
	private String id = "";
	private InputLayerSpec input;
	private OutputLayerSpec output;
	
	public LayerSpec() {}
	
	@JsonCreator
	public LayerSpec(Map<String,Object> props) {
		id     = (String) props.get("id");
		input  = (InputLayerSpec)  props.get("input");
		output = (OutputLayerSpec) props.get("output");
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public InputLayerSpec getInput() {
		return input;
	}
	public void setInput(InputLayerSpec input) {
		this.input = input;
	}
	public OutputLayerSpec getOutput() {
		return output;
	}
	public void setOutput(OutputLayerSpec output) {
		this.output = output;
	}

	
	public String toString() {
		return String.format("LayerSpec[\n\tid: %s, \n\tInputLayerSpec: %s, \n\tOutputLayerSpec: %s\n]\n", id, input, output);
	}
	

}