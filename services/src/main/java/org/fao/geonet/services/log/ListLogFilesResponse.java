package org.fao.geonet.services.log;

import java.util.Collection;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.opengis.referencing.FactoryException;
import org.opengis.referencing.operation.TransformException;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;

/**
 * @author Jesse on 4/9/2015.
 */
@XmlRootElement(name = "response")
@XmlAccessorType(XmlAccessType.FIELD)
public class ListLogFilesResponse {
    @XmlElement
    @JsonProperty
    private Collection<LogFileResponse> logFile = Lists.newArrayList();


    public ListLogFilesResponse() {
    }

    public ListLogFilesResponse(List<LogFileResponse> logFileList) throws TransformException, FactoryException {
    	this.logFile.addAll(logFileList);

    }

    @XmlRootElement
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class LogFileResponse {
        @XmlAttribute
        @JsonProperty("file")
        private String file;
        @XmlAttribute
        @JsonProperty("name")
        private String name;
        
        public LogFileResponse() {
        }

        public LogFileResponse(String name, String file) {
            this.name = name;
            this.file = file;
        }
    }
}
