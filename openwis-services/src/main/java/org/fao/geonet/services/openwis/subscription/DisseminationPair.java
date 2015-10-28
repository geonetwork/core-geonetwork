/**
 * 
 */
package org.fao.geonet.services.openwis.subscription;

import javax.annotation.Nonnull;

import org.openwis.subscription.client.Dissemination;
import org.openwis.subscription.client.ExtractMode;

/**
 * openwis4-openwis-services
 * 
 * @author delawen
 * 
 * 
 */
public class DisseminationPair {

    private Long id;
    @Nonnull
    private ExtractMode extractMode;
    private Dissemination primary;
    private Dissemination secondary;
    @Nonnull
    private String metadataUrn;
    
    @Nonnull
    private String username;
    
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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

}
