package org.fao.geonet.api.anonymousAccessLink;

public class AnonymousAccessLinkDto {

    private int metadataId;
    private String metadataUuid;
    private String hash;

    public AnonymousAccessLinkDto() {
    }

    public int getMetadataId() {
        return metadataId;
    }

    public AnonymousAccessLinkDto setMetadataId(int metadataId) {
        this.metadataId = metadataId;
        return this;
    }

    public String getMetadataUuid() {
        return metadataUuid;
    }

    public AnonymousAccessLinkDto setMetadataUuid(String metadataUuid) {
        this.metadataUuid = metadataUuid;
        return this;
    }

    public String getHash() {
        return hash;
    }

    public AnonymousAccessLinkDto setHash(String hash) {
        this.hash = hash;
        return this;
    }

}
