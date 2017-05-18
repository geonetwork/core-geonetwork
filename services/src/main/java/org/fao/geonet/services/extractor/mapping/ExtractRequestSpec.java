package org.fao.geonet.services.extractor.mapping;

import java.util.List;
import java.util.Map;

import org.codehaus.jackson.annotate.JsonCreator;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.xml.annotate.JacksonXmlElementWrapper;
import com.fasterxml.jackson.xml.annotate.JacksonXmlRootElement;

@JsonIgnoreProperties(ignoreUnknown = true)
@JacksonXmlRootElement(localName = "extract")
public class ExtractRequestSpec {

	@JsonProperty
	@JacksonXmlElementWrapper
	private List<LayerSpec> layers;
	@JsonProperty
	private UserSpec user;

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
