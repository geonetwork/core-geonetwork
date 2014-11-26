package org.fao.geonet.services.statistics.response;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * IPStats response object.
 *
 * @author Jesse on 11/17/2014.
 */
@XmlRootElement(name = "response")
@XmlAccessorType(XmlAccessType.PROPERTY)
public class IpStats {
    private final String ip;
    private final long sumhit;

    public IpStats(String ip, long sumhit) {
        this.ip = ip;
        this.sumhit = sumhit;
    }

    /**
     * Get the ip address of the user performing the searches
     */
    public String getIp() {
        return ip;
    }

    /**
     * Get the total number of hits (the sum of all found records by this ip address).
     */
    public long getSumhit() {
        return sumhit;
    }

    @Override
    public String toString() {
        return "IpStats{" +
               "ip='" + ip + '\'' +
               ", sumhit=" + sumhit +
               '}';
    }
}
