package org.fao.geonet.domain.responses;

import org.fao.geonet.domain.Service;
import org.fao.geonet.domain.ServiceParam;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * This class is a helper class for the service that returns a csw virtual service configuration.
 *
 * XML output:
 *
 * <service>
 *   <record>
 *     <id>391</id>
 *     <name>mycswservice</name>
 *     <description>A custom CSW service</description>
 *     <parameters>{any=water, title=africa}</parameters>
 *   </record>
 * </service>
 *
 * @author Jose García
 */

@XmlRootElement(name = "service")
@XmlAccessorType(XmlAccessType.FIELD)
public class CswVirtualServiceResponse implements Serializable {
    private static final long serialVersionUID = 5342900799858099051L;

    private int id;
    private String name;
    private String description;
    private String explicitQuery;

    public List<ServiceParameter> getParameter() {
        return parameter;
    }

    public void setParameter(List<ServiceParameter> parameter) {
        this.parameter = parameter;
    }

    @XmlElementWrapper(name = "serviceParameters")
    private List<ServiceParameter> parameter;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getExplicitQuery() {
        return explicitQuery;
    }

    public void setExplicitQuery(String explicitQuery) {
        this.explicitQuery = explicitQuery;
    }

    public CswVirtualServiceResponse() {

    }

    public CswVirtualServiceResponse(Service service) {

        id = service.getId();
        name = service.getName();
        description = service.getDescription();
        this.explicitQuery = service.getExplicitQuery();
        parameter = new ArrayList<>();

        final List<ServiceParam> serviceParameters = service.getParameters();

        for (ServiceParam serviceParameter : serviceParameters) {
            parameter.add(new ServiceParameter(serviceParameter));
        }
    }
}

