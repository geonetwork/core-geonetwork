package org.fao.geonet.harvester.wfsfeatures.model;

import javax.xml.bind.annotation.*;
import java.io.Serializable;
import java.util.Map;

/**
 * Created by francois on 14/01/16.
 */
@XmlRootElement(name = "wfs")
public class WFSHarvesterParameter implements Serializable {
    private String metadataUuid;

    private String url;

    private String typeName;

    private String version = "1.0.0";

    private int timeOut = 60000;

    private int maxFeatures = 2000;

    private String encoding = "UTF-8";

    public WFSHarvesterParameter () {}
    public WFSHarvesterParameter (String url, String typeName, String metadataUuid) {
        this.url = url;
        this.typeName = typeName;
        this.metadataUuid = metadataUuid;
    }

    /**
     * List of fields to tokenize during
     * indexing.
     *
     * The key is the column name, the value is the separator.
     */
    private Map<String, String> tokenize;

    @XmlAttribute(required = true)
    public String getUrl() {
        return url;
    }
    public void setUrl(String url) {
        this.url = url;
    }

    @XmlAttribute
    public String getTypeName() {
        return typeName;
    }
    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    @XmlAttribute
    public String getVersion() {
        return version;
    }
    public void setVersion(String version) {
        this.version = version;
    }

    @XmlAttribute
    public int getTimeOut() {
        return timeOut;
    }
    public void setTimeOut(int timeOut) {
        this.timeOut = timeOut;
    }

    @XmlAttribute
    public int getMaxFeatures() {
        return maxFeatures;
    }
    public void setMaxFeatures(int maxFeatures) {
        this.maxFeatures = maxFeatures;
    }

    @XmlAttribute
    public String getEncoding() {
        return encoding;
    }
    public void setEncoding(String encoding) {
        this.encoding = encoding;
    }

    @XmlAttribute
    public String getMetadataUuid() {
        return metadataUuid;
    }
    public void setMetadataUuid(String metadataUuid) {
        this.metadataUuid = metadataUuid;
    }

    @XmlElementWrapper(name="tokenize")
    public Map<String, String> getTokenize() {
        return tokenize;
    }
    public void setTokenize(Map<String, String> tokenize) {
        this.tokenize = tokenize;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer(this.getClass().getSimpleName());
        sb.append("\nurl: ").append(url);
        sb.append("\ntypeName: ").append(typeName);
        sb.append("\nversion:").append(version);
        sb.append("\nmetadataUuid:").append(metadataUuid);
        sb.append("\ntimeOut:").append(timeOut);
        sb.append("\nmaxFeatures:").append(maxFeatures);
        sb.append("\nencoding:").append(encoding);
        if (tokenize != null) {
            sb.append("\ntokenize: ");
            for (Map.Entry<String, String> e : tokenize.entrySet()) {
                sb.append(" * ")
                        .append(e.getKey())
                        .append(" separated by: ")
                        .append(e.getValue());
            }
        }
        return sb.toString();
    }

    private static final long serialVersionUID = 7526471155622776147L;
}
