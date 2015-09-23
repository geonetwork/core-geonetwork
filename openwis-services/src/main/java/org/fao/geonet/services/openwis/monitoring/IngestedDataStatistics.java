/**
 * 
 */
package org.fao.geonet.services.openwis.monitoring;

import java.util.List;

import org.openwis.monitoring.client.IngestedDataType;

/**
 * openwis4-openwis-services
 * 
 * @author delawen
 * 
 * 
 */
public class IngestedDataStatistics implements IResponse {

    
    private List<IngestedDataType> data;

    public  List<IngestedDataType> getData() {
        return data;
    }

    public void setData( List<IngestedDataType> data) {
        this.data = data;
    }


}
