package org.fao.geonet.domain.responses;

import org.fao.geonet.domain.Service;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/**
 * Wrapper for CSW virtual services (@see org.fao.geonet.domain.Service) used for the CSW virtual list service response.
 *
 * @author Jose García
 */
@XmlRootElement(name = "record")
@XmlAccessorType(XmlAccessType.FIELD)
public class CswVirtualServiceForList implements Serializable {
    private static final long serialVersionUID = 5342900799858099051L;

    private int id;
    private String name;
    @XmlElement(name="classname")
    private String className;
    private String description;
    private String parameters;

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getParameters() {
        return parameters;
    }

    public String getClassName() {
        return className;
    }

    public CswVirtualServiceForList() {

    }

    public CswVirtualServiceForList(Service service) {
        id = service.getId();
        name = service.getName();
        className = service.getClassName();
        description = service.getDescription();
        parameters = service.getParameters().toString();
    }
}

