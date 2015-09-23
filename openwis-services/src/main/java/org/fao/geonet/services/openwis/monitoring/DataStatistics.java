/**
 * 
 */
package org.fao.geonet.services.openwis.monitoring;

import java.util.List;

import org.openwis.monitoring.client.DisseminatedDataType;

/**
 * openwis4-openwis-services
 * 
 * @author delawen
 * 
 * 
 */
public class DataStatistics implements IResponse {
    private List<DisseminatedDataType> data;

    public List<DisseminatedDataType> getData() {
        return data;
    }

    public void setData(List<DisseminatedDataType> data) {
        this.data = data;
    }
    
}
