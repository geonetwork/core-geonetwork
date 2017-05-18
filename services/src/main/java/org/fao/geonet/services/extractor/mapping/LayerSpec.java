package org.fao.geonet.services.extractor.mapping;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.xml.annotate.JacksonXmlProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LayerSpec {

    @JacksonXmlProperty(isAttribute = true)
    private String id = "";
    @JsonProperty
    private InputLayerSpec input;
    @JsonProperty
    private OutputLayerSpec output;
    @JsonProperty
    private List<AdditionalInputSpec> additionalInput = new ArrayList<AdditionalInputSpec>();

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

    public List<AdditionalInputSpec> getAdditionalInput() {
        return additionalInput;
    }

    public void setAdditionalInput(List<AdditionalInputSpec> additionalInputs) {
        this.additionalInput = additionalInputs;
    }

    public String toString() {
        return String.format("LayerSpec[\n\tid: %s, \n\tInputLayerSpec: %s, \n\tOutputLayerSpec: %s\n]\n", id, input,
                output);
    }

    public String asXml() throws UnsupportedEncodingException {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("    <layer id=\"%s\">\n", this.id));
        sb.append(String.format("%s", input.asXml()));
        sb.append(String.format("%s", output.asXml()));
        for (AdditionalInputSpec ai : additionalInput) {
            sb.append(String.format("%s", ai.asXml()));
        }
        sb.append("    </layer>\n");
        return sb.toString();
    }

}