package org.fao.geonet.services.extractor.mapping;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import com.fasterxml.jackson.xml.annotate.JacksonXmlRootElement;

@JsonIgnoreProperties(ignoreUnknown = true)
@JacksonXmlRootElement(localName = "extract")
public class ExtractRequestSpec {

	private List<LayerSpec> layers;
	private UserSpec user;

	public ExtractRequestSpec() {}

	@JsonCreator
	public ExtractRequestSpec(Map<String,Object> props) {
		layers = (List<LayerSpec>) props.get("layers");
		user  = (UserSpec) props.get("user");

	}
	
	public List<LayerSpec> getLayers() {
		return layers;
	}

	public void setLayers(List<LayerSpec> layerSpecs) {
		this.layers = layerSpecs;
	}

	public UserSpec getUser() {
		return user;
	}

	public void setUser(UserSpec userSpec) {
		this.user = userSpec;
	}

}
