/**
 * 
 */
package org.fao.geonet.services.openwis.util;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * 
 * DataTables response
 * 
 * openwis4-openwis-services
 * 
 * @author delawen
 * 
 * 
 */
public class Response {
    
    private Integer draw;
    private Long recordsTotal;
    private Long recordsFiltered;
    private List<Map<String, String>> data = new LinkedList<Map<String, String>>();

    public Integer getDraw() {
        return draw;
    }
    public void setDraw(Integer draw) {
        this.draw = draw;
    }
    public Long getRecordsTotal() {
        return recordsTotal;
    }
    public void setRecordsTotal(Long recordsTotal) {
        this.recordsTotal = recordsTotal;
    }
    public Long getRecordsFiltered() {
        return recordsFiltered;
    }
    public void setRecordsFiltered(Long recordsFiltered) {
        this.recordsFiltered = recordsFiltered;
    }
    public List<Map<String, String>> getData() {
        return data;
    }
    public void setData(List<Map<String, String>> data) {
        this.data = data;
    }
    public void addData(Map<String, String> data) {
        this.data.add(data);
    }
    
    //sAjax
    public Integer getsEcho() {
        return draw;
    }
    
    public Long getiTotalRecords() {
        return recordsTotal;
    }

    public Long getiTotalDisplayRecords() {
        return recordsFiltered;
    }

    public  List<Map<String, String>> getAaData() {
        return data;
    }
    

}
