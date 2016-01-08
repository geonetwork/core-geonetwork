package org.fao.geonet.services.metadata.resources;


import org.springframework.web.multipart.MultipartFile;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.BufferedOutputStream;

/**
 * Created by francois on 31/12/15.
 */
@XmlRootElement(name = "resource")
@XmlAccessorType(XmlAccessType.FIELD)
public interface Resource {
    String getId();
    String getUrl();
    String getType();
}
