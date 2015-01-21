package org.fao.geonet.domain.responses;

import org.fao.geonet.domain.Service;

import javax.xml.bind.annotation.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is a helper class for the service that returns the list CSW virtual services.
 *
 * XML output:
 *
 * <service>
 *   <record>
 *     <classname>.services.main.CswDiscoveryDispatcher</classname>
 *     <description>desc1</description>
 *     <id>3147</id>
 *     <name>csw-climate</name>
 *     <parameters>
 *     {keyword=climate}
 *     </parameters>
 *   </record>
 *   <record>
 *     <classname>.services.main.CswDiscoveryDispatcher</classname>
 *     <description>csw-water</description>
 *     <id>3315</id>
 *     <name>csw-servicenamecxz</name>
 *     <parameters>{title=water, abstract=water}</parameters>
 *   </record>
 * </service>
 *
 * @author Jose García
 * 
 */
@XmlRootElement(name = "service")
@XmlSeeAlso(Service.class)
@XmlAccessorType(XmlAccessType.FIELD)
public class CswVirtualServiceListResponse implements Serializable {
    private static final long serialVersionUID = 624011885854733512L;

    @XmlElement(name = "record")
    private List<CswVirtualServiceForList> servicesForList;

    public List<CswVirtualServiceForList> getRecord() {
        return servicesForList;
    }

    public void setRecord(List<CswVirtualServiceForList> servicesForList) {
        this.servicesForList = servicesForList;
    }

    public CswVirtualServiceListResponse() {

    }

    public CswVirtualServiceListResponse(List<Service> services) {

        servicesForList = new ArrayList<CswVirtualServiceForList>();

        for(Service service : services) {
            servicesForList.add(new CswVirtualServiceForList(service));
        }
    }
}
