/**
 * 
 */
package org.fao.geonet.services.openwis.subscription;

import javax.annotation.Nonnull;

import org.openwis.request.client.ClassOfService;
import org.openwis.request.client.Dissemination;
import org.openwis.request.client.ExtractMode;

/**
 * openwis4-openwis-services
 * 
 * @author delawen
 * 
 * 
 */
public class DisseminationPairRequest {

    @Nonnull
    private ExtractMode extractMode;
    private Dissemination primary;
    private Dissemination secondary;
    @Nonnull
    private String metadataUrn;
    
    @Nonnull
    private String username;
    
    private String email;
    private ClassOfService classOfService;
    private String requestType;
    
    public Dissemination getSecondary() {
        return secondary;
    }

    public void setSecondary(Dissemination secondary) {
        this.secondary = secondary;
    }

    public Dissemination getPrimary() {
        return primary;
    }

    public void setPrimary(Dissemination primary) {
        this.primary = primary;
    }

    public String getMetadataUrn() {
        return metadataUrn;
    }

    public void setMetadataUrn(String metadataUrn) {
        this.metadataUrn = metadataUrn;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public String toString() {
        return "DisseminationPair [primary=" + primary + ", secondary="
                + secondary + ", metadataUrn=" + metadataUrn + ", username="
                + username + "]";
    }

    public ExtractMode getExtractMode() {
        return extractMode;
    }

    public void setExtractMode(ExtractMode extractMode) {
        this.extractMode = extractMode;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public ClassOfService getClassOfService() {
        return classOfService;
    }

    public void setClassOfService(ClassOfService classOfService) {
        this.classOfService = classOfService;
    }

    public String getRequestType() {
        return requestType;
    }

    public void setRequestType(String requestType) {
        this.requestType = requestType;
    }

}
