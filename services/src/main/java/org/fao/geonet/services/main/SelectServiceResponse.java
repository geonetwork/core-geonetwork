package org.fao.geonet.services.main;

import jeeves.constants.Jeeves;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @deprecated Use the service with JSON format as output
 *
 * Created by francois on 22/10/15.
 */
@XmlRootElement(name = Jeeves.Elem.RESPONSE)
class SelectServiceResponse {
    String selected;

    public String getSelected() {
        return selected;
    }

    @XmlElement(name = "Selected")
    public SelectServiceResponse setSelected(String selected) {
        this.selected = selected;
        return this;
    }

    public SelectServiceResponse SelectServiceResponse() {
        return this;
    }
}
