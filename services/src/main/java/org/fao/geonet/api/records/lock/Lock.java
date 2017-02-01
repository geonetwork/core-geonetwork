/**
 * 
 */
package org.fao.geonet.api.records.lock;

/**
 * Response for
 * {@link MetadataLockApi#getAllLocks(javax.servlet.http.HttpServletRequest)}
 * 
 * @author delawen
 * 
 * 
 */
public class Lock {

    private String uuid;
    private String username;
    private String date;
    private Integer id;
    
    public String getUuid() {
        return uuid;
    }
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }
    public String getDate() {
        return date;
    }
    public void setDate(String date) {
        this.date = date;
    }
    public Integer getId() {
        return id;
    }
    public void setId(Integer id) {
        this.id = id;
    }
}
