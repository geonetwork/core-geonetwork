/**
 * 
 */
package org.fao.geonet.services.openwis.monitoring;

import java.util.List;

import org.openwis.monitoring.client.AlarmEventType;

/**
 * openwis4-openwis-services
 * 
 * @author delawen
 * 
 * 
 */
public class RecentEvents implements IResponse {

    
    private List<AlarmEventType> data;

    public  List<AlarmEventType> getData() {
        return data;
    }

    public void setData( List<AlarmEventType> data) {
        this.data = data;
    }

}
