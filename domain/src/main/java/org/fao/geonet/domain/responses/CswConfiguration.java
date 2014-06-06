package org.fao.geonet.domain.responses;


import org.fao.geonet.domain.CswCapabilitiesInfoField;

import javax.xml.bind.annotation.*;
import java.util.List;

@XmlRootElement(name = "cswcapabilitiesinfofield")
@XmlAccessorType(XmlAccessType.FIELD)
public class CswConfiguration {

    public boolean isCswEnabled() {
		return cswEnabled;
	}

	public boolean isCswMetadataPublic() {
		return cswMetadataPublic;
	}

	private boolean cswEnabled;
    private boolean cswMetadataPublic;
    private int cswContactId;

    @XmlElement(name = "record")
    private List<CswCapabilitiesInfoField> capabilitiesInfoFields;

    public void setCswEnabled(boolean cswEnabled) {
        this.cswEnabled = cswEnabled;
    }

    public void setCswMetadataPublic(boolean cswMetadataPublic) {
        this.cswMetadataPublic = cswMetadataPublic;
    }

    public int getCswContactId() {
        return cswContactId;
    }

    public void setCswContactId(int cswContactId) {
        this.cswContactId = cswContactId;
    }

    public List<CswCapabilitiesInfoField> getCapabilitiesInfoFields() {
        return capabilitiesInfoFields;
    }

    public void setCapabilitiesInfoFields(List<CswCapabilitiesInfoField> capabilitiesInfoFields) {
        this.capabilitiesInfoFields = capabilitiesInfoFields;
    }
}
