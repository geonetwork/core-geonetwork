package org.fao.geonet.domain.responses;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.List;

/**
 * This class is a helper class for the service that returns the list of custom element sets.
 *
 * @author Jose García
 */
@XmlRootElement(name = "customelementsets")
@XmlAccessorType(XmlAccessType.FIELD)
public class CustomElementSetsListResponse implements Serializable {
    private static final long serialVersionUID = -6356219394588856310L;

    @XmlElement(name = "xpath")
    private List<String> xpaths;

    private boolean cswEnabled;

    public List<String> getXpaths() {
        return xpaths;
    }

    public void setXpaths(List<String> xpaths) {
        this.xpaths = xpaths;
    }

    public boolean isCswEnabled() {
        return cswEnabled;
    }

    public void setCswEnabled(boolean cswEnabled) {
        this.cswEnabled = cswEnabled;
    }
}
